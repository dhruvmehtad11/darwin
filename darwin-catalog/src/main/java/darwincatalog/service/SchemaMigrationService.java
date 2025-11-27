package darwincatalog.service;

import static darwincatalog.util.Common.generateCanonicalHash;

import darwincatalog.entity.SchemaEntity;
import darwincatalog.exception.AssetException;
import darwincatalog.repository.SchemaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.Field;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SchemaMigrationService {

  private final SchemaRepository schemaRepository;

  public SchemaMigrationService(SchemaRepository schemaRepository) {
    this.schemaRepository = schemaRepository;
  }

  @Transactional
  public void createOrUpdateAssetSchema(Long assetId, List<Field> fields) {
    try {
      Map<String, Object> newSchemaJson = convertFieldsToSchemaStructure(fields);

      Optional<SchemaEntity> existingSchemaOpt =
          schemaRepository.findByAssetIdWithMaxVersionId(assetId);

      if (existingSchemaOpt.isPresent()) {
        SchemaEntity existingSchema = existingSchemaOpt.get();
        String newHash = generateCanonicalHash(newSchemaJson);
        if (!newHash.equals(existingSchema.getSchemaHash())) {
          existingSchema.setSchemaJson(newSchemaJson);
          schemaRepository.save(existingSchema);
          log.debug("Updated existing schema latest version for asset {}", assetId);
        } else {
          log.debug("Schema content unchanged for asset {}, returning existing schema", assetId);
        }
      } else {
        SchemaEntity newSchema =
            SchemaEntity.builder()
                .assetId(assetId)
                .versionId(1)
                .schemaJson(newSchemaJson)
                .previousSchemaId(null)
                .build();
        schemaRepository.save(newSchema);
        log.debug("Created initial schema version for asset {}", assetId);
      }
    } catch (Exception e) {
      log.error("Failed to create/update schema for asset {}", assetId, e);
      throw new AssetException(
          String.format("Failed to create/update asset schema for asset %s", assetId));
    }
  }

  private Map<String, Object> convertFieldsToSchemaStructure(List<Field> fields) {
    Map<String, Object> schemaStructure = new HashMap<>();
    schemaStructure.put("type", "record");
    schemaStructure.put("name", "schema");

    List<Map<String, Object>> schemaFields =
        fields.stream().map(this::convertFieldToSchemaField).collect(Collectors.toList());

    schemaStructure.put("fields", schemaFields);

    return schemaStructure;
  }

  private Map<String, Object> convertFieldToSchemaField(Field field) {
    Map<String, Object> schemaField = new HashMap<>();
    schemaField.put("name", field.getName());
    schemaField.put("type", field.getType() != null ? field.getType() : "string");
    return schemaField;
  }
}
