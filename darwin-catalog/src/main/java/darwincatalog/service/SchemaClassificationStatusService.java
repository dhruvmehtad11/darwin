package darwincatalog.service;

import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.repository.SchemaClassificationStatusRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.ClassificationNotificationType;
import org.openapitools.model.PostSchemaClassificationStatus;
import org.openapitools.model.SchemaClassificationStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchemaClassificationStatusService {

  private final SchemaClassificationStatusRepository schemaClassificationStatusRepository;

  public SchemaClassificationStatusService(
      SchemaClassificationStatusRepository schemaClassificationStatusRepository) {
    this.schemaClassificationStatusRepository = schemaClassificationStatusRepository;
  }

  public void postClassificationStatus(
      SchemaClassificationEntity savedClassificationEntity,
      PostSchemaClassificationStatus postSchemaClassificationStatus) {
    SchemaClassificationStatus schemaClassificationStatus =
        postSchemaClassificationStatus.getRequired()
            ? SchemaClassificationStatus.REQUIRED
            : SchemaClassificationStatus.NOT_REQUIRED;
    SchemaClassificationStatusEntity schemaClassificationStatusEntity =
        SchemaClassificationStatusEntity.builder()
            .schemaClassificationId(savedClassificationEntity.getId())
            .schemaClassificationCategory(postSchemaClassificationStatus.getCategory())
            .classificationStatus(schemaClassificationStatus)
            .build();
    schemaClassificationStatusRepository.save(schemaClassificationStatusEntity);
  }

  public void putClassificationStatus(
      Optional<SchemaClassificationStatusEntity> existingStatus,
      PostSchemaClassificationStatus postSchemaClassificationStatus,
      Long schemaClassificationId) {
    SchemaClassificationStatusEntity statusEntity;
    SchemaClassificationStatus schemaClassificationStatus =
        postSchemaClassificationStatus.getRequired()
            ? SchemaClassificationStatus.REQUIRED
            : SchemaClassificationStatus.NOT_REQUIRED;
    if (existingStatus.isPresent()) {
      statusEntity = existingStatus.get();
      statusEntity.setClassificationStatus(schemaClassificationStatus);
    } else {
      statusEntity =
          SchemaClassificationStatusEntity.builder()
              .schemaClassificationId(schemaClassificationId)
              .schemaClassificationCategory(postSchemaClassificationStatus.getCategory())
              .classificationStatus(schemaClassificationStatus)
              .build();
    }
    schemaClassificationStatusRepository.save(statusEntity);
  }

  public void updateNotificationStatus(SchemaClassificationEntity schemaClassificationEntity) {
    schemaClassificationStatusRepository
        .findBySchemaClassificationId(schemaClassificationEntity.getId())
        .forEach(
            schemaClassificationStatusEntity -> {
              if (schemaClassificationStatusEntity.getNotificationStatus()
                  == ClassificationNotificationType.CLASSIFICATION_UPDATE_SENT) {
                schemaClassificationStatusEntity.setNotificationStatus(
                    ClassificationNotificationType.CLASSIFICATION_UPDATE_ACCEPTED);
                schemaClassificationStatusRepository.save(schemaClassificationStatusEntity);
              }
            });
  }
}
