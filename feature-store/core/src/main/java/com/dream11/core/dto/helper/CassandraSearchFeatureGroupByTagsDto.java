package com.dream11.core.dto.helper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraSearchFeatureGroupByTagsDto {
  @JsonProperty("feature_group_name")
  private String featureGroupName;

  @JsonProperty("feature_group_version")
  private String featureGroupVersion;
}
