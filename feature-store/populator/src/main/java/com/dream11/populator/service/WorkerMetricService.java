package com.dream11.populator.service;

import static com.dream11.populator.constant.Constants.FAILED_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME;
import static com.dream11.populator.constant.Constants.SUCCESS_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME;

import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.dto.helper.cachekeys.WorkerCacheKey;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerMetricService {
  // todo: table and file processing metrics
  private final PrometheusMeterRegistry meterRegistry =
      (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
  private final Vertx vertx = AppContext.getInstance(Vertx.class);
  private final AsyncLoadingCache<WorkerCacheKey, Counter> successMessageCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, SUCCESS_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME, this::getSuccessMessageCounter);
  private final AsyncLoadingCache<WorkerCacheKey, Counter> failedMessageCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, FAILED_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME, this::getFailedMessageCounter);

  private final ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>> assignmentMap =
      new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, AtomicInteger> requiredAssignmentMap =
      new ConcurrentHashMap<>();

  public void setAssignmentsGauge(Map<String, Map<String, Integer>> assignmentMap) {
    for (Map.Entry<String, ConcurrentHashMap<String, AtomicInteger>> consumerEntry :
        this.assignmentMap.entrySet()) {
      // consumer-id map is present in new map
      if (assignmentMap.containsKey(consumerEntry.getKey())) {
        updateMainMapFromRefIfExistZeroIfNot(
            consumerEntry.getValue(), assignmentMap.get(consumerEntry.getKey()));
      } else updateMainMapFromRefIfExistZeroIfNot(consumerEntry.getValue(), new HashMap<>());
    }

    for (Map.Entry<String, Map<String, Integer>> entry : assignmentMap.entrySet()) {
      // new map has values that are not present in main map
      if (!this.assignmentMap.containsKey(entry.getKey())) {
        ConcurrentHashMap<String, AtomicInteger> topicMap = copyMapToConcurrent(entry.getValue());
        this.assignmentMap.put(entry.getKey(), topicMap);
      }
    }

    for (String consumerId : this.assignmentMap.keySet()) {
      for (Map.Entry<String, AtomicInteger> entry : this.assignmentMap.get(consumerId).entrySet()) {
        Gauge.builder("populator.worker.assigned", entry.getValue(), AtomicInteger::get)
            .description("consumer assigned")
            .tag("worker-id", consumerId)
            .tag("tenant-name", entry.getKey())
            .register(meterRegistry);
      }
    }
  }

  public void setRequiredAssignmentsGauge(Map<String, Integer> requiredAssignmentMap) {
    updateMainMapFromRefIfExistZeroIfNot(this.requiredAssignmentMap, requiredAssignmentMap);

    for (String topicName : this.requiredAssignmentMap.keySet()) {
      Gauge.builder(
              "populator.worker.required", this.requiredAssignmentMap.get(topicName), AtomicInteger::get)
          .description("workers required")
          .tag("tenant-name", topicName)
          .register(meterRegistry);
    }
  }

  private Single<Counter> getSuccessMessageCounter(WorkerCacheKey key) {
    return Single.just(
        Counter.builder("populator.worker.write.success")
            .description("consumer write success")
            .tag("worker-id", key.getWorkerId())
            .tag("table-name", key.getTableName())
            .register(meterRegistry));
  }

  private Single<Counter> getFailedMessageCounter(WorkerCacheKey key) {
    return Single.just(
        Counter.builder("populator.worker.write.failed")
            .description("consumer write failed")
            .tag("worker-id", key.getWorkerId())
            .tag("table-name", key.getTableName())
            .register(meterRegistry));
  }

  public void processWriteMetrics(
      Integer successRecords, Integer failedRecords, String workerId, String tableName) {
    WorkerCacheKey key =
        WorkerCacheKey.builder().workerId(workerId).tableName(tableName).build();

    Completable successWriteCounter =
        CompletableFutureUtils.toSingle(successMessageCounters.get(key))
            .flatMapCompletable(counter -> incrementCounter(counter, successRecords));

    Completable failedWriteCounter =
        CompletableFutureUtils.toSingle(failedMessageCounters.get(key))
            .flatMapCompletable(counter -> incrementCounter(counter, failedRecords));

    Completable.mergeArray(successWriteCounter, failedWriteCounter).subscribe();
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

  private static void updateMainMapFromRefIfExistZeroIfNot(
      Map<String, AtomicInteger> mainMap, Map<String, Integer> referenceMap) {
    for (Map.Entry<String, AtomicInteger> entry : mainMap.entrySet()) {
      // updating keys present in main map
      entry.getValue().set(referenceMap.getOrDefault(entry.getKey(), 0));
    }

    for (Map.Entry<String, Integer> entry : referenceMap.entrySet()) {
      // adding keys that are absent in main map
      if (!mainMap.containsKey(entry.getKey()))
        mainMap.put(entry.getKey(), new AtomicInteger(entry.getValue()));
    }
  }

  private static ConcurrentHashMap<String, AtomicInteger> copyMapToConcurrent(Map<String, Integer> referenceMap){
    ConcurrentHashMap<String, AtomicInteger> topicMap = new ConcurrentHashMap<>();
    for (Map.Entry<String, Integer> topicEntry : referenceMap.entrySet())
      topicMap.put(topicEntry.getKey(), new AtomicInteger(topicEntry.getValue()));
    return topicMap;
  }
}
