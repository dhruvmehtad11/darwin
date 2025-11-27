package darwincatalog.mapper;

import darwincatalog.annotation.Timed;
import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.entity.SchemaEntity;
import darwincatalog.repository.SchemaClassificationRepository;
import darwincatalog.repository.SchemaClassificationStatusRepository;
import darwincatalog.util.ObjectMapperUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AssetSchema;
import org.openapitools.model.CommonInternalClassificationStatusReadInner;
import org.openapitools.model.SchemaStructure;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaMapper {

  private final SchemaClassificationStatusRepository schemaClassificationStatusRepository;
  private final SchemaClassificationRepository schemaClassificationRepository;
  private final ObjectMapperUtils objectMapperUtils;

  public SchemaMapper(
      SchemaClassificationStatusRepository schemaClassificationStatusRepository,
      SchemaClassificationRepository schemaClassificationRepository,
      ObjectMapperUtils objectMapperUtils) {
    this.schemaClassificationStatusRepository = schemaClassificationStatusRepository;
    this.schemaClassificationRepository = schemaClassificationRepository;
    this.objectMapperUtils = objectMapperUtils;
  }

  @Timed(logLevel = Level.DEBUG)
  public AssetSchema toDto(SchemaEntity entity, boolean includeClassificationInformation) {
    SchemaStructure schemaStructure = objectMapperUtils.convertMapToSchema(entity.getSchemaJson());

    AssetSchema assetSchema =
        new AssetSchema()
            .version(entity.getVersionId())
            .schemaJson(schemaStructure)
            .schemaHash(entity.getSchemaHash())
            .previousSchemaId(entity.getPreviousSchemaId());

    if (includeClassificationInformation) {
      List<CommonInternalClassificationStatusReadInner> classificationStatuses = new ArrayList<>();
      Optional<SchemaClassificationEntity> classificationEntityOptional =
          schemaClassificationRepository.findBySchemaId(entity.getId());
      if (classificationEntityOptional.isPresent()) {
        SchemaClassificationEntity schemaClassificationEntity = classificationEntityOptional.get();
        List<SchemaClassificationStatusEntity> statusEntities =
            schemaClassificationStatusRepository.findBySchemaClassificationId(
                schemaClassificationEntity.getId());
        classificationStatuses =
            statusEntities.stream()
                .map(this::mapClassificationStatus)
                .collect(java.util.stream.Collectors.toList());
      }
      assetSchema.setSchemaClassificationStatus(classificationStatuses);
    }

    return assetSchema;
  }

  private CommonInternalClassificationStatusReadInner mapClassificationStatus(
      SchemaClassificationStatusEntity statusEntity) {
    return getClassificationStatusReadDto(statusEntity);
  }

  public CommonInternalClassificationStatusReadInner getClassificationStatusReadDto(
      SchemaClassificationStatusEntity statusEntity) {
    String reviewedAt =
        statusEntity.getReviewedAt() == null ? null : statusEntity.getReviewedAt().toString();
    String classifiedAt =
        statusEntity.getClassifiedAt() == null ? null : statusEntity.getClassifiedAt().toString();
    return new CommonInternalClassificationStatusReadInner()
        .category(statusEntity.getSchemaClassificationCategory())
        .method(statusEntity.getClassificationMethod())
        .status(statusEntity.getClassificationStatus())
        .classifiedBy(statusEntity.getClassifiedBy())
        .classifiedAt(classifiedAt)
        .classificationNotes(statusEntity.getClassificationNotes())
        .reviewedBy(statusEntity.getReviewedBy())
        .reviewedAt(reviewedAt)
        .reviewNotes(statusEntity.getReviewNotes());
  }
}
