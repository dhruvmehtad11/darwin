package darwincatalog.service;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.AssetLineageEntity;
import darwincatalog.entity.FieldLineageEntity;
import darwincatalog.entity.SchemaEntity;
import darwincatalog.exception.AssetNotFoundException;
import darwincatalog.exception.CyclicDependencyException;
import darwincatalog.mapper.LineageMapper;
import darwincatalog.repository.AssetLineageRepository;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.FieldLineageRepository;
import darwincatalog.util.SqlParser;
import io.openlineage.client.OpenLineage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.Lineage;
import org.openapitools.model.ParentDependency;
import org.openapitools.model.ParseLineageRequest;
import org.openapitools.model.TableType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LineageService {

  private final AssetRepository assetRepository;
  private final SqlParser sqlParser;
  private final AssetLineageRepository assetLineageRepository;
  private final FieldLineageRepository fieldLineageRepository;
  private final LineageMapper lineageMapper;
  private final SchemaService schemaService;
  private final ObjectMapper objectMapper;

  public LineageService(
      AssetRepository assetRepository,
      SqlParser sqlParser,
      AssetLineageRepository assetLineageRepository,
      FieldLineageRepository fieldLineageRepository,
      LineageMapper lineageMapper,
      SchemaService schemaService,
      @Qualifier("customMapper") ObjectMapper objectMapper) {
    this.assetRepository = assetRepository;
    this.sqlParser = sqlParser;
    this.assetLineageRepository = assetLineageRepository;
    this.fieldLineageRepository = fieldLineageRepository;
    this.lineageMapper = lineageMapper;
    this.schemaService = schemaService;
    this.objectMapper = objectMapper;
  }

  public List<ParentDependency> parseLineage(ParseLineageRequest parseLineageRequest) {
    List<ParentDependency> parentDependencies = new ArrayList<>();
    Set<String> tableNames = sqlParser.getTableNames(parseLineageRequest.getSourceQuery());
    tableNames.forEach(
        tableName ->
            mapToParent(tableName, parseLineageRequest.getTableType())
                .ifPresentOrElse(
                    parentDependencies::add,
                    () -> log.error("No parent found: {}. Will continue", tableName)));
    return parentDependencies;
  }

  private Optional<ParentDependency> mapToParent(String tableName, TableType tableType) {

    String searchString;
    Optional<ParentDependency> parentDependency = Optional.empty();
    switch (tableType) {
      case LAKEHOUSE:
        searchString = HIERARCHY_SEPARATOR + "lakehouse" + HIERARCHY_SEPARATOR;
        break;
      case REDSHIFT:
        searchString = HIERARCHY_SEPARATOR + "redshift" + HIERARCHY_SEPARATOR;
        break;
      default:
        throw new IllegalArgumentException("Invalid table type: " + tableType);
    }

    String parsedTableName =
        tableName.toLowerCase().replace(".", HIERARCHY_SEPARATOR).replace("'", "");
    List<AssetEntity> matchedEntities =
        assetRepository.findByFqdnContainingAndFqdnEndsWith(searchString, parsedTableName);
    if (!matchedEntities.isEmpty()) {
      if (matchedEntities.size() > 1) {
        log.warn(
            "multiple assets found for table name: {}, size: {}. Returning the first match",
            parsedTableName,
            matchedEntities.size());
      }
      AssetEntity parentEntity = matchedEntities.get(0);
      parentDependency = Optional.of(new ParentDependency().name(parentEntity.getFqdn()));
    }
    return parentDependency;
  }

  public Lineage getLineage(String assetName) {
    AssetEntity assetEntity =
        assetRepository
            .findByFqdn(assetName)
            .orElseThrow(() -> new AssetNotFoundException(assetName));
    List<AssetLineageEntity> assetLineageEntityList =
        assetLineageRepository.findAssetLineageByAssetId(assetEntity.getId());

    return lineageMapper.toLineageDto(assetLineageEntityList);
  }

  @SuppressWarnings("unchecked")
  public void handleParentDependencies(
      AssetEntity childAssetEntity, List<ParentDependency> parentDependencies) {
    String childAssetName = childAssetEntity.getFqdn();
    if (parentDependencies.stream()
        .map(ParentDependency::getName)
        .collect(java.util.stream.Collectors.toList())
        .contains(childAssetName)) {

      throw new CyclicDependencyException(childAssetName, childAssetName);
    }

    List<Long> downStreamAssetIds =
        assetLineageRepository.getRecursiveDownStreamAssetIds(childAssetEntity.getId());

    Map<String, AssetEntity> parentAssetsMap = new HashMap<>();
    parentDependencies.forEach(
        parentDependency -> {
          AssetEntity parentAssetEntity =
              assetRepository
                  .findByFqdn(parentDependency.getName())
                  .orElseThrow(() -> new AssetNotFoundException(parentDependency.getName()));
          if (downStreamAssetIds.contains(parentAssetEntity.getId())) {
            throw new CyclicDependencyException(parentAssetEntity.getFqdn(), childAssetName);
          }
          parentAssetsMap.put(parentAssetEntity.getFqdn(), parentAssetEntity);
        });

    List<AssetLineageEntity> existingAssetLineageList =
        assetLineageRepository.getAssetLineageEntitiesByToAssetId(childAssetEntity.getId());
    assetLineageRepository.deleteAll(existingAssetLineageList);

    parentDependencies.forEach(
        parentDependency -> {
          long parentAssetId = parentAssetsMap.get(parentDependency.getName()).getId();
          updateAssetLineage(parentAssetId, childAssetEntity.getId(), parentDependency);
        });
  }

  private void updateAssetLineage(
      long parentAssetId, long childAssetId, ParentDependency parentDependency) {

    Long assetLineageId =
        assetLineageRepository
            .save(
                AssetLineageEntity.builder()
                    .fromAssetId(parentAssetId)
                    .toAssetId(childAssetId)
                    .build())
            .getId();
    parentDependency
        .getFieldsMap()
        .forEach(
            (parentFieldName, childFieldNames) ->
                updateFieldLineage(
                    parentAssetId, childAssetId, parentFieldName, childFieldNames, assetLineageId));
  }

  private void updateFieldLineage(
      long parentAssetId,
      long childAssetId,
      String parentFieldName,
      List<String> childFieldNames,
      Long assetLineageId) {
    SchemaEntity parentSchemaEntity = schemaService.findByAssetIdWithMaxVersionId(parentAssetId);
    schemaService.fieldNameExists(parentSchemaEntity, parentFieldName);

    SchemaEntity childSchemaEntity = schemaService.findByAssetIdWithMaxVersionId(childAssetId);

    childFieldNames.forEach(
        (childFieldName) -> schemaService.fieldNameExists(childSchemaEntity, childFieldName));

    List<FieldLineageEntity> fieldLineageEntityList =
        childFieldNames.stream()
            .map(
                (childFieldName) ->
                    FieldLineageEntity.builder()
                        .assetLineageId(assetLineageId)
                        .fromFieldName(parentFieldName)
                        .toFieldName(childFieldName)
                        .build())
            .collect(java.util.stream.Collectors.toList());

    fieldLineageRepository.saveAll(fieldLineageEntityList);
  }

  public void parseOpenLineageEvent(Map<String, Object> openLineageEvent) {
    if (openLineageEvent == null || openLineageEvent.isEmpty()) {
      log.warn("OpenLineage event is null or empty. Skipping processing.");
      return;
    }

    try {
      OpenLineage.RunEvent runEvent =
          objectMapper.convertValue(openLineageEvent, OpenLineage.RunEvent.class);

      if (runEvent == null) {
        log.warn("Failed to parse OpenLineage event. Skipping processing.");
        return;
      }

      // Extract parent (input) datasets and child (output) datasets
      List<OpenLineage.InputDataset> inputDatasets = runEvent.getInputs();
      List<OpenLineage.OutputDataset> outputDatasets = runEvent.getOutputs();

      if (outputDatasets == null || outputDatasets.isEmpty()) {
        log.info("No output datasets found in OpenLineage event. Skipping processing.");
        return;
      }

      log.info(
          "Processing OpenLineage event with {} input datasets and {} output datasets",
          inputDatasets != null ? inputDatasets.size() : 0,
          outputDatasets.size());

      // Process each output dataset and its lineage relationships
      for (OpenLineage.OutputDataset outputDataset : outputDatasets) {
        String childFqdn = constructFqdn(outputDataset.getNamespace(), outputDataset.getName());

        if (childFqdn == null) {
          log.warn(
              "Failed to construct FQDN for output dataset namespace: {}, name: {}. Skipping.",
              outputDataset.getNamespace(),
              outputDataset.getName());
          continue;
        }

        log.info("Processing child asset: {}", childFqdn);

        // Find or validate child asset exists in our system
        Optional<AssetEntity> childAssetOpt = assetRepository.findByFqdn(childFqdn);
        if (childAssetOpt.isEmpty()) {
          log.warn("Child asset not found in system: {}. Skipping lineage processing.", childFqdn);
          continue;
        }
        AssetEntity childAsset = childAssetOpt.get();

        // Extract column lineage information if available
        if (outputDataset.getFacets() != null
            && outputDataset.getFacets().getColumnLineage() != null) {
          OpenLineage.ColumnLineageDatasetFacet columnLineageFacet =
              outputDataset.getFacets().getColumnLineage();
          processColumnLineage(childAsset, columnLineageFacet);
        } else {
          // If no column lineage, process dataset-level lineage only
          if (inputDatasets != null && !inputDatasets.isEmpty()) {
            processDatasetLineage(childAsset, inputDatasets);
          } else {
            log.debug("No input datasets or column lineage found for asset: {}", childFqdn);
          }
        }
      }

      log.info("Completed processing OpenLineage event");
    } catch (Exception e) {
      log.error("Error processing OpenLineage event: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to process OpenLineage event", e);
    }
  }

  private String constructFqdn(String namespace, String name) {
    // Construct FQDN from namespace and name
    // Based on the existing FQDN format in the codebase (e.g.,
    // "example:table:redshift:segment:example:benefitmaster")
    if (namespace == null || name == null) {
      log.warn("Invalid namespace or name: namespace={}, name={}", namespace, name);
      return null;
    }
    return namespace + HIERARCHY_SEPARATOR + name;
  }

  @SuppressWarnings("unchecked")
  private void processColumnLineage(
      AssetEntity childAsset, OpenLineage.ColumnLineageDatasetFacet columnLineageFacet) {
    try {
      // Use Jackson to convert the facet to a Map for easier processing
      Map<String, Object> facetMap = objectMapper.convertValue(columnLineageFacet, Map.class);
      Object fieldsObj = facetMap.get("fields");

      if (fieldsObj instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) fieldsObj;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
          String outputFieldName = entry.getKey();
          Object fieldLineageObj = entry.getValue();

          if (fieldLineageObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldLineageMap = (Map<String, Object>) fieldLineageObj;

            Object inputFieldsObj = fieldLineageMap.get("inputFields");
            if (inputFieldsObj instanceof List) {
              @SuppressWarnings("unchecked")
              List<Map<String, Object>> inputFields = (List<Map<String, Object>>) inputFieldsObj;

              for (Map<String, Object> inputField : inputFields) {
                String namespace = (String) inputField.get("namespace");
                String name = (String) inputField.get("name");
                String fieldName = (String) inputField.get("field");

                if (namespace != null && name != null && fieldName != null) {
                  String parentFqdn = constructFqdn(namespace, name);

                  if (parentFqdn == null) {
                    log.warn(
                        "Failed to construct FQDN for namespace: {}, name: {}", namespace, name);
                    continue;
                  }

                  log.debug(
                      "Processing field lineage: {} -> {}",
                      parentFqdn + "." + fieldName,
                      childAsset.getFqdn() + "." + outputFieldName);

                  // Find parent asset
                  Optional<AssetEntity> parentAssetOpt = assetRepository.findByFqdn(parentFqdn);
                  if (parentAssetOpt.isEmpty()) {
                    log.warn(
                        "Parent asset not found in system: {}. Skipping this lineage entry.",
                        parentFqdn);
                    continue;
                  }
                  AssetEntity parentAsset = parentAssetOpt.get();

                  // Create or update asset lineage relationship
                  Long assetLineageId =
                      createOrUpdateAssetLineage(parentAsset.getId(), childAsset.getId());

                  // Create field lineage relationship
                  createFieldLineage(assetLineageId, fieldName, outputFieldName);
                }
              }
            }
          }
        }
      } else {
        log.debug(
            "No field mappings found in column lineage facet for asset: {}", childAsset.getFqdn());
      }
    } catch (Exception e) {
      log.error(
          "Error processing column lineage for asset {}: {}",
          childAsset.getFqdn(),
          e.getMessage(),
          e);
    }
  }

  private void processDatasetLineage(
      AssetEntity childAsset, List<OpenLineage.InputDataset> inputDatasets) {
    for (OpenLineage.InputDataset inputDataset : inputDatasets) {
      String parentFqdn = constructFqdn(inputDataset.getNamespace(), inputDataset.getName());

      if (parentFqdn == null) {
        log.warn(
            "Failed to construct FQDN for namespace: {}, name: {}",
            inputDataset.getNamespace(),
            inputDataset.getName());
        continue;
      }

      log.debug("Processing dataset lineage: {} -> {}", parentFqdn, childAsset.getFqdn());

      // Find parent asset
      Optional<AssetEntity> parentAssetOpt = assetRepository.findByFqdn(parentFqdn);
      if (parentAssetOpt.isEmpty()) {
        log.warn("Parent asset not found in system: {}. Skipping this lineage entry.", parentFqdn);
        continue;
      }
      AssetEntity parentAsset = parentAssetOpt.get();

      // Create asset lineage relationship
      createOrUpdateAssetLineage(parentAsset.getId(), childAsset.getId());
    }
  }

  private Long createOrUpdateAssetLineage(Long parentAssetId, Long childAssetId) {
    // Check if lineage relationship already exists
    List<AssetLineageEntity> existingLineages =
        assetLineageRepository.findByToAssetId(childAssetId);
    Optional<AssetLineageEntity> existingLineage =
        existingLineages.stream()
            .filter(lineage -> lineage.getFromAssetId().equals(parentAssetId))
            .findFirst();

    if (existingLineage.isPresent()) {
      log.debug("Asset lineage already exists: {} -> {}", parentAssetId, childAssetId);
      return existingLineage.get().getId();
    }

    // Create new asset lineage
    AssetLineageEntity newLineage =
        AssetLineageEntity.builder().fromAssetId(parentAssetId).toAssetId(childAssetId).build();

    AssetLineageEntity savedLineage = assetLineageRepository.save(newLineage);
    log.info("Created new asset lineage: {} -> {}", parentAssetId, childAssetId);
    return savedLineage.getId();
  }

  private void createFieldLineage(Long assetLineageId, String fromFieldName, String toFieldName) {
    // Check if field lineage already exists for this asset lineage
    List<FieldLineageEntity> existingFieldLineages =
        fieldLineageRepository.findAllByAssetLineageId(assetLineageId);
    boolean exists =
        existingFieldLineages.stream()
            .anyMatch(
                fieldLineage ->
                    fieldLineage.getFromFieldName().equals(fromFieldName)
                        && fieldLineage.getToFieldName().equals(toFieldName));

    if (exists) {
      log.debug(
          "Field lineage already exists: {} -> {} for asset lineage {}",
          fromFieldName,
          toFieldName,
          assetLineageId);
      return;
    }

    // Create new field lineage
    FieldLineageEntity fieldLineage =
        FieldLineageEntity.builder()
            .assetLineageId(assetLineageId)
            .fromFieldName(fromFieldName)
            .toFieldName(toFieldName)
            .build();

    fieldLineageRepository.save(fieldLineage);
    log.info(
        "Created new field lineage: {} -> {} for asset lineage {}",
        fromFieldName,
        toFieldName,
        assetLineageId);
  }
}
