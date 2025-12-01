package com.dream11.core.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Min;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFeatureGroupTtlRequest {
  private String name;
  @Builder.Default
  private String version = null;

  @Min(value = 0, message = "The ttl value must be non-negative")
  private Long ttl;
}
