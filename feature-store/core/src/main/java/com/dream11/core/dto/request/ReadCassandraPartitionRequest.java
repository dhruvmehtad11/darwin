package com.dream11.core.dto.request;

import java.util.List;
import com.dream11.core.dto.featuredata.CassandraPartitionData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadCassandraPartitionRequest {
    private String featureGroupName;
    private String featureGroupVersion;
    private List<String> featureColumns;
    private CassandraPartitionData partitionKey;

  public static Boolean validate(ReadCassandraPartitionRequest request) {
    if (request.getFeatureGroupName() == null
        || request.getFeatureColumns() == null
        || request.getFeatureColumns().isEmpty()
        || request.getPartitionKey() == null
        || request.getPartitionKey().getNames() == null
        || request.getPartitionKey().getNames().isEmpty()
        || request.getPartitionKey().getValues() == null
        || request.getPartitionKey().getValues().isEmpty()) {
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }
}
