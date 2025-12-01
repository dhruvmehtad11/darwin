package com.dream11.core.dto.response;

import java.util.List;

import com.dream11.core.dto.response.interfaces.GetDistinctTagsResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetDistinctCassandraFeatureStoreTagsResponse implements GetDistinctTagsResponse {
  private List<String> tags;
}
