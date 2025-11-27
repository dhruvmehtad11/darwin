package com.dream11.core.constant;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum MetricsEnum {
  SUCCESS_READ_COUNTERS("SUCCESS_READ_COUNTERS_CACHE","feature_group.read.success"),
  FAILED_READ_COUNTERS("FAILED_READ_COUNTERS_CACHE","feature_group.read.failed"),
  SUCCESS_PARTITION_READ_COUNTERS ("SUCCESS_PARTITION_READ_COUNTERS_CACHE","feature_group_partition.read.success"),
  FAILED_PARTITION_READ_COUNTERS( "FAILED_PARTITION_READ_COUNTERS_CACHE","feature_group_partition.read.failed"),
  SUCCESS_WRITE_COUNTERS( "SUCCESS_WRITE_COUNTERS_CACHE","feature_group.write.success"),
  FAILED_WRITE_COUNTERS ( "FAILED_WRITE_COUNTERS_CACHE","feature_group.write.failed"),
  READ_ROW_SIZE_DISTRIBUTION_SUMMARY ("READ_ROW_SIZE_DISTRIBUTION_SUMMARY_CACHE","feature_group.read.row.size"),
  PARTITION_READ_ROW_SIZE_DISTRIBUTION_SUMMARY("PARTITION_READ_ROW_SIZE_DISTRIBUTION_SUMMARY_CACHE","feature_group_partition.read.row"
      + ".size"),
  WRITE_ROW_SIZE_DISTRIBUTION_SUMMARY( "WRITE_ROW_SIZE_DISTRIBUTION_SUMMARY_CACHE","feature_group.write.row.size"),
  CONSUMER_SUCCESS_MESSAGE_PROCESSOR_COUNTER( "SUCCESS_MESSAGE_PROCESSOR_COUNTER_CACHE","consumer.write.success"),
  CONSUMER_FAILED_MESSAGE_PROCESSOR_COUNTER("FAILED_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME","consumer.write.failed");

  String metricCacheKey;
  String metricName;
  MetricsEnum(String metricCacheKey, String metricName) {
    this.metricCacheKey = metricCacheKey;
    this.metricName = metricName;
  }
  public static String getMetricNameFromCacheKey(String cacheKey) {
    for (MetricsEnum metricsEnum : MetricsEnum.values()) {
      if (metricsEnum.getMetricCacheKey().equals(cacheKey)) {
        return metricsEnum.getMetricName();
      }
    }
    return null;
  }
}
