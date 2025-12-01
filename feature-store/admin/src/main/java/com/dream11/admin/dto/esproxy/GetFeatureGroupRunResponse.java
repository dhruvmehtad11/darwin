package com.dream11.admin.dto.esproxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetFeatureGroupRunResponse {
  @JsonProperty("feature_group_run")
  FeatureGroupRun featureGroupRun;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FeatureGroupRun {
    private String name;
    private List<RunData> data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunData {
      private String name;
      private Double timestamp;

      @JsonProperty("time_taken")
      private Double timeTaken;

      @JsonProperty("sample_data")
      private List<Map<String, Object>> sampleData;

      private Integer count;
      private String status;

      @JsonProperty("error_message")
      private String errorMessage;
    }
  }
}
