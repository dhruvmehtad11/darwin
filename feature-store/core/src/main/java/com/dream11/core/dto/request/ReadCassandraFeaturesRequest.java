package com.dream11.core.dto.request;

import com.dream11.core.dto.featuredata.CassandraFeatureData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadCassandraFeaturesRequest {
    private String featureGroupName;
    private String featureGroupVersion;
    private List<String> featureColumns;
    private CassandraFeatureData primaryKeys;

  public static Boolean validate(ReadCassandraFeaturesRequest request) {
    if (request.getFeatureGroupName() == null
        || request.getFeatureColumns() == null
        || request.getFeatureColumns().isEmpty()
        || request.getPrimaryKeys() == null
        || request.getPrimaryKeys().getNames() == null
        || request.getPrimaryKeys().getNames().isEmpty()
        || request.getPrimaryKeys().getValues() == null
        || request.getPrimaryKeys().getValues().isEmpty()) {
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }
}
