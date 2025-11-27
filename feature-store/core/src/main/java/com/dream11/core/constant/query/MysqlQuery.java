package com.dream11.core.constant.query;

import static com.dream11.core.constant.Constants.CASSANDRA_FEATURE_GROUP_METADATA_TABLE_NAME;

import java.util.List;
import java.util.Objects;

public class MysqlQuery {
  public static final String addCassandraEntityMetaDataQuery =
      "INSERT INTO cassandra_store_entity_metadata "
          + "(name, entity, owner, tags, description) "
          + "VALUES (?, ?, ?, ?, ?)";

  public static final String getCassandraEntityMetaDataQuery =
      "SELECT * FROM cassandra_store_entity_metadata WHERE name = ? ;";

  public static final String addCassandraFeatureGroupMetaDataQuery =
      "INSERT INTO cassandra_store_feature_group_metadata "
          + "(name, version, version_enabled, feature_group_type, feature_group, entity_name, owner, tags, description) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String updateCassandraFeatureGroupMetaDataQuery =
      "UPDATE cassandra_store_feature_group_metadata SET state = ? WHERE name = ? AND version = ?;";

  public static final String updateCassandraEntityMetaDataQuery =
      "UPDATE cassandra_store_entity_metadata SET state = ? WHERE name = ?;";

  public static final String getCassandraFeatureGroupMetaDataQuery =
      "SELECT * FROM cassandra_store_feature_group_metadata WHERE name = ? AND version = ?;";

  public static final String getAllFeaturesForAnEntityQuery =
      "SELECT * FROM cassandra_store_feature_group_metadata WHERE entity_name = ? ;";

  public static final String getFeatureGroupLatestVersion =
      "SELECT * FROM cassandra_store_feature_group_version_map WHERE name = ? ;";

  public static final String getAllFeatureGroupLatestVersion =
      "SELECT * FROM cassandra_store_feature_group_version_map ;";

  public static final String updateFeatureGroupLatestVersion =
      "INSERT INTO cassandra_store_feature_group_version_map (name, latest_version) "
          + "VALUES (?, ?) "
          + "ON DUPLICATE KEY UPDATE "
          + "latest_version = ? ;";

  public static String addCassandraEntityFeatures =
      "INSERT INTO cassandra_store_entity_feature_map (entity_name, feature_column_name, feature_column_type, description, tags) VALUES (?, ?, ?, ?, ?);";
  public static String addCassandraFeatureGroupFeatures =
      "INSERT INTO cassandra_store_feature_group_feature_map (feature_group_name, feature_group_version, feature_column_name, feature_column_type, description, tags) VALUES (?, ?, ?, ?, ?, ?);";

  public static String getDistinctCassandraFeatureStoreOwnersQuery(String tableName) {
    StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT owner AS owner FROM ");
    stringBuilder.append(tableName);
    stringBuilder.append(" ;");
    return stringBuilder.toString();
  }

  public static String getDistinctCassandraFeatureStoreTagsQuery(String tableName) {
    StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT tag_name AS tag FROM ");
    stringBuilder.append(tableName);
    stringBuilder.append(" ;");
    return stringBuilder.toString();
  }

  public static final String insertEntityTagQuery =
      "INSERT INTO cassandra_store_entity_tag_map (tag_name, entity_name) VALUES (?, ?) ;";

  public static final String insertFeatureGroupTagQuery =
      "INSERT INTO cassandra_store_feature_group_tag_map (tag_name, feature_group_name, feature_group_version) VALUES (?, ?, ?) ;";

  public static String getSearchCassandraEntityByTagsQuery(List<String> tags) {
    StringBuilder queryBuilder =
        new StringBuilder("SELECT entity_name FROM cassandra_store_entity_tag_map WHERE ");
    for (int i = 0; i < tags.size(); i++) {
      queryBuilder.append("tag_name = ?");
      if (i < tags.size() - 1) {
        queryBuilder.append(" AND ");
      }
    }
    return queryBuilder.toString();
  }

  public static String getSearchCassandraEntityByOwnersQuery(List<String> owners) {
    StringBuilder queryBuilder =
        new StringBuilder("SELECT * FROM cassandra_store_entity_metadata WHERE ");
    for (int i = 0; i < owners.size(); i++) {
      queryBuilder.append("owner = ?");
      if (i < owners.size() - 1) {
        queryBuilder.append(" AND ");
      }
    }
    return queryBuilder.toString();
  }

  public static String searchCassandraEntityUsingPattern =
      "SELECT * FROM cassandra_store_entity_metadata WHERE name LIKE ? OR JSON_EXTRACT(entity, '$.features[*].name') LIKE ? ;";

  public static String searchAllInCassandraMetadata(
      String tableName, List<String> owners, List<String> tags, String pattern) {
    StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");

    queryBuilder.append(tableName);
    queryBuilder.append(" WHERE ");

    if (owners != null && !owners.isEmpty()) {
      for (int i = 0; i < owners.size(); i++) {
        queryBuilder.append("owner = ?");
        if (i < owners.size() - 1
            || (tags != null && !tags.isEmpty())
            || (pattern != null && !pattern.equals(""))) {
          queryBuilder.append(" AND ");
        }
      }
    }

    if (tags != null && !tags.isEmpty()) {
      if (owners != null && !owners.isEmpty()) {
        queryBuilder.append("(");
      }
      for (int i = 0; i < tags.size(); i++) {
        queryBuilder.append("JSON_CONTAINS(tags, JSON_QUOTE(?))");
        if (i < tags.size() - 1) {
          queryBuilder.append(" AND ");
        }
      }
      if (owners != null && !owners.isEmpty()) {
        queryBuilder.append(")");
      }
      if (pattern != null && !pattern.equals("")) {
        queryBuilder.append(" AND ");
      }
    }

    if (pattern != null && !pattern.equals("")) {
      if (Objects.equals(tableName, CASSANDRA_FEATURE_GROUP_METADATA_TABLE_NAME)) {
        queryBuilder.append(
            "(name LIKE ? OR JSON_EXTRACT(feature_group, '$.features[*].name') LIKE ?)");
      } else {
        queryBuilder.append("(name LIKE ? OR JSON_EXTRACT(entity, '$.features[*].name') LIKE ?)");
      }
    }

    queryBuilder.append(" LIMIT ? OFFSET ? ;");
    return queryBuilder.toString();
  }

