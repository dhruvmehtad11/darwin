package com.dream11.core.dto.response;

import com.dream11.core.dto.response.interfaces.WriteBulkFeaturesResponse;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteBulkCassandraFeaturesResponse implements WriteBulkFeaturesResponse {
    private List<WriteCassandraFeaturesResponse> batches;
    private List<WriteCassandraFeaturesFailedBatchResponse> failedBatches;
}
