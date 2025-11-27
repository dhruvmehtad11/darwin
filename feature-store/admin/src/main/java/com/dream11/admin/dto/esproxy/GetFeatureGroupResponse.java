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
public class GetFeatureGroupResponse {
  @JsonProperty("feature_group")
  private FeatureGroup featureGroup;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FeatureGroup {
    private String name;
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("compute_meta_data")
    private ComputeMetadata computeMetaData;

    private Integer version;
    private List<com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse.Sinks> sinks;
    private List<String> tags;
    private String description;
    private List<String> entities;
    private String user;
    private List<String> notificationChannel;
    private List<com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse.Source> sources;

    @JsonProperty("is_online_enabled")
    private Boolean isOnlineEnabled;

    private List<SubSchema> schema;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubSchema {
      private String name;
      private String type;
      private List<String> tags;
      private String description;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComputeMetadata {
      @JsonProperty("compute_type")
      private String computeType;
      private String link;
    }
  }
}
