package com.dream11.core.constant.query;

import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.util.FeatureNameUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class CassandraQuery {

  public static final String CASSANDRA_HEALTH_CHECK =
      "SELECT peer, data_center, host_id FROM system.peers";

  public static final String GET_CASSANDRA_TABLE_COLUMNS_QUERY =
      "SELECT column_name\n"
          + "FROM system_schema.columns\n"
          + "WHERE keyspace_name = ? AND table_name = ?;\n";

  public static String getCreateKeyspaceQuery() {
    if (Objects.equals(System.getProperty("app.environment"), "test")
        || Objects.equals(System.getProperty("app.environment"), "local"))
      return "CREATE KEYSPACE IF NOT EXISTS ofs WITH replication = { 'class' : 'org.apache.cassandra.locator.SimpleStrategy'};";

    return "CREATE KEYSPACE IF NOT EXISTS ofs WITH replication = { 'class' : 'org.apache.cassandra.locator.NetworkTopologyStrategy', "
        + "'us-east-1': '3' };";
  }

  public static final String CASSANDRA_UPDATE_TTL_QUERY =
      "ALTER TABLE %s.%s WITH default_time_to_live = %s;";

  public static String getCreateTableQuery(
      String keyspace,
      String tableName,
      List<CassandraFeatureColumn> featureColumnList,
      List<String> primaryKeys,
      Long ttl) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("CREATE TABLE ")
        .append(keyspace)
        .append('.')
        .append(tableName)
        .append(" (");
    for (CassandraFeatureColumn featureColumn : featureColumnList) {
      stringBuilder
          .append(featureColumn.getFeatureName())
          .append(" ")
          .append(featureColumn.getFeatureDataType().toString())
          .append(',');
    }

    stringBuilder.append("PRIMARY KEY (");
    for (String key : primaryKeys) {
      stringBuilder.append(key).append(", ");
    }
    stringBuilder.setLength(stringBuilder.length() - 2); // Remove the trailing comma and space
    stringBuilder.append(")");

    if (ttl != null && ttl != 0) {
      stringBuilder.append(") WITH default_time_to_live = ").append(ttl);
    } else {
      stringBuilder.append(");");
    }

    return stringBuilder.toString();
  }

  public static String getMigrateTableQuery(
      String keyspace,
      String tableName,
      List<CassandraFeatureColumn> featureColumnList,
      List<String> primaryKeys,
      Long ttl) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("CREATE TABLE IF NOT EXISTS ")
        .append(keyspace)
        .append('.')
        .append(tableName)
        .append(" (");
    for (CassandraFeatureColumn featureColumn : featureColumnList) {
      stringBuilder
          .append(featureColumn.getFeatureName())
          .append(" ")
          .append(featureColumn.getFeatureDataType().toString())
          .append(',');
    }

    stringBuilder.append("PRIMARY KEY (");
    for (String key : primaryKeys) {
      stringBuilder.append(key).append(", ");
    }
    stringBuilder.setLength(stringBuilder.length() - 2); // Remove the trailing comma and space
    stringBuilder.append(")");

    if (ttl != null && ttl != 0) {
      stringBuilder.append(") WITH default_time_to_live = ").append(ttl);
    } else {
      stringBuilder.append(");");
    }

    return stringBuilder.toString();
  }

  public static String getDeleteEntityQuery(String keyspace, String name) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("DROP TABLE ");
    stringBuilder.append(keyspace).append('.').append(name).append(';');
    return stringBuilder.toString();
  }

  public static String getAlterTableQuery(
      String keyspace,
      String tableName,
      String featureGroupName,
      String version,
      List<CassandraFeatureColumn> featureColumnList,
      Boolean versionEnabled) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("ALTER TABLE ")
        .append(keyspace)
        .append('.')
        .append(tableName)
        .append(" ADD (");

    int size = featureColumnList.size();
    for (int i = 0; i < size; i++) {
      CassandraFeatureColumn featureColumn = featureColumnList.get(i);
      String featureColumnName =
          FeatureNameUtils.getFgColumnName(
              featureGroupName, version, featureColumn.getFeatureName(), versionEnabled);
      stringBuilder
          .append(featureColumnName)
          .append(" ")
          .append(featureColumn.getFeatureDataType().toString());

      if (i < size - 1) {
        stringBuilder.append(", ");
      }
    }
    stringBuilder.append(" );");
    return stringBuilder.toString();
  }

  public static String getMigrateFeatureGroupQuery(
      String keyspace,
      String tableName,
      String featureGroupName,
      String version,
      List<CassandraFeatureColumn> featureColumnList,
      Boolean versionEnabled,
      Set<String> currentColumns) {
    Set<String> requiredColumns = new HashSet<>();

    int size = featureColumnList.size();
    for (int i = 0; i < size; i++) {
      CassandraFeatureColumn featureColumn = featureColumnList.get(i);
      String featureColumnName =
          FeatureNameUtils.getFgColumnName(
              featureGroupName, version, featureColumn.getFeatureName(), versionEnabled);
      // required because scylla columns are case in sensitive
      requiredColumns.add(featureColumnName.toLowerCase());
    }

    requiredColumns.removeAll(currentColumns);

    if(requiredColumns.isEmpty())
      return "";

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("ALTER TABLE ")
        .append(keyspace)
        .append('.')
        .append(tableName)
        .append(" ADD (");

    for (int i = 0; i < size; i++) {
      CassandraFeatureColumn featureColumn = featureColumnList.get(i);
      String featureColumnName =
          FeatureNameUtils.getFgColumnName(
              featureGroupName, version, featureColumn.getFeatureName(), versionEnabled);
      if (requiredColumns.contains(featureColumnName.toLowerCase())) {
        stringBuilder
            .append(featureColumnName)
            .append(" ")
            .append(featureColumn.getFeatureDataType().toString());

        if (i < size - 1) {
          stringBuilder.append(", ");
        }
      }
    }
    stringBuilder.append(" );");

    return stringBuilder.toString();
  }

  public static String getRemoveColumnQuery(
      String keyspace,
      String tableName,
      String featureGroupName,
      String version,
      List<String> columnsToRemove,
      Boolean versionEnabled) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("ALTER TABLE ")
        .append(keyspace)
        .append('.')
        .append(tableName)
        .append(" DROP (");

    int size = columnsToRemove.size();
    for (int i = 0; i < size; i++) {
      String featureColumnName =
          FeatureNameUtils.getFgColumnName(
              featureGroupName, version, columnsToRemove.get(i), versionEnabled);
      stringBuilder.append(featureColumnName);

      if (i < size - 1) {
        stringBuilder.append(", ");
      }
    }
    stringBuilder.append(" );");
    return stringBuilder.toString();
  }

  public static String getInsertFeaturesQuery(
      String keyspace,
      String tableName,
      Set<String> entityCols,
      String featureGroupName,
      String version,
      Boolean versionEnabled,
      List<String> featureColumnList) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("INSERT INTO ")
        .append(keyspace)
        .append('.')
        .append(tableName)
        .append(" ( ");

    int size = featureColumnList.size();
    for (int i = 0; i < size; i++) {
      String featureColumnName = featureColumnList.get(i);
      if (!entityCols.contains(featureColumnName))
        featureColumnName =
            FeatureNameUtils.getFgColumnName(
                featureGroupName, version, featureColumnName, versionEnabled);

      stringBuilder.append(featureColumnName);

      if (i < size - 1) {
        stringBuilder.append(", ");
      }
    }
    stringBuilder.append(" )");

    stringBuilder.append(" VALUES ( ");
    for (int i = 0; i < featureColumnList.size(); i++) {
      stringBuilder.append("?");
      if (i < size - 1) {
        stringBuilder.append(", ");
      }
    }

    stringBuilder.append(" );");
    return stringBuilder.toString();
  }

  public static String getReadFeaturesQuery(
      String keyspace,
      String tableName,
      Set<String> entityCols,
      String featureGroupName,
      String version,
      Boolean versionEnabled,
      List<String> featureColumnList,
      List<String> primaryKeys) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("SELECT ");

    // Append feature columns with AS clause for aliasing
    int size = featureColumnList.size();
    for (int i = 0; i < size; i++) {
      String featureColumnName = featureColumnList.get(i);
      String alias = featureColumnName; // Default alias is the feature column name

      if (!entityCols.contains(featureColumnName)) {
        // If the column is not part of the entity columns, use alias
        featureColumnName =
            FeatureNameUtils.getFgColumnName(
                featureGroupName, version, featureColumnName, versionEnabled);
        alias = featureColumnList.get(i); // Alias is the original feature column name
      }

      stringBuilder.append(featureColumnName);

      // Append AS clause for aliasing
      stringBuilder.append(" AS ").append("\"").append(alias).append("\"");

      if (i < size - 1) {
        stringBuilder.append(", ");
      }
    }

    // Append FROM clause
    stringBuilder.append(" FROM ").append(keyspace).append('.').append(tableName);

    // Append WHERE clause with primary keys
    stringBuilder.append(" WHERE ");
    int pkSize = primaryKeys.size();
    for (int i = 0; i < pkSize; i++) {
      stringBuilder.append(primaryKeys.get(i)).append(" = ?");
      if (i < pkSize - 1) {
        stringBuilder.append(" AND ");
      }
    }

    return stringBuilder.toString();
  }
}
