package com.dream11.core.dto.response;

import com.dream11.core.dto.helper.SuccessfulReadKey;
import com.dream11.core.dto.response.interfaces.ReadFeaturesResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadCassandraPartitionResponse implements ReadFeaturesResponse {
  private String featureGroupName;
  private String featureGroupVersion;
  private List<List<Object>> features;

  @JsonIgnore
  private List<Long> payloadSizes;
}
