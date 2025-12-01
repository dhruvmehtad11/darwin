package darwincatalog.service;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.exception.AssetNotFoundException;
import darwincatalog.exception.InvalidClassificationStatusTransition;
import darwincatalog.exception.SchemaClassificationStatusNotFound;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.SchemaClassificationRepository;
import darwincatalog.repository.SchemaClassificationStatusRepository;
import darwincatalog.resolver.SchemaNotifierResolver;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.ClassificationNotificationType;
import org.openapitools.model.PutExternalSchemaClassificationReviewStatus;
import org.openapitools.model.PutExternalSchemaClassificationStatus;
import org.openapitools.model.SchemaClassificationStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalSchemaClassificationStatusService {
  private final SchemaClassificationStatusRepository schemaClassificationStatusRepository;
  private final SchemaNotifierResolver schemaNotifierResolver;
  private final SchemaClassificationRepository schemaClassificationRepository;
  private final AssetRepository assetRepository;
  List<SchemaClassificationStatus> putClassificationStatusBlacklistStatuses =
      List.of(SchemaClassificationStatus.NOT_REQUIRED);

  public ExternalSchemaClassificationStatusService(
      SchemaClassificationStatusRepository schemaClassificationStatusRepository,
      SchemaNotifierResolver schemaNotifierResolver,
      SchemaClassificationRepository schemaClassificationRepository,
      AssetRepository assetRepository) {
    this.schemaClassificationStatusRepository = schemaClassificationStatusRepository;
    this.schemaNotifierResolver = schemaNotifierResolver;
    this.schemaClassificationRepository = schemaClassificationRepository;
    this.assetRepository = assetRepository;
  }

  public void putClassificationStatus(
      Long schemaClassificationId,
      List<PutExternalSchemaClassificationStatus> schemaClassificationStatuses,
      String client) {
    List<SchemaClassificationStatusEntity> existingStatuses =
        schemaClassificationStatusRepository.findBySchemaClassificationId(schemaClassificationId);

    List<SchemaClassificationStatusEntity> notificationRequiringStatuses = new ArrayList<>();
    for (PutExternalSchemaClassificationStatus statusUpdate : schemaClassificationStatuses) {
      SchemaClassificationStatusEntity schemaClassificationStatusEntity =
          existingStatuses.stream()
              .filter(s -> s.getSchemaClassificationCategory().equals(statusUpdate.getCategory()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new SchemaClassificationStatusNotFound(
                          String.format(
                              "classification status for schema id %s not found with category: %s",
                              schemaClassificationId, statusUpdate.getCategory())));
      SchemaClassificationStatusEntity statusEntity =
          putClassificationStatus(schemaClassificationStatusEntity, statusUpdate, client);
      if (statusEntity.getClassificationStatus() == SchemaClassificationStatus.CLASSIFIED
          && statusEntity.getNotificationStatus() == ClassificationNotificationType.NOT_INITIATED) {
        notificationRequiringStatuses.add(statusEntity);
      }
    }
    sendSchemaUpdateNotification(notificationRequiringStatuses);
  }

  private SchemaClassificationStatusEntity putClassificationStatus(
      SchemaClassificationStatusEntity statusEntity,
      PutExternalSchemaClassificationStatus statusUpdate,
      String classifiedBy) {
    if (putClassificationStatusBlacklistStatuses.contains(statusEntity.getClassificationStatus())) {
      throw new InvalidClassificationStatusTransition(
          String.format(
              "current state for %s is %s. This operation is not allowed",
              statusEntity.getSchemaClassificationCategory(),
              statusEntity.getClassificationStatus()));
    }
    statusEntity.setClassificationStatus(statusUpdate.getStatus());
    statusEntity.setClassificationMethod(statusUpdate.getMethod());
    statusEntity.setClassifiedBy(classifiedBy);
    statusEntity.setClassifiedAt(Instant.now());
    statusEntity.setClassificationNotes(statusUpdate.getNotes());
    if (statusEntity.getNotificationStatus() == null) {
      statusEntity.setNotificationStatus(ClassificationNotificationType.NOT_INITIATED);
    }
    return schemaClassificationStatusRepository.save(statusEntity);
  }

  public void putClassificationStatusReview(
      Long schemaClassificationId,
      List<PutExternalSchemaClassificationReviewStatus> classificationReviewStatuses,
      String reviewedBy) {
    List<SchemaClassificationStatusEntity> existingStatuses =
        schemaClassificationStatusRepository.findBySchemaClassificationId(schemaClassificationId);

    List<SchemaClassificationStatusEntity> notificationRequiringStatuses = new ArrayList<>();
    for (PutExternalSchemaClassificationReviewStatus statusUpdate : classificationReviewStatuses) {
      SchemaClassificationStatusEntity schemaClassificationStatusEntity =
          existingStatuses.stream()
              .filter(s -> s.getSchemaClassificationCategory().equals(statusUpdate.getCategory()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new SchemaClassificationStatusNotFound(
                          String.format(
                              "classification status for schema id %s not found with category: %s",
                              schemaClassificationId, statusUpdate.getCategory())));
      SchemaClassificationStatusEntity statusEntity =
          putClassificationStatusReview(schemaClassificationStatusEntity, statusUpdate, reviewedBy);
      if (statusEntity.getClassificationStatus() == SchemaClassificationStatus.CLASSIFIED
          && statusEntity.getNotificationStatus() == ClassificationNotificationType.NOT_INITIATED) {
        notificationRequiringStatuses.add(statusEntity);
      }
    }
    sendSchemaUpdateNotification(notificationRequiringStatuses);
  }

  private SchemaClassificationStatusEntity putClassificationStatusReview(
      SchemaClassificationStatusEntity statusEntity,
      PutExternalSchemaClassificationReviewStatus statusUpdate,
      String reviewedBy) {
    statusEntity.setClassificationStatus(statusUpdate.getStatus());
    statusEntity.setReviewedBy(reviewedBy);
    statusEntity.setReviewedAt(Instant.now());
    statusEntity.setReviewNotes(statusUpdate.getNotes());
    return schemaClassificationStatusRepository.save(statusEntity);
  }

  private void sendSchemaUpdateNotification(List<SchemaClassificationStatusEntity> statusEntities) {
    long schemaClassificationId = statusEntities.get(0).getSchemaClassificationId();
    SchemaClassificationEntity schemaClassificationEntity =
        schemaClassificationRepository
            .findById(schemaClassificationId)
            .orElseThrow(
                () ->
                    new SchemaClassificationStatusNotFound(
                        String.format(
                            "Schema classification with id %s not found", schemaClassificationId)));
    AssetEntity assetEntity =
        assetRepository
            .findById(schemaClassificationEntity.getAssetId())
            .orElseThrow(() -> new AssetNotFoundException(schemaClassificationEntity.getAssetId()));
    try {
      schemaNotifierResolver.notify(assetEntity, statusEntities);
      statusEntities.forEach(
          entity ->
              entity.setNotificationStatus(
                  ClassificationNotificationType.CLASSIFICATION_UPDATE_SENT));
      schemaClassificationStatusRepository.saveAll(statusEntities);
    } catch (Exception ex) {
      log.error(
          "Schema update notifier failed for {}: {}. Skipping ",
          assetEntity.getFqdn(),
          ex.getMessage());
    }
  }
}
