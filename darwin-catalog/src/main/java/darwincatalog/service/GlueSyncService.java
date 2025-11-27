package darwincatalog.service;

import static darwincatalog.util.Common.getFqdn;
import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.annotation.Timed;
import darwincatalog.config.ConfigProvider;
import darwincatalog.entity.AssetDirectoryEntity;
import darwincatalog.entity.AssetEntity;
import darwincatalog.model.Org;
import darwincatalog.repository.AssetDirectoryRepository;
import darwincatalog.repository.AssetRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AssetType;
import org.openapitools.model.Field;
import org.slf4j.event.Level;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.GetTableRequest;
import software.amazon.awssdk.services.glue.model.GetTableResponse;
import software.amazon.awssdk.services.glue.model.GetTablesRequest;
import software.amazon.awssdk.services.glue.model.GetTablesResponse;
import software.amazon.awssdk.services.glue.model.Table;

@Service
@Slf4j
public class GlueSyncService implements DdlSyncService {

  public static final String REDSHIFT = "redshift";
  public static final String REDSHIFT_UNDERSCORE = "redshift_";
  public static final String STREAM_UNDERSCORE = "stream_";
  private final AssetRepository assetRepository;
  private final GlueClient glueClient;
  private final AssetService assetService;
  private final AssetDirectoryRepository assetDirectoryRepository;
  private final ConfigProvider configProvider;
  private final SchemaMigrationService schemaMigrationService;

  public GlueSyncService(
      AssetRepository assetRepository,
      GlueClient glueClient,
      AssetService assetService,
      AssetDirectoryRepository assetDirectoryRepository,
      ConfigProvider configProvider,
      SchemaMigrationService schemaMigrationService) {
    this.assetRepository = assetRepository;
    this.glueClient = glueClient;
    this.assetService = assetService;
    this.assetDirectoryRepository = assetDirectoryRepository;
    this.configProvider = configProvider;
    this.schemaMigrationService = schemaMigrationService;
  }

  @Timed
  public void initialDump(List<String> databaseNames) {
    List<String> parsedDatabaseWhitelist;
    if (CollectionUtils.isEmpty(databaseNames)) {
      parsedDatabaseWhitelist = configProvider.getGlueDatabaseWhitelist();
    } else {
      parsedDatabaseWhitelist = databaseNames;
    }
    for (String database : parsedDatabaseWhitelist) {
      try {
        fetchAndProcessTablesForDatabase(database);
      } catch (Exception ex) {
        log.error("failed to fetch tables for database: {}. Continuing for others", database, ex);
      }
      log.info("database {} processed!", database);
    }
  }

  @Timed(logLevel = Level.DEBUG)
  public void addTables(String databaseName, List<String> newTables) {
    if (!configProvider.getGlueDatabaseWhitelist().contains(databaseName)) {
      return;
    }
    newTables.stream()
        .map(
            e ->
                glueClient.getTable(
                    GetTableRequest.builder().databaseName(databaseName).name(e).build()))
        .map(GetTableResponse::table)
        .collect(java.util.stream.Collectors.toList())
        .forEach(e -> addTable(databaseName, e));
    log.debug("glue add event processed for tables {}", newTables);
  }

  @Timed(logLevel = Level.DEBUG)
  public void updateTable(String databaseName, String affectedTable) {
    if (!configProvider.getGlueDatabaseWhitelist().contains(databaseName)) {
      return;
    }
    Table table =
        glueClient
            .getTable(
                GetTableRequest.builder().databaseName(databaseName).name(affectedTable).build())
            .table();
    updateTable(databaseName, table);
  }

  @Timed(logLevel = Level.DEBUG)
  public void deleteTables(String databaseName, List<String> tablesToDelete) {
    tablesToDelete.forEach(
        e -> {
          List<AssetDirectoryEntity> assetDirectoryEntities =
              assetDirectoryRepository.findByAssetNameAndIsTerminal(e.toLowerCase(), true);
          assetDirectoryEntities.forEach(
              entity -> {
                String parsedPartialPrefix = parsePartialPrefix(databaseName);
                if (entity.getAssetPrefix().contains(parsedPartialPrefix)) {
                  String assetFqdn = entity.getAssetPrefix() + HIERARCHY_SEPARATOR + e;
                  assetService.deleteAsset(assetFqdn);
                  log.info("asset {} deleted!", assetFqdn);
                }
              });
        });
  }

