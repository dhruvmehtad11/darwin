package com.dream11.core.dto.featuredata;

import com.dream11.core.dto.featuredata.interfaces.FeatureData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraPartitionData {
  private List<String> names;
  private List<Object> values;
}
