package com.dream11.core.dto.request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCassandraFeatureGroupRequest {
  String searchPattern;
  List<String> tags;
  List<String> owners;

  public static Boolean validateRequest(
      SearchCassandraFeatureGroupRequest searchCassandraFeatureGroupRequest) {
    if (searchCassandraFeatureGroupRequest.getSearchPattern() == null
            && (searchCassandraFeatureGroupRequest.getOwners() == null
                || searchCassandraFeatureGroupRequest.getOwners().isEmpty())
            && (searchCassandraFeatureGroupRequest.getTags() == null
        || searchCassandraFeatureGroupRequest.getTags().isEmpty())) return Boolean.FALSE;

    return Boolean.TRUE;
  }

  public static Set<String> getOwnerSet(
      SearchCassandraFeatureGroupRequest searchCassandraFeatureGroupRequest) {
    return new HashSet<>(searchCassandraFeatureGroupRequest.getOwners());
  }
}
