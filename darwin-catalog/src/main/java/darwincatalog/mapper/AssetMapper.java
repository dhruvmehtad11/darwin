package darwincatalog.mapper;

import static darwincatalog.util.Common.mapToDto;
import static darwincatalog.util.Common.validateFields;
import static darwincatalog.util.Constants.ASSET_ATTRIBUTES_JSON_TO_POJO;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.AssetLineageEntity;
import darwincatalog.entity.AssetTagRelationEntity;
import darwincatalog.entity.FieldLineageEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.exception.AssetException;
import darwincatalog.repository.AssetLineageRepository;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.AssetTagRelationRepository;
import darwincatalog.repository.FieldLineageRepository;
import darwincatalog.repository.RuleRepository;
import darwincatalog.repository.SchemaRepository;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.util.Common;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.Asset;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.AssetSchema;
import org.openapitools.model.ParentDependency;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AssetMapper implements EntityResponseMapper<AssetEntity, Asset> {

  private final AssetHierarchyResolver assetHierarchyResolver;
  private final TagMapper tagMapper;
  private final RuleMapper ruleMapper;
  private final RuleRepository ruleRepository;
  private final AssetTagRelationRepository assetTagRelationRepository;
  private final SchemaRepository schemaRepository;
  private final SchemaMapper schemaMapper;
  private final AssetLineageRepository assetLineageRepository;
  private final AssetRepository assetRepository;
  private final FieldLineageRepository fieldLineageRepository;

  public AssetMapper(
      AssetHierarchyResolver assetHierarchyResolver,
      TagMapper tagMapper,
      RuleMapper ruleMapper,
      RuleRepository ruleRepository,
      AssetTagRelationRepository assetTagRelationRepository,
      SchemaRepository schemaRepository,
      SchemaMapper schemaMapper,
      AssetLineageRepository assetLineageRepository,
      AssetRepository assetRepository,
      FieldLineageRepository fieldLineageRepository) {
    this.assetHierarchyResolver = assetHierarchyResolver;
    this.tagMapper = tagMapper;
    this.ruleMapper = ruleMapper;
    this.ruleRepository = ruleRepository;
    this.assetTagRelationRepository = assetTagRelationRepository;
    this.schemaRepository = schemaRepository;
    this.schemaMapper = schemaMapper;
    this.assetLineageRepository = assetLineageRepository;
    this.assetRepository = assetRepository;
    this.fieldLineageRepository = fieldLineageRepository;
  }

  @Override
  public AssetEntity toEntity(Asset asset) {
    // impl not required
    return new AssetEntity();
  }

  @Override
  public Asset toDto(AssetEntity entity) {
    Long createdAt =
        entity.getAssetCreatedAt() == null ? null : entity.getAssetCreatedAt().toEpochMilli();
    Long updatedAt =
        entity.getAssetUpdatedAt() == null ? null : entity.getAssetUpdatedAt().toEpochMilli();

    AssetDetail assetDetail = assetHierarchyResolver.getDetails(entity);

    return new Asset()
        .fqdn(entity.getFqdn())
        .type(entity.getType())
        .description(entity.getDescription())
        .sourcePlatform(entity.getSourcePlatform())
        .businessRoster(entity.getBusinessRoster())
        .detail(assetDetail)
        .assetCreatedAt(createdAt)
        .assetUpdatedAt(updatedAt)
        .metadata(entity.getMetadata());
  }

  public List<Asset> toDto(List<AssetEntity> entities, Set<String> fields) {
    validateFields(fields);
    return entities.stream()
        .map(entity -> toDtoHelper(entity, fields, false))
        .collect(java.util.stream.Collectors.toList());
  }

  public Asset toDto(AssetEntity entity, Set<String> fields) {
    validateFields(fields);
    return toDtoHelper(entity, fields, true);
  }

  public Asset toLineageAssetDto(AssetEntity entity) {
    Set<String> fields = Set.of("fqdn", "asset_schema", "type", "detail");
    validateFields(fields);
    return toDtoHelper(entity, fields, false);
  }

  private Asset toDtoHelper(
      AssetEntity entity, Set<String> fields, boolean includeClassificationInformation) {
    Asset asset = this.toDto(entity);
    if (fields.contains("tags")) {
      List<AssetTagRelationEntity> assetTagRelationEntities =
          assetTagRelationRepository.findAllByAssetId(entity.getId());
      asset.setTags(mapToDto(assetTagRelationEntities, tagMapper));
    }
    if (fields.contains("rules")) {
      List<RuleEntity> ruleEntities = ruleRepository.findByAssetId(entity.getId());
      asset.setRules(mapToDto(ruleEntities, ruleMapper));
    }
    if (fields.contains("asset_schema")) {
      schemaRepository
          .findByAssetIdWithMaxVersionId(entity.getId())
          .ifPresent(
              schemaEntity -> {
                AssetSchema assetSchema =
                    schemaMapper.toDto(schemaEntity, includeClassificationInformation);
                if (!includeClassificationInformation) {
                  assetSchema.setSchemaClassificationStatus(null);
                }
                asset.setAssetSchema(assetSchema);
              });
    }
    if (fields.contains("immediate_parents")) {
      List<AssetLineageEntity> assetLineageEntityList =
          assetLineageRepository.findByToAssetId(entity.getId());
      List<Long> parentAssetIds =
          assetLineageEntityList.stream()
              .map(AssetLineageEntity::getFromAssetId)
              .collect(java.util.stream.Collectors.toList());

      Map<Long, String> parentAssetIdToFqdn = new HashMap<>();
      assetRepository
          .findAllById(parentAssetIds)
          .forEach(
              assetEntity -> parentAssetIdToFqdn.put(assetEntity.getId(), assetEntity.getFqdn()));

      List<ParentDependency> parentDependencies =
          assetLineageEntityList.stream()
              .map(
                  assetLineageEntity -> {
                    List<FieldLineageEntity> fieldLineageEntityList =
                        fieldLineageRepository.findAllByAssetLineageId(assetLineageEntity.getId());
                    Map<String, List<String>> fieldsMap = new HashMap<>();
                    fieldLineageEntityList.forEach(
                        fieldLineageEntity ->
                            Common.constructFieldMap(fieldLineageEntity, fieldsMap));
                    return new ParentDependency()
                        .name(parentAssetIdToFqdn.get(assetLineageEntity.getFromAssetId()))
                        .fieldsMap(fieldsMap);
                  })
              .collect(java.util.stream.Collectors.toList());
      asset.setImmediateParents(parentDependencies);
    }
    removeExtraProperties(asset, fields);
    return asset;
  }

  private void removeExtraProperties(Asset asset, Set<String> fields) {
    for (Map.Entry<String, String> entry : ASSET_ATTRIBUTES_JSON_TO_POJO.entrySet()) {
      try {
        if (!fields.contains(entry.getKey())) {
          String pojoAttribute = entry.getValue();
          java.lang.reflect.Field field = asset.getClass().getDeclaredField(pojoAttribute);
          field.setAccessible(true);
          field.set(asset, null);
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new AssetException("Failed while whitelisting requested attributes");
      }
    }
  }
}
