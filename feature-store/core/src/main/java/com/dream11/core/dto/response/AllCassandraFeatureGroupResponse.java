package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.response.interfaces.SearchFeatureGroupResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllCassandraFeatureGroupResponse implements SearchFeatureGroupResponse {
  private List<CassandraFeatureGroupMetadata> featureGroups;
}
