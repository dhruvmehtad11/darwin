package com.dream11.core.dto.populator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureGroupPopulatorMap {
  @JsonProperty("tenant_name")
  private String tenantName;
  @JsonProperty("num_workers")
  private Integer numWorkers;
}
