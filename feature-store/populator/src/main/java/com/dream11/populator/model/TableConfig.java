package com.dream11.populator.model;

import io.delta.kernel.Table;
import io.delta.kernel.types.StructType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableConfig {
  private String name;
  private String path;
  private Table table;
  private StructType schema;
  private String featureGroupName;
  private String featureGroupVersion;
  private String topicName;
  private String runId;
  private Boolean replicateWrites;
  private Long startVersion;
  private Long endVersion;
}