  private static void logFailedFqdnConstruction(String databaseName, Table e) {
    log.error(
        "could not determine fqdn for table: {}, db: {}, parameters: {}",
        e.name(),
        databaseName,
        e.parameters());
  }

  private void fetchAndProcessTablesForDatabase(String dbName) {
    String nextToken = null;
    int tablesCount = 0;
    do {
      GetTablesResponse tablesResponse =
          glueClient.getTables(
              GetTablesRequest.builder().databaseName(dbName).nextToken(nextToken).build());
      for (Table table : tablesResponse.tableList()) {
        try {
          addTable(dbName, table);
        } catch (Exception e) {
          log.info(
              "failed to persist table {}.{}. Continuing for others",
              table.databaseName(),
              table.name(),
              e);
        }
        tablesCount++;
        if (tablesCount % 100 == 0) {
          log.info("processed {} tables for database {}", tablesCount, dbName);
        }
      }
      nextToken = tablesResponse.nextToken();
    } while (nextToken != null);
  }

  private void addTable(String databaseName, Table table) {
    Optional<String> fqdnOpt = formFqdn(table);
    if (fqdnOpt.isEmpty()) {
      logFailedFqdnConstruction(databaseName, table);
      return;
    }

    String fqdn = fqdnOpt.get();
    String type = fqdn.split(HIERARCHY_SEPARATOR)[1];
    List<Field> fieldEntities = buildFieldEntities(table);

    AssetEntity assetEntity =
        AssetEntity.builder()
            .fqdn(fqdn)
            .type(AssetType.fromValue(type))
            .assetCreatedAt(table.createTime())
            .assetUpdatedAt(table.updateTime())
            .build();

    getTableMetadata(table).ifPresent(assetEntity::setMetadata);

    try {
      AssetEntity savedAssetEntity = assetService.persistAsset(assetEntity);

      // Create or update asset schema instead of saving individual fields
      if (!fieldEntities.isEmpty()) {
        schemaMigrationService.createOrUpdateAssetSchema(savedAssetEntity.getId(), fieldEntities);
        log.debug("Created asset schema for new asset {}", fqdn);
      }
    } catch (DataIntegrityViolationException ex) {
      if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
        org.hibernate.exception.ConstraintViolationException hibernateEx =
            (org.hibernate.exception.ConstraintViolationException) ex.getCause();
        String sqlMessage = hibernateEx.getSQLException().getMessage();
        String constraintName = hibernateEx.getConstraintName();
        log.error(
            "Constraint violation on: {} -> {}. Will update existing entity",
            constraintName,
            sqlMessage);
        assetRepository.findByFqdn(fqdn).ifPresent(entity -> updateTable(table, entity));
      } else {
        // else let the exception propagate
        throw ex;
      }
      return;
    }

