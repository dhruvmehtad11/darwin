package com.dream11.core.dto.tenant;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAllTenantResponse {
   private List<TenantForTypeResponse> tenants; 
}
