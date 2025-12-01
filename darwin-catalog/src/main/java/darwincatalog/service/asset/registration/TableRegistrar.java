package darwincatalog.service.asset.registration;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.config.FeatureFlag;
import darwincatalog.config.Properties;
import darwincatalog.exception.AssetException;
import darwincatalog.exception.AssetToRegisterNotFound;
import darwincatalog.exception.InvalidAttributeException;
import darwincatalog.model.Org;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AssetType;
import org.openapitools.model.TableType;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.EntityNotFoundException;
import software.amazon.awssdk.services.glue.model.GetTableRequest;

@Component
@Slf4j
public class TableRegistrar implements AssetRegistrar {
  private final FeatureFlag featureFlag;
  private final GlueClient glueClient;
  private final Properties properties;

  public TableRegistrar(FeatureFlag featureFlag, GlueClient glueClient, Properties properties) {
    this.featureFlag = featureFlag;
    this.glueClient = glueClient;
    this.properties = properties;
  }

  @Override
  public boolean isType(AssetType type) {
    return type == AssetType.TABLE;
  }

  @Override
  public void validateDetails(Map<String, String> detail) {
    String org = detail.get("org");
    String type = detail.get("type");
    String tableName = detail.get("table_name");
    String databaseName = detail.get("database_name");

    if (StringUtils.isBlank(org)) {
      throw new InvalidAttributeException("Table detail must include 'org'");
    }
    if (StringUtils.isBlank(type)) {
      throw new InvalidAttributeException("Table detail must include 'type' (REDSHIFT, LAKEHOUSE)");
    }
    if (StringUtils.isBlank(tableName)) {
      throw new InvalidAttributeException("Table detail must include 'table_name'");
    }
    if (StringUtils.isBlank(databaseName)) {
      throw new InvalidAttributeException("Table detail must include 'database_name'");
    }
    TableType tableType = TableType.fromValue(type);
    if (featureFlag.isAssetVerificationSourceEnabled()) {
      try {
        switch (tableType) {
          case REDSHIFT:
            log.info("Validating table details for 'REDSHIFT'");
            String catalogName = detail.get("catalog_name");
            if (StringUtils.isBlank(catalogName)) {
              throw new InvalidAttributeException("Redshift detail must include 'catalog_name'");
            }
            validateRedshiftTable(catalogName, databaseName, tableName);
            break;
          case LAKEHOUSE:
            log.info("Validating table details for 'LAKEHOUSE'");
            GetTableRequest getTableRequest =
                GetTableRequest.builder().name(tableName).databaseName(databaseName).build();
            glueClient.getTable(getTableRequest);
            break;
        }
      } catch (EntityNotFoundException | AssetToRegisterNotFound ex) {
        throw new AssetToRegisterNotFound(
            String.format(
                "Table details %s not found in source. Error: %s", detail, ex.getMessage()));
      }
    }
  }

  @Override
  public String generateFqdn(Map<String, String> detail) {
    StringBuilder fqdn = new StringBuilder();
    Org orgEnum = Org.fromValue(detail.get("org"));
    fqdn.append(orgEnum.getName()).append(HIERARCHY_SEPARATOR);
    fqdn.append("table").append(HIERARCHY_SEPARATOR);
    fqdn.append(detail.get("type").toLowerCase()).append(HIERARCHY_SEPARATOR);

    String catalogName = detail.get("catalog_name");
    if (StringUtils.isNotBlank(catalogName)) {
      fqdn.append(catalogName.toLowerCase()).append(HIERARCHY_SEPARATOR);
    }

    String databaseName = detail.get("database_name");
    if (StringUtils.isNotBlank(databaseName)) {
      fqdn.append(databaseName.toLowerCase()).append(HIERARCHY_SEPARATOR);
    }

    fqdn.append(detail.get("table_name"));
    return fqdn.toString();
  }

  private void validateRedshiftTable(String catalogName, String schemaName, String tableName) {
    if (StringUtils.isBlank(properties.getRedshiftJdbcUrl())) {
      log.warn(
          "Redshift JDBC URL not configured. Skipping table validation for: {}.{}.{}",
          catalogName,
          schemaName,
          tableName);
      return;
    }

    if (StringUtils.isBlank(properties.getRedshiftUsername())) {
      log.warn(
          "Redshift username not configured. Skipping table validation for: {}.{}.{}",
          catalogName,
          schemaName,
          tableName);
      return;
    }

    String sql =
        "SELECT COUNT(1) as table_count FROM information_schema.tables WHERE table_catalog = ? AND table_schema = ? AND table_name = ?";

    log.debug(
        "Executing Redshift validation query for table: {}.{}.{}",
        catalogName,
        schemaName,
        tableName);

    try (Connection conn =
            DriverManager.getConnection(
                properties.getRedshiftJdbcUrl(),
                properties.getRedshiftUsername(),
                properties.getRedshiftPassword());
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, catalogName);
      stmt.setString(2, schemaName);
      stmt.setString(3, tableName);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt("table_count");
          if (count < 1) {
            throw new AssetToRegisterNotFound(
                String.format(
                    "Table '%s.%s.%s' does not exist in Redshift",
                    catalogName, schemaName, tableName));
          }
        }
        log.info(
            "Successfully validated Redshift table existence: {}.{}.{}",
            catalogName,
            schemaName,
            tableName);
      }

    } catch (SQLException e) {
      log.error(
          "Failed to validate Redshift table {}.{}.{}: {}",
          catalogName,
          schemaName,
          tableName,
          e.getMessage());
      throw new AssetException(String.format("Redshift table validation failed: %s", e));
    }
  }
}
