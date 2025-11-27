package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.response.interfaces.SearchFeatureGroupResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCassandraFeatureGroupResponse implements SearchFeatureGroupResponse {
  private List<CassandraFeatureGroupMetadata> featureGroups;
}
