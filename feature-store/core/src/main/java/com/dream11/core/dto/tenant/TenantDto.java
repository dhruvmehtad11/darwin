package com.dream11.core.dto.tenant;

import com.dream11.core.dto.metastore.TenantConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantDto {
  private String featureGroupName;
  private TenantConfig tenantConfig;
}