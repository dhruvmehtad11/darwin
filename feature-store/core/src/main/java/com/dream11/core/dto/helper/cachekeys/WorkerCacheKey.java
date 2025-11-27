package com.dream11.core.dto.helper.cachekeys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerCacheKey {
  private String workerId;
  private String tableName;
}
