package com.dream11.spark.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCassandraFeatureGroupSchemaResponse {
    private List<CassandraFeatureColumn> schema;
    private List<String> primaryKeys;
    private String version;
    private FeatureGroupType featureGroupType;

    public enum FeatureGroupType{
      ONLINE, OFFLINE
    }
}
