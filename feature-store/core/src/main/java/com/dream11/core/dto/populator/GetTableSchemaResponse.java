package com.dream11.core.dto.populator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetTableSchemaResponse {
  private String path;
  private Map<String, String> schema;
}
