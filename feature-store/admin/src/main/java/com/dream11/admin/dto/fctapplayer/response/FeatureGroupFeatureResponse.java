package com.dream11.admin.dto.fctapplayer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureGroupFeatureResponse {
  private String title;
  private String description;
  private List<String> tags;
  private String type;
  private Boolean isPrimaryKey;
  private List<GetFeatureGroupResponse.CopyCode> copyCode;
  private String usage = null;
}
