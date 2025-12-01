package com.dream11.admin.dto.fctapplayer.response;

import java.util.List;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetFeatureGroupResponse {
  private String id;
  private String title;
  private String description;
  private Integer version;
  private List<Integer> allVersions;
  private String lastValueUpdated;
  private List<String> lastFiveRuns;
  private String createdBy;
  private List<String> tags;
  private String type;
  private List<String> typesAvailable;
  private List<CopyCode> copyCode;
  private List<Sinks> sinks;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CopyCode {
    private String name;
    private String value;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Sinks {
    private String location;
    private String type;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Source {
    private String location;
    private String type;
  }
}
