package com.dream11.core.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetFeatureGroupConsumerMetadataResponse {
  private String tenantName;
  private String topicName;
  private Integer numPartitions;
  private Integer numConsumers;

  public static GetFeatureGroupConsumerMetadataResponse getResponse(FeatureGroupConsumerMap map){
    return GetFeatureGroupConsumerMetadataResponse.builder()
        .tenantName(map.getTenantName())
        .topicName(map.getTopicName())
        .numPartitions(map.getNumPartitions())
        .numConsumers(map.getNumConsumers())
        .build();
  }
}
