package com.dream11.core.dto.populator;

import com.dream11.core.util.RunDataUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTableLatestVersionResponse {
  private String path;
  private Long latestVersion;
}
