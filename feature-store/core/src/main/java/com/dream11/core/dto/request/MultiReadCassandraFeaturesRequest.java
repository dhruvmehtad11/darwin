package com.dream11.core.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultiReadCassandraFeaturesRequest {
  public static final Integer DEFAULT_MAX_ROWS = 10;

  private List<ReadCassandraFeaturesRequest> batches;

  public static Boolean validate(MultiReadCassandraFeaturesRequest request) {
    if (request.getBatches() == null || request.getBatches().isEmpty()) {
      return Boolean.FALSE;
    }
    for (ReadCassandraFeaturesRequest readCassandraFeaturesRequest : request.getBatches()) {
      if (!ReadCassandraFeaturesRequest.validate(readCassandraFeaturesRequest)) {
        return Boolean.FALSE;
      }
    }
    return Boolean.TRUE;
  }
}
