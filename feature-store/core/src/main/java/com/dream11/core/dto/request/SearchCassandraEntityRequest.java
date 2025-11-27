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
public class SearchCassandraEntityRequest {
  String searchPattern;
  List<String> tags;
  List<String> owners;

  public static Boolean validateRequest(SearchCassandraEntityRequest searchCassandraEntityRequest) {
    if (searchCassandraEntityRequest.getSearchPattern() == null
        && (searchCassandraEntityRequest.getOwners() == null
            || searchCassandraEntityRequest.getOwners().isEmpty())
        && (searchCassandraEntityRequest.getTags() == null
            || searchCassandraEntityRequest.getTags().isEmpty())) return Boolean.FALSE;

    return Boolean.TRUE;
  }

  public static Set<String> getOwnerSet(SearchCassandraEntityRequest searchCassandraEntityRequest) {
    return new HashSet<>(searchCassandraEntityRequest.getOwners());
  }
}
