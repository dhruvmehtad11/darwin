package darwincatalog.service;

import static darwincatalog.util.Common.generateCanonicalHash;

import com.fasterxml.jackson.core.JsonProcessingException;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.SchemaEntity;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.SchemaRepository;
import darwincatalog.util.ObjectMapperUtils;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AssetDescription;
import org.openapitools.model.Field;
import org.openapitools.model.FieldDescription;
import org.openapitools.model.PutBulkDescriptionsRequest;
import org.openapitools.model.PutBulkDescriptionsResponse;
import org.openapitools.model.PutBulkDescriptionsResponseFailedFieldUpdatesInner;
import org.openapitools.model.SchemaStructure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class BulkDescriptionService {

  private final AssetRepository assetRepository;
  private final SchemaRepository schemaRepository;
  private final ObjectMapperUtils objectMapperUtils;

  public BulkDescriptionService(
      AssetRepository assetRepository,
      SchemaRepository schemaRepository,
      ObjectMapperUtils objectMapperUtils) {
    this.assetRepository = assetRepository;
    this.schemaRepository = schemaRepository;
    this.objectMapperUtils = objectMapperUtils;
  }

  @Transactional
  public PutBulkDescriptionsResponse updateBulkDescriptions(PutBulkDescriptionsRequest request) {
    PutBulkDescriptionsResponse response = new PutBulkDescriptionsResponse();

    // Initialize response counters
    response.successfulAssetUpdates(0);
    response.successfulFieldUpdates(0);
    response.failedAssetUpdates(new ArrayList<>());
    response.failedFieldUpdates(new ArrayList<>());

    // Process asset descriptions
    if (!CollectionUtils.isEmpty(request.getAssetDescriptions())) {
      processAssetDescriptions(request.getAssetDescriptions(), response);
    }

    // Process field descriptions
    if (!CollectionUtils.isEmpty(request.getFieldDescriptions())) {
      processFieldDescriptions(request.getFieldDescriptions(), response);
    }

    return response;
  }

  private void processAssetDescriptions(
      List<AssetDescription> assetDescriptions, PutBulkDescriptionsResponse response) {
    for (AssetDescription assetDesc : assetDescriptions) {
      try {
        Optional<AssetEntity> assetOpt = assetRepository.findByFqdn(assetDesc.getAssetFqdn());
        if (assetOpt.isPresent()) {
          AssetEntity asset = assetOpt.get();
          asset.setDescription(assetDesc.getDescription());
          assetRepository.save(asset);
          response.setSuccessfulAssetUpdates(response.getSuccessfulAssetUpdates() + 1);
          log.debug("Updated description for asset: {}", assetDesc.getAssetFqdn());
        } else {
          response.getFailedAssetUpdates().add(assetDesc.getAssetFqdn());
          log.warn("Asset not found: {}", assetDesc.getAssetFqdn());
        }
      } catch (Exception e) {
        response.getFailedAssetUpdates().add(assetDesc.getAssetFqdn());
        log.error("Failed to update description for asset: {}", assetDesc.getAssetFqdn(), e);
      }
    }
  }

  private void processFieldDescriptions(
      List<FieldDescription> fieldDescriptions, PutBulkDescriptionsResponse response) {
    // Group field descriptions by asset FQDN for efficient processing
    Map<String, List<FieldDescription>> fieldsByAsset =
        fieldDescriptions.stream()
            .collect(java.util.stream.Collectors.groupingBy(FieldDescription::getAssetFqdn));

    for (Map.Entry<String, List<FieldDescription>> entry : fieldsByAsset.entrySet()) {
      String assetFqdn = entry.getKey();
      List<FieldDescription> fieldsForAsset = entry.getValue();

      try {
        // Find the asset
        Optional<AssetEntity> assetOpt = assetRepository.findByFqdn(assetFqdn);
        if (assetOpt.isEmpty()) {
          // Add all fields for this asset to failed updates
          for (FieldDescription fieldDesc : fieldsForAsset) {
            addFailedFieldUpdate(response, fieldDesc, "Asset not found: " + assetFqdn);
          }
          continue;
        }

        // Find the latest schema for this asset
        Optional<SchemaEntity> schemaOpt =
            schemaRepository.findByAssetIdWithMaxVersionId(assetOpt.get().getId());
        if (schemaOpt.isEmpty()) {
          // Add all fields for this asset to failed updates
          for (FieldDescription fieldDesc : fieldsForAsset) {
            addFailedFieldUpdate(response, fieldDesc, "No schema found for asset: " + assetFqdn);
          }
          continue;
        }

        // Update field descriptions in the schema
        updateFieldDescriptionsInSchema(schemaOpt.get(), fieldsForAsset, response);

      } catch (Exception e) {
        log.error("Failed to process field descriptions for asset: {}", assetFqdn, e);
        for (FieldDescription fieldDesc : fieldsForAsset) {
          addFailedFieldUpdate(response, fieldDesc, "Processing error: " + e.getMessage());
        }
      }
    }
  }

  private void updateFieldDescriptionsInSchema(
      SchemaEntity schemaEntity,
      List<FieldDescription> fieldDescriptions,
      PutBulkDescriptionsResponse response)
      throws JsonProcessingException, NoSuchAlgorithmException {

    // Convert schema JSON to SchemaStructure
    SchemaStructure schemaStructure =
        objectMapperUtils.convertMapToSchema(schemaEntity.getSchemaJson());

    boolean schemaUpdated = false;
    Set<String> updatedFields = new HashSet<>();

    // Update field descriptions
    for (FieldDescription fieldDesc : fieldDescriptions) {
      boolean fieldFound = false;

      if (schemaStructure.getFields() != null) {
        for (Field field : schemaStructure.getFields()) {
          if (field.getName().equals(fieldDesc.getFieldName())) {
            field.setDoc(fieldDesc.getDescription());
            fieldFound = true;
            schemaUpdated = true;
            updatedFields.add(fieldDesc.getFieldName());
            response.setSuccessfulFieldUpdates(response.getSuccessfulFieldUpdates() + 1);
            log.debug(
                "Updated description for field '{}' in asset: {}",
                fieldDesc.getFieldName(),
                fieldDesc.getAssetFqdn());
            break;
          }
        }
      }

      if (!fieldFound) {
        addFailedFieldUpdate(
            response, fieldDesc, "Field not found in schema: " + fieldDesc.getFieldName());
      }
    }

    // Save updated schema if any changes were made
    if (schemaUpdated) {
      Map<String, Object> updatedSchemaJson = objectMapperUtils.convertSchemaToMap(schemaStructure);

      // Check if the schema content actually changed (different hash)
      String newHash = generateCanonicalHash(updatedSchemaJson);
      if (!newHash.equals(schemaEntity.getSchemaHash())) {
        schemaEntity.setSchemaJson(updatedSchemaJson);
        schemaRepository.save(schemaEntity);
        log.debug(
            "Saved updated schema for asset ID: {} with {} field description updates",
            schemaEntity.getAssetId(),
            updatedFields.size());
      } else {
        log.debug(
            "Schema hash unchanged for asset ID: {}, no save needed", schemaEntity.getAssetId());
      }
    }
  }

  private void addFailedFieldUpdate(
      PutBulkDescriptionsResponse response, FieldDescription fieldDesc, String error) {
    PutBulkDescriptionsResponseFailedFieldUpdatesInner failedUpdate =
        new PutBulkDescriptionsResponseFailedFieldUpdatesInner()
            .assetFqdn(fieldDesc.getAssetFqdn())
            .fieldName(fieldDesc.getFieldName())
            .error(error);
    response.getFailedFieldUpdates().add(failedUpdate);
    log.warn(
        "Failed to update field '{}' in asset '{}': {}",
        fieldDesc.getFieldName(),
        fieldDesc.getAssetFqdn(),
        error);
  }
}
