package com.dream11.admin.dto.esproxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetEntityResponse {

  private Entity entity;
  private long ttl;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Entity{
    private String name;

    @JsonProperty("value_type")
    private List<String> valueType;

    @JsonProperty("join_keys")
    private List<String> joinKeys;

    private String description;
  }

}
