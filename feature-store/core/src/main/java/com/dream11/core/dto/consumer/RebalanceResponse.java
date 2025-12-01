package com.dream11.core.dto.consumer;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class RebalanceResponse {
  private Boolean topologyChanged;
}
