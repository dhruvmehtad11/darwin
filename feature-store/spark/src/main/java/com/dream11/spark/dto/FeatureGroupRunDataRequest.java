package com.dream11.spark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureGroupRunDataRequest {
  private String name;
  private String version;
  private String runId;
  private Integer timeTaken;
  private Long count;
  private List<Map<String, Object>> sampleData;
  private String errorMessage;
  private RunStatus status;

  public enum RunStatus{
    SUCCESS, FAILED
  }
}
