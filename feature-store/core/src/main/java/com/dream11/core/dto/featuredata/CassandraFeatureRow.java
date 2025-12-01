package com.dream11.core.dto.featuredata;

import com.dream11.core.dto.featuredata.interfaces.FeatureRow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureRow implements FeatureRow {
  private List<Object> row;
}
