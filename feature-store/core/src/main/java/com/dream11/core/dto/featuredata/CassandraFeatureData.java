package com.dream11.core.dto.featuredata;

import java.util.List;

import com.dream11.core.dto.featuredata.interfaces.FeatureData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureData implements FeatureData {
  private List<String> names;
  private List<List<Object>> values;
}
