package com.dream11.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HelixConfig {
  private String zkServer;
  private String clusterName;
  private String instanceName;
}
