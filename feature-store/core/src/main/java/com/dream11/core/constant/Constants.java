package com.dream11.core.constant;

import com.dream11.core.dto.helper.CassandraStatementPrimaryKeyMap;
import com.dream11.core.dto.helper.Data;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.VersionMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public final class Constants {
  public static final String RUNNING = "RUNNING";
  public static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();
  public static final List<JsonObject> EMPTY_JSON_OBJECT_LIST = new ArrayList<>();
  public static final List<String> EMPTY_TAG_ARRAY = new ArrayList<>();
  public static final String DEFAULT_VERSION = "v0";
  public static final VersionMetadata DEFAULT_VERSION_METADATA =
      VersionMetadata.builder().latestVersion(DEFAULT_VERSION).build();
  public static final String PREPARED_STATEMENT_CAFFEINE_CACHE_NAME = "PREPARED_STATEMENT_CACHE";
  public static final String FEATURE_GROUP_METADATA_CACHE = "FEATURE_GROUP_METADATA_CACHE";
  public static final String FEATURE_GROUP_VERSION_CACHE = "FEATURE_GROUP_VERSION_CACHE";
  public static final String ENTITY_METADATA_CACHE = "ENTITY_METADATA_CACHE";
  public static final String CONSUMER_GROUP_METADATA_CACHE = "CONSUMER_GROUP_METADATA_CACHE";
  public static final String CASSANDRA_CLIENT_FACTORY_CACHE = "CASSANDRA_CLIENT_FACTORY_CACHE";

  public static final String CASSANDRA_FEATURE_GROUP_METADATA_TABLE_NAME =
      "cassandra_store_feature_group_metadata";
  public static final String CASSANDRA_ENTITY_METADATA_TABLE_NAME =
      "cassandra_store_entity_metadata";
  public static final String CASSANDRA_FEATURE_GROUP_TAG_MAP_TABLE_NAME =
      "cassandra_store_feature_group_tag_map";
  public static final String CASSANDRA_ENTITY_TAG_MAP_TABLE_NAME = "cassandra_store_entity_tag_map";
  public static final CassandraFeatureGroupMetadata EMPTY_FEATURE_GROUP_METADATA =
      CassandraFeatureGroupMetadata.builder().build();

  public static String EMPTY_RESPONSE_ERROR_STRING = "EMPTY_FEATURE_VECTOR_ERROR";
  public static final String INVALID_REQUEST_ERROR_MESSAGE = "invalid request";
  public static final String DUPLICATE_COLUMNS_ERROR_MESSAGE = "duplicate columns in read request";

  public static final int SUCCESS_HTTP_STATUS_CODE = 200;
  public static final int NOT_FOUND_HTTP_STATUS_CODE = 404;
  public static final int ERROR_HTTP_STATUS_CODE = 500;
  public static final String NOT_FOUND_ERROR_MESSAGE = "not found";
  public static final String DATA_FETCH_SUCCESS_MESSAGE = "Data fetched successfully";
  public static final String WRITE_SUCCESSFUL_SUB_STRING = "Write successful in ";

  public static final String INVALID_DATA_TYPE_ERROR_MESSAGE = "Invalid column data type: ";
  public static final String OLD_SDK_OWNER = "old_fct@dream11.com";
  public static final String SUCCESS_TABLE_CREATION = "Successfully created the table.";
  public static final String SUCCESS_TABLE_ALTERATION = "Successfully Altered the table";
  public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

  public static final TypeReference<Data<String>> DATA_STRING_TYPE_REFERENCE =  new TypeReference<Data<String>>() {};

  public static final String JOB_HEALTH_CACHE_NAME = "JOB_HEALTH_CACHE_NAME";

  public static final String LOCAL_CONSUMER_CONFIG_CACHE_NAME = "LOCAL_CONSUMER_CONFIG_CACHE";
  public static final String GLOBAL_CONSUMER_CONFIG_CACHE_NAME = "GLOBAL_CONSUMER_CONFIG_CACHE";

  public static final String FEATURE_GROUP_TENANT_METADATA_CACHE = "FEATURE_GROUP_TENANT_METADATA_CACHE";
  public static final String DEFAULT_TENANT_NAME = "default-tenant";
  public static final CassandraStatementPrimaryKeyMap EMPTY_STATEMENT_PRIMARY_KEY_MAP = CassandraStatementPrimaryKeyMap.builder().build();
  public static final Integer DEFAULT_INIT_CONSUMERS = 1;

  public static final String SDK_VERSION_HEADER_NAME = "sdk-version";
  public static final String SDK_V1_HEADER_VALUE = "v1";
  public static final String SDK_V2_HEADER_VALUE = "v2";

  public static final String FEATURE_GROUP_HEADER_NAME = "feature-group-name";
  public static final String FEATURE_GROUP_VERSION_HEADER_NAME = "feature-group-version";
  public static final String FEATURE_GROUP_RUN_ID_HEADER_NAME = "run-id";
  public static final String WRITE_REPLICATION_HEADER_NAME = "replicated-writes";
  public enum FeatureStoreType {
    cassandra
  }

  public enum FeatureStoreTenantType {
    READER, WRITER, CONSUMER, ALL
  }
}
