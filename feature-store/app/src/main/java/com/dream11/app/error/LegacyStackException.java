package com.dream11.app.error;

import com.dream11.core.dto.helper.LegacyFeatureStoreResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class LegacyStackException extends Throwable {
  private LegacyFeatureStoreResponse response;
  private int statusCode;

  @SneakyThrows
  public static LegacyStackException getException(LegacyFeatureStoreResponse response, int statusCode, ObjectMapper objectMapper){
    return LegacyStackException.builder().response(response).statusCode(statusCode).build();
  }
}
