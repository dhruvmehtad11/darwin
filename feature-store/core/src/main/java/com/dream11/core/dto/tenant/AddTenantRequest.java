package com.dream11.core.dto.tenant;

import com.dream11.core.dto.metastore.TenantConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddTenantRequest {
  @NotNull
  private String featureGroupName;
  @NotNull
  private TenantConfig tenantConfig;
}