  public static String getSearchCassandraFeatureGroupsByTagsQuery(List<String> tags) {
    StringBuilder queryBuilder =
        new StringBuilder(
            "SELECT feature_group_name, feature_group_version FROM cassandra_store_feature_group_tag_map WHERE ");
    for (int i = 0; i < tags.size(); i++) {
      queryBuilder.append("tag_name = ?");
      if (i < tags.size() - 1) {
        queryBuilder.append(" AND ");
      }
    }
    return queryBuilder.toString();
  }

  public static String getSearchCassandraFeatureGroupsByOwnersQuery(List<String> owners) {
    StringBuilder queryBuilder =
        new StringBuilder("SELECT * FROM cassandra_store_feature_group_metadata WHERE ");
    for (int i = 0; i < owners.size(); i++) {
      queryBuilder.append("owner = ?");
      if (i < owners.size() - 1) {
        queryBuilder.append(" AND ");
      }
    }
    return queryBuilder.toString();
  }

  public static String searchCassandraFeatureGroupUsingPattern =
      "SELECT * FROM cassandra_store_feature_group_metadata WHERE name LIKE ? OR JSON_EXTRACT(feature_group, '$.features[*].name') LIKE ? ;";

  public static String getAllCassandraEntities = "SELECT * FROM cassandra_store_entity_metadata;";
  public static String getAllCassandraFeatureGroups =
      "SELECT * FROM cassandra_store_feature_group_metadata;";

  public static String getAllCassandraFeatureGroupVersions =
      "SELECT * FROM cassandra_store_feature_group_metadata WHERE name = ?; ";

  public static String getUpdatedCassandraEntities =
      "SELECT * FROM cassandra_store_entity_metadata where updated_at >= FROM_UNIXTIME(?) ;";
  public static String getUpdatedCassandraFeatureGroups =
      "SELECT * FROM cassandra_store_feature_group_metadata where updated_at >= FROM_UNIXTIME(?) ;";

  public static String getUpdatedCassandraFeatureGroupVersions =
      "SELECT * FROM cassandra_store_feature_group_version_map where updated_at >= FROM_UNIXTIME(?) ;";

  public static String getCassandraStoreTenantConsumerDetails =
      "SELECT * FROM cassandra_store_tenant_consumer_map WHERE tenant_name = ? ;";

  public static String getAllCassandraStoreTenantConsumerDetails =
      "SELECT * FROM cassandra_store_tenant_consumer_map;";

  public static String insertIntoCassandraStoreTenantConsumerDetails =
      "INSERT INTO cassandra_store_tenant_consumer_map "
          + "(tenant_name, topic_name, num_partitions, num_consumers) "
          + "VALUES (?, ?, ?, ?)";

  public static String selectCassandraStoreTenantConsumerDetailsForUpdate =
      "SELECT * FROM cassandra_store_tenant_consumer_map WHERE tenant_name = ? FOR UPDATE; ";

  public static String updateCassandraStoreTenantNumConsumers =
      "UPDATE cassandra_store_tenant_consumer_map SET num_consumers = ? WHERE tenant_name = ?; ";

  public static String updateCassandraStoreTenantConsumerTopic =
      "UPDATE cassandra_store_tenant_consumer_map SET topic_name = ? WHERE tenant_name = ?; ";

  public static String updateCassandraStoreTenantNumPartitions =
      "UPDATE cassandra_store_tenant_consumer_map SET num_partitions = ? WHERE tenant_name = ?; ";

  public static String updateFgTenantQuery =
      "UPDATE cassandra_store_feature_group_metadata SET tenant_config = ? WHERE name = ?; ";

  public static final String updateEntityTtlQuery =
      "UPDATE cassandra_store_entity_metadata SET entity = ? WHERE name = ?;";

  public static final String addFeatureGroupRunQuery =
      "INSERT INTO cassandra_store_feature_group_run_data (name, version, run_id, time_taken, row_count, sample_data, error_message, status)"+
          " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
  public static final String getFeatureGroupRunsQuery =
      "SELECT * FROM cassandra_store_feature_group_run_data c WHERE (c.name, c.version, c.run_id) IN ( SELECT name, version, run_id FROM "
          + "cassandra_store_feature_group_run_data WHERE name = ? AND version = ? ORDER BY created_at DESC)   LIMIT 5 ";


  public static String insertIntoCassandraStoreTenantPopulatorDetails =
      "INSERT INTO cassandra_store_tenant_populator_map "
          + "(tenant_name, num_workers) "
          + "VALUES (?, ?) ON DUPLICATE KEY UPDATE num_workers = ?";

  public static String getAllCassandraStoreTenantPopulatorDetails =
      "SELECT * FROM cassandra_store_tenant_populator_map;";
}
