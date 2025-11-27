package com.dream11.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProducerConfig {

  private String clientId;
  private String bootstrapServers;
  private String keySerializer;
  private String valueSerializer;
  private String schemaRegistryUrl;
  private Integer batchSize;
  private Long lingerMs;
  private String partitionerClass;
  private Integer maxRequestSize;
  private Integer maxInFlightRequestsPerConnection;
  private Long bufferMemory;
  private String acks;
  private Integer retries;
  private Integer requestTimeoutMs;
  private Long metadataMaxAgeMs;
  private Long metadataMaxIdleMs;
}