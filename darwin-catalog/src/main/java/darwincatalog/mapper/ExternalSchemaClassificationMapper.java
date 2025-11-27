package darwincatalog.mapper;

import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.exception.SchemaNotFoundException;
import darwincatalog.repository.SchemaClassificationStatusRepository;
import darwincatalog.repository.SchemaRepository;
import darwincatalog.util.ObjectMapperUtils;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CommonInternalClassificationStatusReadInner;
import org.openapitools.model.ExternalAssetSchemaClassification;
import org.openapitools.model.SchemaStructure;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalSchemaClassificationMapper {

  private final SchemaClassificationStatusRepository schemaClassificationStatusRepository;
  private final SchemaRepository schemaRepository;
  private final ObjectMapperUtils objectMapperUtils;
  private final SchemaMapper schemaMapper;

  public ExternalSchemaClassificationMapper(
      SchemaClassificationStatusRepository schemaClassificationStatusRepository,
      SchemaRepository schemaRepository,
      ObjectMapperUtils objectMapperUtils,
      SchemaMapper schemaMapper) {
    this.schemaClassificationStatusRepository = schemaClassificationStatusRepository;
    this.schemaRepository = schemaRepository;
    this.objectMapperUtils = objectMapperUtils;
    this.schemaMapper = schemaMapper;
  }

  public ExternalAssetSchemaClassification toDto(SchemaClassificationEntity entity) {
    List<SchemaClassificationStatusEntity> statusEntities =
        schemaClassificationStatusRepository.findBySchemaClassificationId(entity.getId());

    List<CommonInternalClassificationStatusReadInner> classificationStatuses =
        statusEntities.stream()
            .map(this::mapClassificationStatus)
            .collect(java.util.stream.Collectors.toList());

    SchemaStructure classifiedSchema =
        objectMapperUtils.convertMapToSchema(entity.getClassifiedSchemaJson());

    Map<String, Object> schemaStructureMap =
        schemaRepository
            .findById(entity.getSchemaId())
            .orElseThrow(
                () ->
                    new SchemaNotFoundException(
                        String.format("schema with id %s not found", entity.getSchemaId())))
            .getSchemaJson();

    SchemaStructure originalSchema = objectMapperUtils.convertMapToSchema(schemaStructureMap);

    return new ExternalAssetSchemaClassification()
        .schemaClassificationId(entity.getId())
        .originalSchemaJson(originalSchema)
        .classifiedSchemaJson(classifiedSchema)
        .schemaClassificationStatus(classificationStatuses);
  }

  private CommonInternalClassificationStatusReadInner mapClassificationStatus(
      SchemaClassificationStatusEntity statusEntity) {
    return schemaMapper.getClassificationStatusReadDto(statusEntity);
  }
}
