package com.dream11.admin.dto.fctapplayer.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetFeatureGroupCountRequest {
  @JsonProperty("search_string")
  private String searchString;

  private List<Filter> filters;

  public Map<String, Set<String>> getFilterMap(){
    Map<String, Set<String>> map = new HashMap<>();
    for (Filter filter : filters) {
        if(map.containsKey(filter.getName()))
          map.get(filter.getName()).addAll(filter.getValue());
        else{
          map.put(filter.getName(), new HashSet<>(filter.getValue()));
        }
    }
    return map;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Filter {
    private String name;
    private Set<String> value;

    public enum FilterName {
      tags,
      owners
    }

    @JsonIgnore
    public Set<String> getValueMap() {
      return new HashSet<>(this.value);
    }
  }
}
