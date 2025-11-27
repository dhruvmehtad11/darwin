package com.dream11.core.dto.response;

import com.dream11.core.dto.helper.Error;
import com.dream11.core.dto.helper.SuccessfulReadKey;
import java.util.List;

import com.dream11.core.dto.response.interfaces.ReadFeaturesResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadCassandraFeaturesResponse implements ReadFeaturesResponse {
  private String featureGroupName;
  private String featureGroupVersion;
  private List<SuccessfulReadKey> successfulKeys;
  private List<List<Object>> failedKeys;

  private String error;

  @JsonIgnore
  private List<Long> payloadSizes;
}
