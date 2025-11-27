package com.dream11.core.dto.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureGroupConsumerMap {
  @JsonProperty("tenant_name")
  private String tenantName;
  @JsonProperty("topic_name")
  private String topicName;
  @JsonProperty("num_partitions")
  private Integer numPartitions;
  @JsonProperty("num_consumers")
  private Integer numConsumers;
}
