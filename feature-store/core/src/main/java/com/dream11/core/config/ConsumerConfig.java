package com.dream11.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumerConfig {
  private String clientId;
  private String bootstrapServers;
  private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
  private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
  private String schemaRegistryUrl;
  private Integer maxPollRecords;
  private Long pollTimeoutMs;
  private String groupId;
  private Boolean enableAutoCommit = false;
  private Long autoCommitIntervalMs;
  private String autoOffsetReset = "earliest";
  private Integer sessionTimeoutMs;
  private Integer heartbeatIntervalMs;
  private Long metadataMaxAgeMs;
  private Long metadataMaxIdleMs;
}
