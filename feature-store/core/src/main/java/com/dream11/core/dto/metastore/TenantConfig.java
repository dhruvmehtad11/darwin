package com.dream11.core.dto.metastore;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.dream11.core.constant.Constants.DEFAULT_TENANT_NAME;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantConfig {
  public static final TenantConfig DEFAULT_TENANT_CONFIG = TenantConfig.builder().build();

  @JsonProperty("read")
  @Builder.Default
  private String readerTenant = DEFAULT_TENANT_NAME;

  @JsonProperty("write")
  @Builder.Default
  private String writerTenant = DEFAULT_TENANT_NAME;

  @JsonProperty("consume")
  @Builder.Default
  private String consumerTenant = DEFAULT_TENANT_NAME;
}
