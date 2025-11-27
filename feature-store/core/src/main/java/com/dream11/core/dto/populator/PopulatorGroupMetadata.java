package com.dream11.core.dto.populator;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PopulatorGroupMetadata {
  private String tenantName;
  private Integer numWorkers;
}
