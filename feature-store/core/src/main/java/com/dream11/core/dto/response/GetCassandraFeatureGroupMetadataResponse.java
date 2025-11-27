package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.response.interfaces.GetFeatureGroupMetadataResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCassandraFeatureGroupMetadataResponse implements GetFeatureGroupMetadataResponse {
  @JsonProperty("metadata")
  private CassandraFeatureGroupMetadata featureGroupMetadata;
}
