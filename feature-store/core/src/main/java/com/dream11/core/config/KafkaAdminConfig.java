package com.dream11.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaAdminConfig {
  private String bootstrapServers;
  private String clientId;
  private String securityProtocol;
  private String saslMechanism;
  private String saslJaasConfig;
  private int retries;
  private int requestTimeoutMs;
  private int connectionsMaxIdleMs;
  private int metadataMaxAgeMs;
  private int retryBackoffMs;
}
