package com.dream11.core.dto.helper;

import com.dream11.core.dto.request.WriteCassandraFeaturesBatchRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteRequestWithReplicationFlag {
  private WriteCassandraFeaturesBatchRequest request;
  @Builder.Default private Boolean replicateWrites = Boolean.TRUE;
}
