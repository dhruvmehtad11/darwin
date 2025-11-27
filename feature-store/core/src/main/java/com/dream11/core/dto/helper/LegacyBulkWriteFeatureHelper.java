package com.dream11.core.dto.helper;

import com.dream11.core.dto.response.WriteCassandraFeaturesResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyBulkWriteFeatureHelper {
  private WriteCassandraFeaturesResponse successfulBatch;
  private LegacyBatchWriteFailedRecords failedBatch;

  public static List<LegacyBatchWriteFailedRecords> getFailedRecords(
      List<LegacyBulkWriteFeatureHelper> li) {
    List<LegacyBatchWriteFailedRecords> res = new ArrayList<>();
    for (LegacyBulkWriteFeatureHelper l : li) {
      if (l.failedBatch != null) res.add(l.failedBatch);
    }
    return res;
  }

  public static List<WriteCassandraFeaturesResponse> getSuccessfulRecords(
          List<LegacyBulkWriteFeatureHelper> li) {
    List<WriteCassandraFeaturesResponse> res = new ArrayList<>();
    for (LegacyBulkWriteFeatureHelper l : li) {
      if (l.successfulBatch != null) res.add(l.successfulBatch);
    }
    return res;
  }
}
