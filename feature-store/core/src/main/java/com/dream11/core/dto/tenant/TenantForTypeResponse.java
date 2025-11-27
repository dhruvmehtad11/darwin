package com.dream11.core.dto.tenant;

import com.dream11.core.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantForTypeResponse {
  @NotNull
  private Constants.FeatureStoreTenantType tenantType;
  @NotNull
  private List<String> names;
}
