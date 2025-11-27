package com.dream11.core.dto.request;

import com.dream11.core.dto.featuredata.CassandraFeatureData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteCassandraFeaturesRequest {
  private String featureGroupName;
  private String featureGroupVersion;
  private CassandraFeatureData features;
  private String runId;

  public static Boolean validate(WriteCassandraFeaturesRequest request) {
    if (request.getFeatureGroupName() == null || request.getFeatures() == null || request.getFeatures().getNames().isEmpty() ||
        request.getFeatures().getValues().isEmpty()) {
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }
}

