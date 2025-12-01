package com.dream11.core.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultiBulkReadResponseHelper<T> {
  private String featureGroupName;
  private LegacyFeatureStoreResponse<T> features;
}
