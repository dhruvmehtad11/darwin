package com.dream11.admin.dto.fctapplayer.response;

import com.dream11.admin.dto.esproxy.GetFeatureGroupRunResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.annotation.Nullable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunDataResponse {
  private Double executionTime;
  private Double duration;
  private String status;
  private Integer statusCode;
  @JsonInclude
  private String link = null;
  private Integer version;
  private Integer totalRecordsCount;
  @JsonInclude
  private String message = null;

  private List<FeatureDetails> features;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FeatureDetails {
    private String title;
    private List<OtherFactors> otherFactors;
    private List<Object> sampleData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OtherFactors {
      private String name;
      @JsonInclude
      private Object value;
    }
  }

  @Data
  @Builder
  private static class StatsResult {
    public Integer mean;
    public Integer median;
    public Integer mode;
  }

  public static RunDataResponse getResponse(
      GetFeatureGroupRunResponse.FeatureGroupRun.RunData runData) {
    List<FeatureDetails> details = new ArrayList<>();
    if (!runData.getSampleData().isEmpty()) {
      List<String> li = new ArrayList<>(runData.getSampleData().get(0).keySet());

      for (String featureName : li) {
        List<Object> data = new ArrayList<>();
        for (Map<String, Object> map : runData.getSampleData()) {
          if (map.containsKey(featureName)) data.add(map.get(featureName));
        }
        details.add(
            FeatureDetails.builder()
                .title(featureName)
                .sampleData(data)
                .otherFactors(getFeatureInsights(data))
                .build());
      }
    }

    return RunDataResponse.builder()
        .executionTime(runData.getTimestamp())
        .duration(runData.getTimeTaken())
        .status(runData.getStatus())
        .statusCode(Objects.equals(runData.getStatus(), "SUCCESS") ? 200 : 500)
        .version(1)
        .totalRecordsCount(runData.getCount())
        .message(runData.getErrorMessage() != null ? runData.getErrorMessage() : null)
        .features(details)
        .build();
  }

  private static List<FeatureDetails.OtherFactors> getFeatureInsights(List<Object> data) {
    StatsResult statsResult = calculateStats(data);
    List<FeatureDetails.OtherFactors> li = new ArrayList<>();
    li.add(
        FeatureDetails.OtherFactors.builder()
            .name("mean")
            .value(statsResult != null ? statsResult.getMean() : null)
            .build());
    li.add(
        FeatureDetails.OtherFactors.builder()
            .name("mode")
            .value(statsResult != null ? statsResult.getMode() : null)
            .build());
    li.add(
        FeatureDetails.OtherFactors.builder()
            .name("median")
            .value(statsResult != null ? statsResult.getMedian() : null)
            .build());
    return li;
  }

  public static StatsResult calculateStats(List<Object> list) {
    // Filter out any non-numeric objects
    List<Double> numbers =
        list.stream()
            .filter(obj -> obj instanceof Number)
            .map(obj -> ((Number) obj).doubleValue())
            .collect(Collectors.toList());

    // If there are non-numeric objects or the list is empty, return null
    if (numbers.size() != list.size() || numbers.isEmpty()) {
      return null;
    }

    // Calculate mean
    double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
    double mean = sum / numbers.size();

    // Calculate median
    Collections.sort(numbers);
    double median;
    int size = numbers.size();
    if (size % 2 == 0) {
      median = (numbers.get(size / 2 - 1) + numbers.get(size / 2)) / 2.0;
    } else {
      median = numbers.get(size / 2);
    }

    // Calculate mode
    Map<Double, Long> frequencyMap =
        numbers.stream().collect(Collectors.groupingBy(Double::doubleValue, Collectors.counting()));

    long maxFrequency = frequencyMap.values().stream().max(Long::compare).orElse(0L);
    List<Double> mode =
        frequencyMap.entrySet().stream()
            .filter(entry -> entry.getValue() == maxFrequency)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

    // Return the result
    return StatsResult.builder()
        .mean((int) mean)
        .mode(!mode.isEmpty() ? (mode.get(0).intValue()) : 0)
        .median((int) median)
        .build();
  }
}
