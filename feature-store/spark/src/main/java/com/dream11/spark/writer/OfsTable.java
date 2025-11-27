package com.dream11.spark.writer;

import com.dream11.spark.service.OfsAdminService;
import com.dream11.spark.utils.RunDataUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.spark.sql.connector.catalog.SupportsWrite;
import org.apache.spark.sql.connector.catalog.TableCapability;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.WriteBuilder;
import org.apache.spark.sql.types.StructType;

import static com.dream11.spark.constant.Constants.FEATURE_GROUP_NAME_KEY;
import static com.dream11.spark.constant.Constants.FEATURE_GROUP_VERSION_KEY;
import static com.dream11.spark.constant.Constants.RUN_ID_KEY;

public class OfsTable implements SupportsWrite {
  private final StructType schema;
  private final Map<String, String> properties;
  private final String featureGroupName;
  private final String tableName;
  private final String featureGroupVersion;
  private final String kafkaHost;
  private final String kafkaTopic;
  private final String runId;

  public OfsTable(StructType schema, Map<String, String> properties, OfsAdminService adminService) {
    this.schema = schema;
    this.properties = properties;

    this.featureGroupName = properties.get(FEATURE_GROUP_NAME_KEY);
    if (featureGroupName == null || featureGroupName.isEmpty())
      throw new RuntimeException("feature-group-name cannot be null or empty");
    this.featureGroupVersion = properties.get(FEATURE_GROUP_VERSION_KEY);

    this.tableName = featureGroupName;
    this.kafkaHost = adminService.getKafkaHost();
    this.kafkaTopic = adminService.getKafkaTopicFromAdmin(featureGroupName);
    this.runId = properties.getOrDefault(RUN_ID_KEY, RunDataUtils.createRunId());
  }

  @Override
  public String name() {
    return tableName;
  }

  @Override
  public StructType schema() {
    return schema;
  }

  @Override
  public Set<TableCapability> capabilities() {
    Set<TableCapability> capabilities = new HashSet<>();
    capabilities.add(TableCapability.BATCH_WRITE);
    return capabilities;
  }

  @Override
  public WriteBuilder newWriteBuilder(LogicalWriteInfo info) {
    return new OfsWriteBuilder(
        properties, schema, featureGroupName, featureGroupVersion, kafkaHost, kafkaTopic, runId);
  }
}
