package darwincatalog.service.notifier;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.entity.SchemaEntity;
import darwincatalog.exception.SchemaNotFoundException;
import darwincatalog.mapper.SchemaMapper;
import darwincatalog.model.NexusSchemaNotification;
import darwincatalog.model.SchemaUpdateNotification;
import darwincatalog.repository.SchemaClassificationRepository;
import darwincatalog.repository.SchemaRepository;
import darwincatalog.util.ObjectMapperUtils;
import java.util.List;
import org.openapitools.model.AssetSchema;
import org.openapitools.model.AssetType;
import org.openapitools.model.CommonInternalClassificationStatusReadInner;
import org.openapitools.model.SchemaStructure;
import org.springframework.stereotype.Component;

@Component
public class NexusSchemaUpdateNotifier implements SchemaUpdateNotifier {
  private final SchemaRepository schemaRepository;
  private final ObjectMapperUtils objectMapperUtils;
  private final SchemaMapper schemaMapper;
  private final SchemaClassificationRepository schemaClassificationRepository;

  public NexusSchemaUpdateNotifier(
      SchemaRepository schemaRepository,
      ObjectMapperUtils objectMapperUtils,
      SchemaMapper schemaMapper,
      SchemaClassificationRepository schemaClassificationRepository) {
    this.schemaRepository = schemaRepository;
    this.objectMapperUtils = objectMapperUtils;
    this.schemaMapper = schemaMapper;
    this.schemaClassificationRepository = schemaClassificationRepository;
  }

  @Override
  public boolean isType(AssetEntity assetEntity) {
    return assetEntity.getType() == AssetType.STREAM;
  }

  @Override
  public void notify(
      AssetEntity assetEntity, List<SchemaClassificationStatusEntity> statusEntities) {
    NexusSchemaNotification notification = new NexusSchemaNotification();
    notification.setAssetFqdn(assetEntity.getFqdn());

    SchemaClassificationEntity schemaClassificationEntity =
        schemaClassificationRepository
            .findById(statusEntities.get(0).getSchemaClassificationId())
            .orElseThrow(
                () ->
                    new SchemaNotFoundException(
                        "Schema Classification Not Found for id "
                            + statusEntities.get(0).getSchemaClassificationId()));
    SchemaEntity schemaEntity =
        schemaRepository
            .findById(schemaClassificationEntity.getSchemaId())
            .orElseThrow(
                () ->
                    new SchemaNotFoundException(
                        String.format(
                            "schema with id %s not found",
                            schemaClassificationEntity.getSchemaId())));

    AssetSchema assetSchema = new AssetSchema();

    SchemaStructure schemaStructure =
        objectMapperUtils.convertMapToSchema(schemaClassificationEntity.getClassifiedSchemaJson());

    assetSchema.setVersion(schemaEntity.getVersionId());

    List<CommonInternalClassificationStatusReadInner> classificationStatuses =
        statusEntities.stream()
            .map(schemaMapper::getClassificationStatusReadDto)
            .collect(java.util.stream.Collectors.toList());

    SchemaUpdateNotification schemaUpdateNotification =
        SchemaUpdateNotification.builder()
            .version(schemaEntity.getVersionId())
            .schemaJson(schemaStructure)
            .schemaClassificationStatuses(classificationStatuses)
            .build();
    // todo send the schemaUpdateNotification to endpoint/queue when finalised

  }
}
