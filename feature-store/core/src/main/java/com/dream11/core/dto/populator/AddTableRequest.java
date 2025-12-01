package com.dream11.core.dto.populator;

import com.dream11.core.util.RunDataUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddTableRequest {
  private String tenantName;
  private String tableName;
  private String featureGroupName;
  private String featureGroupVersion;
  private String topicName;
  private String path;
  @Builder.Default private String runId = RunDataUtils.createRunId();
  @Builder.Default private Boolean replicateWrites = Boolean.TRUE;
  private Long startVersion;
  private Long endVersion;
}
