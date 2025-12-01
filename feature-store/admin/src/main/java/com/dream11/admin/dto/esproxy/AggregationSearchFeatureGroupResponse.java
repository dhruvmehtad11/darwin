package com.dream11.admin.dto.esproxy;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregationSearchFeatureGroupResponse {
  private Boolean success;

  private Response response;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response {
    private Total total;
    private List<GetFeatureGroupResponse.FeatureGroup> aggregations;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Total {
      private Integer value;
      private String relation;
    }
  }
}
