package com.dream11.core.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyGetFeatureGroupTopicResponse {
  @Builder.Default private String message = "Feature Group Topic Details Fetched Successfully";
  private Data data;

  @lombok.Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  private static class Data {
    private String topic;
  }

  public static LegacyGetFeatureGroupTopicResponse getSuccessResponse(String topicName) {
    return LegacyGetFeatureGroupTopicResponse.builder()
        .data(Data.builder().topic(topicName).build())
        .build();
  }
}
