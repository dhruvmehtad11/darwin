package darwincatalog.service;

import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.repository.SchemaClassificationRepository;
import darwincatalog.repository.SchemaClassificationStatusRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.PostSchemaClassificationStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaClassificationService {

  private final SchemaClassificationRepository schemaClassificationRepository;
  private final SchemaClassificationStatusRepository schemaClassificationStatusRepository;
  private final SchemaClassificationStatusService schemaClassificationStatusService;

  public SchemaClassificationService(
      SchemaClassificationRepository schemaClassificationRepository,
      SchemaClassificationStatusRepository schemaClassificationStatusRepository,
      SchemaClassificationStatusService schemaClassificationStatusService) {
    this.schemaClassificationRepository = schemaClassificationRepository;
    this.schemaClassificationStatusRepository = schemaClassificationStatusRepository;
    this.schemaClassificationStatusService = schemaClassificationStatusService;
  }

  public void postSchemaClassification(
      long assetId,
      long schemaId,
      List<PostSchemaClassificationStatus> postSchemaClassificationStatuses) {
    SchemaClassificationEntity schemaClassificationEntity =
        SchemaClassificationEntity.builder().assetId(assetId).schemaId(schemaId).build();
    SchemaClassificationEntity savedClassificationEntity =
        schemaClassificationRepository.save(schemaClassificationEntity);
    for (PostSchemaClassificationStatus postSchemaClassificationStatus :
        postSchemaClassificationStatuses) {
      schemaClassificationStatusService.postClassificationStatus(
          savedClassificationEntity, postSchemaClassificationStatus);
    }
  }

  public void putSchemaClassification(
      long assetId,
      long schemaId,
      List<PostSchemaClassificationStatus> postSchemaClassificationStatuses) {
    Optional<SchemaClassificationEntity> schemaClassificationEntityOptional =
        schemaClassificationRepository.findBySchemaId(schemaId);
    SchemaClassificationEntity savedClassificationEntity;
    if (schemaClassificationEntityOptional.isPresent()) {
      savedClassificationEntity = schemaClassificationEntityOptional.get();
    } else {
      SchemaClassificationEntity schemaClassificationEntity =
          SchemaClassificationEntity.builder().assetId(assetId).schemaId(schemaId).build();
      savedClassificationEntity = schemaClassificationRepository.save(schemaClassificationEntity);
    }

    List<SchemaClassificationStatusEntity> existingStatuses =
        schemaClassificationStatusRepository.findBySchemaClassificationId(
            savedClassificationEntity.getId());
    for (PostSchemaClassificationStatus postSchemaClassificationStatus :
        postSchemaClassificationStatuses) {
      Optional<SchemaClassificationStatusEntity> existingStatus =
          existingStatuses.stream()
              .filter(
                  s ->
                      s.getSchemaClassificationCategory()
                          .equals(postSchemaClassificationStatus.getCategory()))
              .findFirst();
      schemaClassificationStatusService.putClassificationStatus(
          existingStatus, postSchemaClassificationStatus, savedClassificationEntity.getId());
    }
  }

  public void updateSchemaClassificationNotificationIfApplicable(long schemaId) {
    schemaClassificationRepository
        .findBySchemaId(schemaId)
        .ifPresent(schemaClassificationStatusService::updateNotificationStatus);
  }
}
