package com.dream11.admin.dto.esproxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregationSearchUsersAndTagsResponse {
  private Boolean success;
  private String message;
  private Aggregations aggregations;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Aggregations {
    @JsonProperty("distinct_field")
    private DistinctField distinctField;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DistinctField {
      @JsonProperty("after_key")
      private AfterKey afterKey;

      private List<Bucket> buckets;

      @Data
      @Builder
      @AllArgsConstructor
      @NoArgsConstructor
      public static class AfterKey {
        @JsonProperty("field_name")
        private String fieldName;
      }

      @Data
      @Builder
      @AllArgsConstructor
      @NoArgsConstructor
      public static class Bucket {
        private Key key;

        @JsonProperty("doc_count")
        private Integer docCount;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Key {
          @JsonProperty("field_name")
          private String fieldName;
        }
      }
    }
  }
}
