package com.dream11.core.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddFeatureGroupConsumerMetadataRequest {
  private String tenantName;
  private String topicName;
  private Integer numPartitions;
}
