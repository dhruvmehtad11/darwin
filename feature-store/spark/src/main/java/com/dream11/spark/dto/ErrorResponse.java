package com.dream11.spark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
  private Error error;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Error {
    private String code;
    private String message;
  }
}
