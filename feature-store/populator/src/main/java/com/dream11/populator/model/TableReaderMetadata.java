package com.dream11.populator.model;

import com.dream11.core.util.RunDataUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableReaderMetadata {
  private String name;
  private String path;
  private String tenantName;
  private String featureGroupName;
  private String featureGroupVersion;
  private String topicName;
  private String runId = RunDataUtils.createRunId();
  private Boolean replicateWrites = Boolean.TRUE;
  private Long startVersion;
  private Long endVersion;
}
