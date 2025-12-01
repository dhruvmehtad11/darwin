package com.dream11.core.dto.helper;

import com.dream11.core.dto.response.WriteCassandraFeaturesFailedBatchResponse;
import com.dream11.core.dto.response.WriteCassandraFeaturesResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkWriteCassandraFeaturesHelper {
  private Boolean success;
  private WriteCassandraFeaturesResponse successBatch;
  private WriteCassandraFeaturesFailedBatchResponse failedBatch;
}
