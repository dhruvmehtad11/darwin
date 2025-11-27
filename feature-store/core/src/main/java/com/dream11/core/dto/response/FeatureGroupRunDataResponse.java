package com.dream11.core.dto.response;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureGroupRunDataResponse {
  private String name;
  private String version;
  @JsonAlias("run_id")
  private String runId;
  @JsonAlias("time_taken")
  private Integer timeTaken;
  @JsonAlias("row_count")
  private Integer count;
  @JsonAlias("sample_data")
  private List<Map<String, Object>> sampleData;
  @JsonAlias("error_message")
  private String errorMessage;
  private RunStatus status;
  private Long timestamp;


  public enum RunStatus{
    SUCCESS, FAILED
  }
}
