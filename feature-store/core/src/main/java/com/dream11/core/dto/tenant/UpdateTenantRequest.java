package com.dream11.core.dto.tenant;

import com.dream11.core.constant.Constants;
import com.dream11.core.dto.metastore.TenantConfig;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTenantRequest {
  @NotNull
  private Constants.FeatureStoreTenantType tenantType;
  @NotNull
  private String currentTenant;
  @NotNull
  private String newTenant;
}
