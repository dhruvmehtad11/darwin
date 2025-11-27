package com.dream11.admin.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FctAppLayerBaseResponse<T> {
  private Status status;
  private Integer statusCode;
  private T data;

  public enum Status{
    SUCCESS,
    ERROR
  }
}
