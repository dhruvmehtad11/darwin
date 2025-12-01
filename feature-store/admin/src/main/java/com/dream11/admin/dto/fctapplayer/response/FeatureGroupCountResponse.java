package com.dream11.admin.dto.fctapplayer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeatureGroupCountResponse {
  private Long onlineCount;
  private Long offlineCount;
}
