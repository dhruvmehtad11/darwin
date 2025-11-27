package com.dream11.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCassandraFeatureGroupSchemaResponseMetadataPair {
  private GetCassandraFeatureGroupSchemaResponse schema;
  private String name;
  private String version;
}
