package com.dream11.core.dto.request.legacystack;

import com.dream11.core.dto.request.BatchWriteRequest;
import com.dream11.core.dto.request.WriteCassandraFeaturesBatchRequest;
import com.dream11.core.dto.request.WriteCassandraFeaturesRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyBatchWriteFeaturesRequest implements LegacyRequest, BatchWriteRequest {
  @NotNull
  @Valid
  @JsonProperty("Body")
  List<LegacyWriteFeaturesRequest> batches;

  public static WriteCassandraFeaturesBatchRequest getWriteCassandraFeaturesBatchRequest(
      LegacyBatchWriteFeaturesRequest legacyBatchWriteFeaturesRequest) {
    List<WriteCassandraFeaturesRequest> batch = new ArrayList<>();
    for (LegacyWriteFeaturesRequest legacyWriteFeaturesRequest :
        legacyBatchWriteFeaturesRequest.getBatches()) {
      batch.add(LegacyWriteFeaturesRequest.getWriteFeaturesRequest(legacyWriteFeaturesRequest));
    }
    return WriteCassandraFeaturesBatchRequest.builder().batches(batch).build();
  }

  // converting to lowercase to support legacy stack
  public void patchRequest() {
    for (LegacyWriteFeaturesRequest legacyWriteFeaturesRequest : batches) {
      legacyWriteFeaturesRequest.patchRequest();
    }
  }
}
