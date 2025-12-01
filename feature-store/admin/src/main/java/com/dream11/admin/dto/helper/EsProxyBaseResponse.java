package com.dream11.admin.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EsProxyBaseResponse<T> {
    private T data;
    private Integer statusCode;
}