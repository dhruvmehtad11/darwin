package com.dream11.spark.writer;

import java.util.Map;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.DataWriterFactory;
import org.apache.spark.sql.connector.write.PhysicalWriteInfo;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructType;

public class OfsBatchWrite implements BatchWrite {
  private final Map<String, String> properties;
  private final StructType schema;
  private final String featureGroupName;
  private final String featureGroupVersion;
  private final String kafkaHost;
  private final String kafkaTopic;
  private final String runId;

  public OfsBatchWrite(
      Map<String, String> properties,
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
  public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
    return new OfsDataWriterFactory(
        properties, schema, featureGroupName, featureGroupVersion, kafkaHost, kafkaTopic, runId);
  }

  @Override
  public void commit(WriterCommitMessage[] messages) {
    // Handle commit logic if needed
  }

  @Override
  public void abort(WriterCommitMessage[] messages) {
    // Handle abort logic if needed
  }
}
