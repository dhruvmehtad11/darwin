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
public class AddTableSinceTimestampRequest {
  private String tenantName;
  private String tableName;
  private String featureGroupName;
  private String featureGroupVersion;
  private String topicName;
  private String path;
  @Builder.Default private String runId = RunDataUtils.createRunId();
  @Builder.Default private Boolean replicateWrites = Boolean.TRUE;
  private Long timestamp;

  public static AddTableRequest getRequest(AddTableSinceTimestampRequest request, Long start, Long end){
    return AddTableRequest.builder()
        .tenantName(request.tenantName)
        .tableName(request.tableName)
        .featureGroupName(request.featureGroupName)
        .featureGroupVersion(request.featureGroupVersion)
        .topicName(request.topicName)
        .path(request.path)
        .runId(request.runId)
        .replicateWrites(request.replicateWrites)
        .startVersion(start)
        .endVersion(end)
        .build();
  }
}