    log.debug("entity {} inserted!", fqdn);
  }

  private Optional<Map<String, Object>> getTableMetadata(Table table) {
    String subtype = null;
    Map<String, Object> metadata = new HashMap<>();
    if (table.storageDescriptor() != null
        && table.storageDescriptor().outputFormat() != null
        && table.storageDescriptor().outputFormat().contains("Parquet")) {
      subtype = "parquet";
    } else if ("iceberg".equalsIgnoreCase(table.parameters().get("table_type"))) {
      subtype = "iceberg";
    } else if (REDSHIFT.equalsIgnoreCase(table.parameters().get("classification"))
        || REDSHIFT.equalsIgnoreCase(table.parameters().get("table_type"))) {
      subtype = "redshift";
    }

    if (subtype != null) {
      metadata.put("type", subtype);
    }

    if (table.storageDescriptor() != null
        && StringUtils.isNotBlank(table.storageDescriptor().location())) {
      metadata.put("path", table.storageDescriptor().location());
    }

    return metadata.isEmpty() ? Optional.empty() : Optional.of(metadata);
  }

  private List<Field> buildFieldEntities(Table table) {
    if (table.storageDescriptor() == null || table.storageDescriptor().columns() == null) {
      return Collections.emptyList();
    }
    return table.storageDescriptor().columns().stream()
        .map(
            e -> {
              String type = e.type();
              if (type.length() > 255) {
                type = "varchar";
              }
              return new Field().name(e.name()).type(type);
            })
        .collect(java.util.stream.Collectors.toList());
  }

  private void updateTable(String databaseName, Table table) {
    Optional<String> fqdnOpt = formFqdn(table);
    if (fqdnOpt.isEmpty()) {
      logFailedFqdnConstruction(databaseName, table);
      return;
    }

    String fqdn = fqdnOpt.get();
    assetRepository
        .findByFqdn(fqdn)
        .ifPresentOrElse(entity -> updateTable(table, entity), () -> addTable(databaseName, table));
    log.debug("glue update event processed for {}", fqdn);
  }

  private void updateTable(Table table, AssetEntity entity) {
    List<Field> fieldEntities = buildFieldEntities(table);

    if (!fieldEntities.isEmpty()) {
      schemaMigrationService.createOrUpdateAssetSchema(entity.getId(), fieldEntities);
      log.debug("Updated asset schema for asset {}", entity.getFqdn());
    }

    Instant updatedAt = table.updateTime();
    Instant updatedAtInDb = entity.getAssetUpdatedAt();

    boolean assetChanged = false;
    if (updatedAt != null && !Objects.equals(updatedAt, updatedAtInDb)) {
      entity.setAssetUpdatedAt(updatedAt);
      assetChanged = true;
    }

    Optional<Map<String, Object>> metadataOptional = getTableMetadata(table);
    if (metadataOptional.isPresent()) {
      Map<String, Object> metadata = metadataOptional.get();
      if (!metadata.equals(entity.getMetadata())) {
        entity.setMetadata(metadata);
        assetChanged = true;
      }
    }

    if (assetChanged) {
      assetRepository.save(entity);
    }
  }

  private Optional<String> formFqdn(Table table) {
    Optional<String> result = Optional.empty();
    String org = getOrgName(table);
    AssetType type = null;
    String subtype;
    // Defensive: parameters may be null
    Map<String, String> parameters =
        table.parameters() != null ? table.parameters() : Collections.emptyMap();
    if (REDSHIFT.equalsIgnoreCase(parameters.get("classification"))
        || REDSHIFT.equalsIgnoreCase(parameters.get("table_type"))) {
      type = AssetType.TABLE;
      subtype = REDSHIFT;
      String fullDatabaseName = table.databaseName();
      String tableName = table.name();
      if (fullDatabaseName != null && fullDatabaseName.startsWith(REDSHIFT_UNDERSCORE)) {
        fullDatabaseName = fullDatabaseName.substring(REDSHIFT_UNDERSCORE.length());
        int firstUnderscoreIndex = fullDatabaseName.indexOf('_');
        if (firstUnderscoreIndex == -1) {
          result = Optional.of(getFqdn(org, type.toString(), subtype, fullDatabaseName, tableName));
        } else {
          String catalogName = fullDatabaseName.substring(0, firstUnderscoreIndex);
          String databaseName1 = fullDatabaseName.substring(firstUnderscoreIndex + 1);
          result =
              Optional.of(
                  getFqdn(org, type.toString(), subtype, catalogName, databaseName1, tableName));
        }
      }
    } else if (table.databaseName() != null && table.databaseName().startsWith(STREAM_UNDERSCORE)) {
      type = AssetType.KAFKA;
      String fullDatabaseName = table.databaseName();
      String topicName = table.name();
      String clusterName = fullDatabaseName.substring(STREAM_UNDERSCORE.length());
      result = Optional.of(getFqdn(org, type.toString(), clusterName, topicName));
    } else {
      type = AssetType.TABLE;
      String databaseName1 = table.databaseName();
      String tableName = table.name();
      subtype = "lakehouse";
      result = Optional.of(getFqdn(org, type.toString(), subtype, databaseName1, tableName));
    }
    return result;
  }

  private static String getOrgName(Table table) {
    return Org.EXAMPLE.getName();
  }

  private String parsePartialPrefix(String databaseName) {
    String result = databaseName;
    if (databaseName.startsWith(REDSHIFT_UNDERSCORE)) {
      String databaseName1 = databaseName.substring(REDSHIFT_UNDERSCORE.length());
      int firstUnderscoreIndex = databaseName1.indexOf('_');
      if (firstUnderscoreIndex == -1) {
        result = getFqdn("table", REDSHIFT, databaseName1);
      } else {
        String catalogName = databaseName1.substring(0, firstUnderscoreIndex);
        databaseName1 = databaseName1.substring(firstUnderscoreIndex + 1);
        result = getFqdn("table", REDSHIFT, catalogName, databaseName1);
      }
    } else if (databaseName.startsWith(STREAM_UNDERSCORE)) {
      String clusterName = databaseName.substring(STREAM_UNDERSCORE.length());
      result = getFqdn("kafka", clusterName);
    }
    return result;
  }
}
