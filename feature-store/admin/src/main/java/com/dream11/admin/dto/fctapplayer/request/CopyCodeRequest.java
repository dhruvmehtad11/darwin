package com.dream11.admin.dto.fctapplayer.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyCodeRequest {
  @JsonProperty("feature_group_name")
  private String featureGroupName;

  @JsonProperty("feature_titles")
  private List<String> featureTitles;
}
