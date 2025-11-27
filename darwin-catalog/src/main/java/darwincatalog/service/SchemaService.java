package darwincatalog.service;

import static darwincatalog.util.Common.generateCanonicalHash;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.SchemaEntity;
import darwincatalog.exception.FieldNotFoundException;
import darwincatalog.exception.MissingSchemaVersionException;
import darwincatalog.exception.SchemaNotFoundException;
import darwincatalog.mapper.SchemaMapper;
import darwincatalog.repository.SchemaRepository;
import darwincatalog.util.ObjectMapperUtils;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AssetSchema;
import org.openapitools.model.PostAssetSchema;
import org.openapitools.model.PutAssetSchema;
import org.openapitools.model.SchemaStructure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class SchemaService {
  private final SchemaRepository schemaRepository;
  private final SchemaClassificationService schemaClassificationService;
  private final ObjectMapperUtils objectMapperUtils;
  private final ValidatorService validatorService;
  private final SchemaMapper schemaMapper;

  public SchemaService(
      SchemaRepository schemaRepository,
      SchemaClassificationService schemaClassificationService,
      ObjectMapperUtils objectMapperUtils,
      ValidatorService validatorService,
      SchemaMapper schemaMapper) {
    this.schemaRepository = schemaRepository;
    this.schemaClassificationService = schemaClassificationService;
    this.objectMapperUtils = objectMapperUtils;
    this.validatorService = validatorService;
    this.schemaMapper = schemaMapper;
  }

  public void registerAssetSchema(AssetEntity assetEntity, PostAssetSchema postAssetSchema) {
    if (postAssetSchema != null && postAssetSchema.getSchemaJson() != null) {
      Map<String, Object> schemaMapInRequest =
          objectMapperUtils.convertSchemaToMap(postAssetSchema.getSchemaJson());
      SchemaEntity savedSchema = registerTheFirstAssetSchema(assetEntity, schemaMapInRequest);
      if (!CollectionUtils.isEmpty(postAssetSchema.getSchemaClassificationStatus())) {
        schemaClassificationService.postSchemaClassification(
            assetEntity.getId(),
            savedSchema.getId(),
            postAssetSchema.getSchemaClassificationStatus());
      }
    }
  }

  @Transactional
  public AssetSchema putSchema(PutAssetSchema putAssetSchema) {
    String assetFqdn = putAssetSchema.getAssetFqdn();
    AssetEntity assetEntity = validatorService.verifyAssetExists(assetFqdn);

    SchemaStructure schemaStructure = putAssetSchema.getSchemaJson();
    Map<String, Object> schemaMapInRequest = objectMapperUtils.convertSchemaToMap(schemaStructure);
    String hashOfSchemaInRequest = generateCanonicalHash(schemaMapInRequest);
    SchemaEntity savedSchema;

    if (putAssetSchema.getVersion() == null) {
      savedSchema = putSchemaWithoutVersion(assetEntity, hashOfSchemaInRequest, schemaMapInRequest);
    } else {
      int versionInRequest = putAssetSchema.getVersion();
      savedSchema =
          putSchemaWithVersion(
              versionInRequest, assetEntity, hashOfSchemaInRequest, schemaMapInRequest);
    }

    if (!CollectionUtils.isEmpty(putAssetSchema.getSchemaClassificationStatus())) {
      schemaClassificationService.putSchemaClassification(
          assetEntity.getId(), savedSchema.getId(), putAssetSchema.getSchemaClassificationStatus());
    }

    return schemaMapper.toDto(savedSchema, true);
  }

  private SchemaEntity putSchemaWithVersion(
      int versionInRequest,
      AssetEntity assetEntity,
      String hashOfSchemaInRequest,
      Map<String, Object> schemaMapInRequest) {
    SchemaEntity savedSchema;
    Optional<SchemaEntity> latestAssetSchemaOptional =
        schemaRepository.findByAssetIdWithMaxVersionId(assetEntity.getId());

    if (latestAssetSchemaOptional.isPresent()) {
      // evolve schema only if:
      // 1. version in db is less than version in request.
      // 2. There is an actual change in the schema (compare with their hashes)
      savedSchema = latestAssetSchemaOptional.get();
      if (!hashOfSchemaInRequest.equals(savedSchema.getSchemaHash())) {
        if (versionInRequest > savedSchema.getVersionId()) {
          SchemaEntity schemaEntity =
              SchemaEntity.builder()
                  .assetId(assetEntity.getId())
                  .versionId(versionInRequest)
                  .schemaJson(schemaMapInRequest)
                  .previousSchemaId(savedSchema.getId())
                  .build();
          savedSchema = persistSchema(schemaEntity);
          log.info(
              "New schema for asset {} was created with id: {}, version: {}",
              assetEntity.getId(),
              savedSchema.getId(),
              savedSchema.getVersionId());
          schemaClassificationService.updateSchemaClassificationNotificationIfApplicable(
              savedSchema.getId());
        } else if (versionInRequest == savedSchema.getVersionId()) {
          throw new MissingSchemaVersionException(
              String.format(
                  "Version %s conflicts with the existing schema. Specify a higher schema version if schema evolution is required",
                  savedSchema.getVersionId()));
        } else {
          throw new MissingSchemaVersionException(
              String.format(
                  "Newer schema version %s exists in database", savedSchema.getVersionId()));
        }
      }
    } else {
      savedSchema = registerTheFirstAssetSchema(assetEntity, schemaMapInRequest);
    }
    return savedSchema;
  }

  private SchemaEntity putSchemaWithoutVersion(
      AssetEntity assetEntity,
      String hashOfSchemaInRequest,
      Map<String, Object> schemaMapInRequest) {
    SchemaEntity savedSchema;
    Optional<SchemaEntity> existingSchemaEntityOptional =
        schemaRepository.findByAssetIdWithMaxVersionId(assetEntity.getId());
    if (existingSchemaEntityOptional.isPresent()) {
      savedSchema = existingSchemaEntityOptional.get();
      if (!hashOfSchemaInRequest.equals(savedSchema.getSchemaHash())) {
        throw new MissingSchemaVersionException(
            "Current schema hash mismatch. Specify a the exact schema version if schema evolution is required.");
      }
    } else {
      savedSchema = registerTheFirstAssetSchema(assetEntity, schemaMapInRequest);
      log.info("Schema was registered for asset {} by the PUT call", assetEntity.getFqdn());
    }
    return savedSchema;
  }

  private SchemaEntity registerTheFirstAssetSchema(
      AssetEntity assetEntity, Map<String, Object> schemaMapInRequest) {
    SchemaEntity schemaEntity =
        SchemaEntity.builder()
            .assetId(assetEntity.getId())
            .versionId(1)
            .schemaJson(schemaMapInRequest)
            .build();
    return persistSchema(schemaEntity);
  }

  public SchemaEntity findByAssetIdWithMaxVersionId(long assetId) {
    return schemaRepository
        .findByAssetIdWithMaxVersionId(assetId)
        .orElseThrow(() -> new SchemaNotFoundException("Schema not found for assetId: " + assetId));
  }

  public void fieldNameExists(SchemaEntity schemaEntity, String fieldName) {
    SchemaStructure schemaStructure =
        objectMapperUtils.convertMapToSchema(schemaEntity.getSchemaJson());
    boolean exists =
        schemaStructure.getFields().stream().anyMatch(f -> f.getName().equals(fieldName));
    if (!exists) {
      throw new FieldNotFoundException(schemaEntity.getId(), fieldName);
    }
  }

  private SchemaEntity persistSchema(SchemaEntity schemaEntity) {
    return schemaRepository.save(schemaEntity);
  }
}
