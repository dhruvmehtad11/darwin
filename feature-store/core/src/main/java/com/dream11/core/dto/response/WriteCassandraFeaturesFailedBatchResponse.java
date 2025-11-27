package com.dream11.core.dto.response;

import com.dream11.core.dto.request.WriteCassandraFeaturesRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteCassandraFeaturesFailedBatchResponse {
  private String error;
  private WriteCassandraFeaturesRequest batch;
}
