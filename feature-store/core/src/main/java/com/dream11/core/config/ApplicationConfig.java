package com.dream11.core.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApplicationConfig {
  private String cassandraOfsKeySpace;
  private Long cassandraMetaStoreCacheUpdationTime;
  private Long cassandraMetaStoreCacheUpdationJobHealthcheckTime;
  private Integer cassandraBatchChunkSize;
  private Integer cassandraMaxBatchSizeBytes;
  private String ofsAdminHost;

  private String ofsAdminConsumersMetadataEndpoint;
  private String ofsAdminGetFeatureGroupMetadataEndpoint;
  private String ofsAdminGetAllFeatureGroupMetadataEndpoint;
  private String ofsAdminGetAllUpdatedFeatureGroupMetadataEndpoint;
  private String ofsAdminGetEntityMetadataEndpoint;
  private String ofsAdminGetAllEntityMetadataEndpoint;
  private String ofsAdminGetAllUpdatedEntityMetadataEndpoint;
  private String ofsAdminGetFeatureGroupLatestVersionEndpoint;
  private String ofsAdminGetAllFeatureGroupLatestVersionEndpoint;
  private String ofsAdminGetAllUpdatedFeatureGroupLatestVersionEndpoint;

  private String ofsAdminCreateEntityEndpoint;
  private String ofsAdminCreateFeatureGroupEndpoint;
  private String ofsHost;
  private String ofsWriterHost;
  private String ofsAsyncBatchWriteEndpoint;
  private String ofsBatchWriteV1Endpoint;

  private Long ofsWriterMaxBatchSize;
  private Integer defaultConsumerRecordFetchSize;
  private Short defaultKafkaTopicReplicationFactor;
  private Long defaultKafkaTopicRetentionMs;

  private Long defaultConsumerPollTime;

  private String consumerHelixClusterName;
  private String populatorHelixClusterName;
  private String esProxyHost;
  private String genericCassandraHost;

  private String ofsAdminGetFeatureTenantMetadataEndpoint;
  private String ofsAdminGetFeatureAllTenantMetadataEndpoint;

  private String metastoreS3Bucket;
  private String metastoreS3BucketBasePath;
  private String metastoreS3FullMetadataBasePath;
  private String metastoreS3EntityBasePath;
  private String metastoreS3FeatureGroupBasePath;
  private String metastoreS3FeatureGroupVersionBasePath;
  private String metastoreS3FeatureGroupSchemaBasePath;
  private String metastoreS3FeatureGroupTopicBasePath;
  private String populatorScanFileBucket;
  private String ofsAdminPopulatorMetadataEndpoint;
  private Integer populatorWorkerParallelism;
  private String metastoreS3ConsumerConfigBasePath;
  private String metastoreS3KafkaConfigBasePath;
}
