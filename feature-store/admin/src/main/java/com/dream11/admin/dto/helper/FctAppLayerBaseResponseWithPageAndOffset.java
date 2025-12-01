package com.dream11.admin.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FctAppLayerBaseResponseWithPageAndOffset<T> {
  private Status status;
  private Integer statusCode;
  private Integer resultSize;
  private Integer pageSize;
  private Integer offset;
  private Integer totalRecordsCount;
  private T data;

  public enum Status{
    SUCCESS,
    ERROR
  }
}
