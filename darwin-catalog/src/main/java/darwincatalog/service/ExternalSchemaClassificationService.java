package darwincatalog.service;

import darwincatalog.entity.SchemaClassificationEntity;
import darwincatalog.exception.SchemaClassificationNotFound;
import darwincatalog.mapper.ExternalSchemaClassificationMapper;
import darwincatalog.repository.SchemaClassificationRepository;
import darwincatalog.util.ObjectMapperUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openapitools.model.ExternalAssetSchemaClassification;
import org.openapitools.model.PaginatedSchemas;
import org.openapitools.model.PutExternalSchemaClassification;
import org.openapitools.model.PutExternalSchemaClassificationReview;
import org.openapitools.model.SchemaClassificationCategory;
import org.openapitools.model.SchemaClassificationMethod;
import org.openapitools.model.SchemaClassificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExternalSchemaClassificationService {
  private final SchemaClassificationRepository schemaClassificationRepository;
  private final ObjectMapperUtils objectMapperUtils;
  private final ExternalSchemaClassificationStatusService externalSchemaClassificationStatusService;
  private final ExternalSchemaClassificationMapper externalSchemaClassificationMapper;

  public ExternalSchemaClassificationService(
      SchemaClassificationRepository schemaClassificationRepository,
      ObjectMapperUtils objectMapperUtils,
      ExternalSchemaClassificationStatusService externalSchemaClassificationStatusService,
      ExternalSchemaClassificationMapper externalSchemaClassificationMapper) {
    this.schemaClassificationRepository = schemaClassificationRepository;
    this.objectMapperUtils = objectMapperUtils;
    this.externalSchemaClassificationStatusService = externalSchemaClassificationStatusService;
    this.externalSchemaClassificationMapper = externalSchemaClassificationMapper;
  }

  public PaginatedSchemas getSchemas(
      Integer offset,
      Integer pageSize,
      String category1,
      String status1,
      String method1,
      Long schemaClassificationId) {
    Pageable pageable = PageRequest.of(offset / pageSize, pageSize);
    String category =
        category1 == null ? null : SchemaClassificationCategory.fromValue(category1).toString();
    String status =
        status1 == null ? null : SchemaClassificationStatus.fromValue(status1).toString();
    String method =
        method1 == null ? null : SchemaClassificationMethod.fromValue(method1).toString();

    Page<SchemaClassificationEntity> schemasPage =
        schemaClassificationRepository.findSchemasWithClassificationFilters(
            category, status, method, schemaClassificationId, pageable);

    List<ExternalAssetSchemaClassification> schemaClassifications =
        schemasPage.getContent().stream()
            .map(externalSchemaClassificationMapper::toDto)
            .collect(Collectors.toList());

    return new PaginatedSchemas()
        .total(schemasPage.getTotalElements())
        .offset(offset)
        .pageSize(pageSize)
        .data(schemaClassifications);
  }

  @Transactional
  public ExternalAssetSchemaClassification patchSchemaClassification(
      PutExternalSchemaClassification putExternalSchemaClassification, String client) {
    Optional<SchemaClassificationEntity> entityOpt =
        schemaClassificationRepository.findById(
            putExternalSchemaClassification.getSchemaClassificationId());

    if (entityOpt.isEmpty()) {
      throw new SchemaClassificationNotFound(
          "Schema classification not found with ID: "
              + putExternalSchemaClassification.getSchemaClassificationId());
    }

    SchemaClassificationEntity schemaClassificationEntity = entityOpt.get();

    Map<String, Object> schemaJson =
        objectMapperUtils.convertSchemaToMap(
            putExternalSchemaClassification.getClassifiedSchemaJson());
    schemaClassificationEntity.setClassifiedSchemaJson(schemaJson);
    schemaClassificationRepository.save(schemaClassificationEntity);

    externalSchemaClassificationStatusService.putClassificationStatus(
        schemaClassificationEntity.getId(),
        putExternalSchemaClassification.getSchemaClassificationStatus(),
        client);

    return externalSchemaClassificationMapper.toDto(schemaClassificationEntity);
  }

  public ExternalAssetSchemaClassification patchSchemaClassificationReview(
      PutExternalSchemaClassificationReview putExternalSchemaClassificationReview, String client) {
    Optional<SchemaClassificationEntity> entityOpt =
        schemaClassificationRepository.findById(
            putExternalSchemaClassificationReview.getSchemaClassificationId());

    if (entityOpt.isEmpty()) {
      throw new IllegalArgumentException(
          "Schema classification not found with ID: "
              + putExternalSchemaClassificationReview.getSchemaClassificationId());
    }

    SchemaClassificationEntity schemaClassificationEntity = entityOpt.get();

    externalSchemaClassificationStatusService.putClassificationStatusReview(
        schemaClassificationEntity.getId(),
        putExternalSchemaClassificationReview.getSchemaClassificationStatus(),
        client);

    return externalSchemaClassificationMapper.toDto(schemaClassificationEntity);
  }
}
