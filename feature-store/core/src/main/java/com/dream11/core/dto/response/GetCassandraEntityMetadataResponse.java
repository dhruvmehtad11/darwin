package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.response.interfaces.GetEntityMetadataResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCassandraEntityMetadataResponse implements GetEntityMetadataResponse {
  @JsonProperty("metadata")
  private CassandraEntityMetadata entityMetadata;
}
