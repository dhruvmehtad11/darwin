package com.dream11.spark.writer;

import java.util.Map;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.DataWriterFactory;
import org.apache.spark.sql.types.StructType;

public class OfsDataWriterFactory implements DataWriterFactory {
  private final Map<String, String> properties;
  private final StructType schema;
  private final String featureGroupName;
  private final String featureGroupVersion;
  private final String kafkaHost;
  private final String kafkaTopic;
  private final String runId;

  public OfsDataWriterFactory(Map<String, String> properties,
                              StructType schema,
                              String featureGroupName,
                              String featureGroupVersion,
                              String kafkaHost,
                              String kafkaTopic,
                              String runId) {
    this.properties = properties;
    this.schema = schema;
    this.featureGroupName = featureGroupName;
    this.featureGroupVersion = featureGroupVersion;
    this.kafkaHost = kafkaHost;
    this.kafkaTopic = kafkaTopic;
    this.runId = runId;
  }

  @Override
  public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
    return new OfsDataWriter(partitionId, taskId, schema, featureGroupName, featureGroupVersion, kafkaHost, kafkaTopic, runId);
  }
}
