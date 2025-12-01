package com.dream11.app.service;

import static com.dream11.app.constant.Constants.*;

import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.constant.MetricsEnum;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKey;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKeyWithRunId;
import com.dream11.core.dto.response.ReadCassandraFeaturesResponse;
import com.dream11.core.dto.response.ReadCassandraPartitionResponse;
import com.dream11.core.dto.response.WriteCassandraFeaturesResponse;
import com.dream11.core.util.CacheUtils;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeatureGroupMetricService {
  private final PrometheusMeterRegistry meterRegistry =
      (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
  private final Vertx vertx = AppContext.getInstance(Vertx.class);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter> successReadCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, MetricsEnum.SUCCESS_READ_COUNTERS.getMetricCacheKey(), this::getSuccessReadCounter);
  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter> failedReadCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, MetricsEnum.FAILED_READ_COUNTERS.getMetricCacheKey(), this::getFailedReadCounter);
  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter>
      successPartitionReadCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx,
          MetricsEnum.SUCCESS_PARTITION_READ_COUNTERS.getMetricCacheKey(),
          this::getSuccessPartitionReadCounter);
  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter>
      failedPartitionReadCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx,
          MetricsEnum.FAILED_PARTITION_READ_COUNTERS.getMetricCacheKey(),
          this::getFailedPartitionReadCounter);
  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter> successWriteCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, MetricsEnum.SUCCESS_WRITE_COUNTERS.getMetricCacheKey(), this::getSuccessWriteCounter);
  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter> failedWriteCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, MetricsEnum.FAILED_WRITE_COUNTERS.getMetricCacheKey(), this::getFailedWriteCounter);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, DistributionSummary>
      readRowSizeDistributionSummaries =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx,
          MetricsEnum.READ_ROW_SIZE_DISTRIBUTION_SUMMARY.getMetricCacheKey(),
          this::getReadRowSizeDistributionSummary);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, DistributionSummary>
      partitionReadRowSizeDistributionSummaries =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx,
          MetricsEnum.PARTITION_READ_ROW_SIZE_DISTRIBUTION_SUMMARY.getMetricCacheKey(),
          this::getPartitionReadRowSizeDistributionSummary);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, DistributionSummary>
      writeRowSizeDistributionSummaries =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx,
          MetricsEnum.WRITE_ROW_SIZE_DISTRIBUTION_SUMMARY.getMetricCacheKey(),
          this::getWriteRowSizeDistributionSummary);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKeyWithRunId, Counter>
      successWriteCountersById =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx,
          SUCCESS_WRITE_COUNTERS_WITH_RUN_ID_CACHE_NAME,
          this::getSuccessWriteCounterById);
  private final AsyncLoadingCache<CassandraFeatureGroupCacheKeyWithRunId, Counter>
      failedWriteCountersById =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, FAILED_WRITE_COUNTERS_WITH_RUN_ID_CACHE_NAME, this::getFailedWriteCounterById);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKeyWithRunId, DistributionSummary>
      writeRowSizeDistributionSummariesWithRunId =
          CaffeineCacheFactory.createAsyncLoadingCache(
              vertx,
              WRITE_ROW_SIZE_DISTRIBUTION_SUMMARY_WITH_RUN_ID_CACHE_NAME,
              this::getWriteRowSizeDistributionSummaryById);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, DistributionSummary>
      readLatencyDistributionSummaries =
          CaffeineCacheFactory.createAsyncLoadingCache(
              vertx, READ_LATENCY_DISTRIBUTION_SUMMARY, this::getReadLatencyDistributionSummary);

  private Single<Counter> getSuccessReadCounter(CassandraFeatureGroupCacheKey key) {
    return Single.just(
        Counter.builder(MetricsEnum.SUCCESS_READ_COUNTERS.getMetricName())
            .description("features read success")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .register(meterRegistry));
  }

  private Single<Counter> getSuccessPartitionReadCounter(CassandraFeatureGroupCacheKey key) {
    return Single.just(
        Counter.builder(MetricsEnum.SUCCESS_PARTITION_READ_COUNTERS.getMetricName())
            .description("features read success")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .register(meterRegistry));
  }

  private Single<Counter> getFailedReadCounter(CassandraFeatureGroupCacheKey key) {
    return Single.just(
        Counter.builder(MetricsEnum.FAILED_READ_COUNTERS.getMetricName())
            .description("features read failures")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .register(meterRegistry));
  }

  private Single<Counter> getFailedPartitionReadCounter(CassandraFeatureGroupCacheKey key) {
    return Single.just(
        Counter.builder(MetricsEnum.FAILED_PARTITION_READ_COUNTERS.getMetricName())
            .description("features read failures")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .register(meterRegistry));
  }

  private Single<Counter> getSuccessWriteCounter(CassandraFeatureGroupCacheKey key) {
    return Single.just(
        Counter.builder(MetricsEnum.SUCCESS_WRITE_COUNTERS.getMetricName())
            .description("features write success")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .register(meterRegistry));
  }

  private Single<Counter> getFailedWriteCounter(CassandraFeatureGroupCacheKey key) {
    return Single.just(
        Counter.builder(MetricsEnum.FAILED_WRITE_COUNTERS.getMetricName())
            .description("features write failures")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .register(meterRegistry));
  }

  private Single<Counter> getSuccessWriteCounterById(CassandraFeatureGroupCacheKeyWithRunId key) {
    return Single.just(
        Counter.builder("feature-group.run.write.success")
            .description("features write success")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .tag("run-id", key.getRunId())
            .register(meterRegistry));
  }

  private Single<Counter> getFailedWriteCounterById(CassandraFeatureGroupCacheKeyWithRunId key) {
    return Single.just(
        Counter.builder("feature-group.run.write.failed")
            .description("features write failures")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .tag("run-id", key.getRunId())
            .register(meterRegistry));
  }

  private Single<DistributionSummary> getReadRowSizeDistributionSummary(
      CassandraFeatureGroupCacheKey key) {
    return Single.just(
        DistributionSummary.builder(MetricsEnum.READ_ROW_SIZE_DISTRIBUTION_SUMMARY.getMetricName())
            .description("features read row size")
            .baseUnit("bytes")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .publishPercentiles(0.99)
            .register(meterRegistry));
  }

  private Single<DistributionSummary> getPartitionReadRowSizeDistributionSummary(
      CassandraFeatureGroupCacheKey key) {
    return Single.just(
        DistributionSummary.builder(
                MetricsEnum.PARTITION_READ_ROW_SIZE_DISTRIBUTION_SUMMARY.getMetricName())
            .description("features read row size")
            .baseUnit("bytes")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .publishPercentiles(0.99)
            .register(meterRegistry));
  }

  private Single<DistributionSummary> getWriteRowSizeDistributionSummary(
      CassandraFeatureGroupCacheKey key) {
    return Single.just(
        DistributionSummary.builder(MetricsEnum.WRITE_ROW_SIZE_DISTRIBUTION_SUMMARY.getMetricName())
            .description("features write row size")
            .baseUnit("bytes")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .publishPercentiles(0.99)
            .register(meterRegistry));
  }

  private Single<DistributionSummary> getWriteRowSizeDistributionSummaryById(
      CassandraFeatureGroupCacheKeyWithRunId key) {
    return Single.just(
        DistributionSummary.builder("feature-group.run.write.row.size")
            .description("features write row size")
            .baseUnit("bytes")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .tag("run-id", key.getRunId())
            .publishPercentiles(0.99)
            .register(meterRegistry));
  }

  private Single<DistributionSummary> getReadLatencyDistributionSummary(
      CassandraFeatureGroupCacheKey key) {
    return Single.just(
        DistributionSummary.builder("feature-group.read.latency")
            .description("features read latency")
            .baseUnit("milliseconds")
            .tag("feature-group-name", key.getName())
            .tag("feature-group-version", key.getVersion())
            .publishPercentiles(0.99, 0.95)
            .register(meterRegistry));
  }

  public void processCassandraFeatureGroupReadResponse(ReadCassandraFeaturesResponse response) {
    CassandraFeatureGroupCacheKey key =
        CassandraFeatureGroupCacheKey.builder()
            .name(response.getFeatureGroupName())
            .version(response.getFeatureGroupVersion())
            .build();

    Completable successReadCounter =
        CompletableFutureUtils.toSingle(successReadCounters.get(key))
            .flatMapCompletable(
                counter -> incrementCounter(counter, response.getSuccessfulKeys().size()));

    Completable failedReadCounter =
        CompletableFutureUtils.toSingle(failedReadCounters.get(key))
            .flatMapCompletable(
                counter -> incrementCounter(counter, response.getFailedKeys().size()));

    Completable readThroughputCounter =
        CompletableFutureUtils.toSingle(readRowSizeDistributionSummaries.get(key))
            .flatMapCompletable(
                summary -> addToDistributionSummary(summary, response.getPayloadSizes()));

    Completable.mergeArray(successReadCounter, failedReadCounter, readThroughputCounter)
        .subscribe();
  }

  public void processCassandraFeatureGroupWriteResponseWithRunId(
      WriteCassandraFeaturesResponse response, String runId) {
    CassandraFeatureGroupCacheKeyWithRunId key =
        CassandraFeatureGroupCacheKeyWithRunId.builder()
            .name(response.getFeatureGroupName())
            .version(response.getFeatureGroupVersion())
            .runId(runId)
            .build();
    Completable successWriteCounterById =
        CompletableFutureUtils.toSingle(successWriteCountersById.get(key))
            .flatMapCompletable(
                counter -> incrementCounter(counter, response.getSuccessfulRows().size()));

    Completable failedWriteCounterById =
        CompletableFutureUtils.toSingle(failedWriteCountersById.get(key))
            .flatMapCompletable(
                counter -> incrementCounter(counter, response.getFailedRows().size()));

    Completable writeThroughputCounter =
        CompletableFutureUtils.toSingle(writeRowSizeDistributionSummariesWithRunId.get(key))
            .flatMapCompletable(
                summary -> addToDistributionSummary(summary, response.getPayloadSizes()));

    Completable.mergeArray(successWriteCounterById, failedWriteCounterById, writeThroughputCounter)
        .subscribe();
  }

  public void processCassandraFeatureGroupPartitionReadResponse(
      ReadCassandraPartitionResponse response) {
    CassandraFeatureGroupCacheKey key =
        CassandraFeatureGroupCacheKey.builder()
            .name(response.getFeatureGroupName())
            .version(response.getFeatureGroupVersion())
            .build();

    boolean success = !response.getFeatures().isEmpty();

    Completable successReadCounter =
        success
            ? CompletableFutureUtils.toSingle(successPartitionReadCounters.get(key))
            .flatMapCompletable(counter -> incrementCounter(counter, 1))
            : Completable.complete();

    Completable failedReadCounter =
        !success
            ? CompletableFutureUtils.toSingle(failedPartitionReadCounters.get(key))
            .flatMapCompletable(counter -> incrementCounter(counter, 1))
            : Completable.complete();

    long readSize = response.getPayloadSizes().stream().mapToLong(Long::longValue).sum();

    Completable readThroughputCounter =
        CompletableFutureUtils.toSingle(partitionReadRowSizeDistributionSummaries.get(key))
            .flatMapCompletable(summary -> addToDistributionSummary(summary, List.of(readSize)));

    Completable.mergeArray(successReadCounter, failedReadCounter, readThroughputCounter)
        .subscribe();
  }

  public void processCassandraFeatureGroupWriteResponse(WriteCassandraFeaturesResponse response) {
    CassandraFeatureGroupCacheKey key =
        CassandraFeatureGroupCacheKey.builder()
            .name(response.getFeatureGroupName())
            .version(response.getFeatureGroupVersion())
            .build();

    Completable successWriteCounter =
        CompletableFutureUtils.toSingle(successWriteCounters.get(key))
            .flatMapCompletable(
                counter -> incrementCounter(counter, response.getSuccessfulRows().size()));

    Completable failedWriteCounter =
        CompletableFutureUtils.toSingle(failedWriteCounters.get(key))
            .flatMapCompletable(
                counter -> incrementCounter(counter, response.getFailedRows().size()));

    Completable writeThroughputCounter =
        CompletableFutureUtils.toSingle(writeRowSizeDistributionSummaries.get(key))
            .flatMapCompletable(
                summary -> addToDistributionSummary(summary, response.getPayloadSizes()));

    Completable.mergeArray(successWriteCounter, failedWriteCounter, writeThroughputCounter)
        .subscribe();
  }

  public void registerCassandraFeatureGroupReadLatency(
      CassandraFeatureGroupCacheKey key, Long latency) {
    CompletableFutureUtils.toSingle(readLatencyDistributionSummaries.get(key))
        .flatMapCompletable(summary -> addToDistributionSummary(summary, latency))
        .subscribe();
  }

  private Completable incrementCounter(Counter counter, long value) {
    return Completable.create(
        emitter -> {
          try {
            counter.increment(value);
            emitter.onComplete();
          } catch (Exception e) {
            log.error(e.getMessage(), e);
            emitter.onError(e);
          }
        });
  }

  private Completable addToDistributionSummary(
      DistributionSummary distributionSummary, long value) {
    return Completable.create(
        emitter -> {
          try {
            distributionSummary.record(value);
            emitter.onComplete();
          } catch (Exception e) {
            log.error(e.getMessage(), e);
            emitter.onError(e);
          }
        });
  }

  private Completable addToDistributionSummary(
      DistributionSummary distributionSummary, List<Long> values) {
    return Completable.create(
        emitter -> {
          try {
            values.forEach(distributionSummary::record);
            emitter.onComplete();
          } catch (Exception e) {
            log.error(e.getMessage(), e);
            emitter.onError(e);
          }
        });
  }

  public Map<MetricsEnum, AsyncLoadingCache<CassandraFeatureGroupCacheKey, Counter>> getAllCounterMetricsCacheMap() {
    return Map.of(
        MetricsEnum.SUCCESS_READ_COUNTERS, successReadCounters,
        MetricsEnum.FAILED_READ_COUNTERS, failedReadCounters,
        MetricsEnum.SUCCESS_PARTITION_READ_COUNTERS, successPartitionReadCounters,
        MetricsEnum.FAILED_PARTITION_READ_COUNTERS, failedPartitionReadCounters,
        MetricsEnum.SUCCESS_WRITE_COUNTERS, successWriteCounters,
        MetricsEnum.FAILED_WRITE_COUNTERS, failedWriteCounters
    );
  }
  public Map<MetricsEnum, AsyncLoadingCache<CassandraFeatureGroupCacheKey, DistributionSummary>> getAllDistributionMetricsCacheMap() {
    return Map.of(
        MetricsEnum.READ_ROW_SIZE_DISTRIBUTION_SUMMARY, readRowSizeDistributionSummaries,
        MetricsEnum.PARTITION_READ_ROW_SIZE_DISTRIBUTION_SUMMARY, partitionReadRowSizeDistributionSummaries,
        MetricsEnum.WRITE_ROW_SIZE_DISTRIBUTION_SUMMARY, writeRowSizeDistributionSummaries
    );
  }

}
