package com.dream11.core.dto.helper;

import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.dto.request.legacystack.LegacyRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletionStage;

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyFeatureStoreResponse<T> {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LegacyRequest body;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer code;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Object message;
}
