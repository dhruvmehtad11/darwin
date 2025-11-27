package com.dream11.consumer.service;

import static com.dream11.consumer.constant.Constants.FAILED_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME;
import static com.dream11.consumer.constant.Constants.SUCCESS_MESSAGE_PROCESSOR_COUNTER_CACHE_NAME;

import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.core.constant.MetricsEnum;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKey;
import com.dream11.core.dto.helper.cachekeys.ConsumerCacheKey;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.inject.Inject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerMetricService {
  private final PrometheusMeterRegistry meterRegistry =
      (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();
  private final StatsDClient nonBlockingStatsDClient = AppContext.getInstance(StatsDClient.class);
  private final Vertx vertx = AppContext.getInstance(Vertx.class);
  private final AsyncLoadingCache<ConsumerCacheKey, Counter> successMessageCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, MetricsEnum.CONSUMER_SUCCESS_MESSAGE_PROCESSOR_COUNTER.getMetricCacheKey(), this::getSuccessMessageCounter);
  private final AsyncLoadingCache<ConsumerCacheKey, Counter> failedMessageCounters =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, MetricsEnum.CONSUMER_FAILED_MESSAGE_PROCESSOR_COUNTER.getMetricCacheKey(), this::getFailedMessageCounter);

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
      } else {
        updateMainMapFromRefIfExistZeroIfNot(consumerEntry.getValue(), new HashMap<>());
      }
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
        Gauge.builder("consumer.assigned", entry.getValue(), AtomicInteger::get)
            .description("consumer assigned")
            .tag("consumer-id", consumerId)
            .tag("topic-name", entry.getKey())
            .register(meterRegistry);
      }
    }
  }

  public void setRequiredAssignmentsGauge(Map<String, Integer> requiredAssignmentMap) {
    updateMainMapFromRefIfExistZeroIfNot(this.requiredAssignmentMap, requiredAssignmentMap);

    for (String topicName : this.requiredAssignmentMap.keySet()) {
      Gauge.builder(
              "consumer.required", this.requiredAssignmentMap.get(topicName), AtomicInteger::get)
          .description("consumer required")
          .tag("topic-name", topicName)
          .register(meterRegistry);
    }
  }

  private Single<Counter> getSuccessMessageCounter(ConsumerCacheKey key) {
    Counter prometheusCounter = Counter.builder(MetricsEnum.CONSUMER_SUCCESS_MESSAGE_PROCESSOR_COUNTER.getMetricName())
        .description("consumer write success")
        .tag("consumer-id", key.getConsumerId())
        .tag("topic-name", key.getTopicName())
        .register(meterRegistry);
    return Single.just(prometheusCounter);
  }

  private Single<Counter> getFailedMessageCounter(ConsumerCacheKey key) {
    Counter prometheusCounter = Counter.builder(MetricsEnum.CONSUMER_FAILED_MESSAGE_PROCESSOR_COUNTER.getMetricName())
        .description("consumer write failed")
        .tag("consumer-id", key.getConsumerId())
        .tag("topic-name", key.getTopicName())
        .register(meterRegistry);

    return Single.just(prometheusCounter);
  }

  private Single<Counter> getDlqMessageCounter(ConsumerCacheKey key) {
    Counter prometheusCounter = Counter.builder("consumer.write.failed")
        .description("features write failures")
        .tag("consumer-id", key.getConsumerId())
        .tag("topic-name", key.getTopicName())
        .register(meterRegistry);
    return Single.just(prometheusCounter);
  }

  public void processWriteMetrics(
      Integer successRecords, Integer failedRecords, String consumerId, String topicName) {
    ConsumerCacheKey key =
        ConsumerCacheKey.builder().consumerId(consumerId).topicName(topicName).build();

    Completable successWriteCounter =
        CompletableFutureUtils.toSingle(successMessageCounters.get(key))
            .flatMapCompletable(counter -> incrementCounter(counter, successRecords)
                .andThen(sendCounterMetricToDd(MetricsEnum.CONSUMER_SUCCESS_MESSAGE_PROCESSOR_COUNTER.getMetricCacheKey(), key, counter)));

    Completable failedWriteCounter =
        CompletableFutureUtils.toSingle(failedMessageCounters.get(key))
            .flatMapCompletable(counter -> incrementCounter(counter, failedRecords).andThen(
                sendCounterMetricToDd(MetricsEnum.CONSUMER_FAILED_MESSAGE_PROCESSOR_COUNTER.getMetricCacheKey(), key, counter)));

    Completable.mergeArray(successWriteCounter, failedWriteCounter).subscribe();
  }

  public void incrementDlqFailedMessageCounters(ConsumerCacheKey key) {
    CompletableFutureUtils.toSingle(failedMessageCounters.get(key))
        .flatMapCompletable(counter -> incrementCounter(counter, 1L)).subscribe();
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

  private Completable sendCounterMetricToDd(String cacheKey, ConsumerCacheKey key, Counter counter) {
    return Completable.create(
        emitter -> {
          try {
            nonBlockingStatsDClient.count(
                MetricsEnum.getMetricNameFromCacheKey(cacheKey), counter.count(), getConsumerTags(key));
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
      if (!mainMap.containsKey(entry.getKey())) {
        mainMap.put(entry.getKey(), new AtomicInteger(entry.getValue()));
      }
    }
  }

  private static ConcurrentHashMap<String, AtomicInteger> copyMapToConcurrent(Map<String, Integer> referenceMap) {
    ConcurrentHashMap<String, AtomicInteger> topicMap = new ConcurrentHashMap<>();
    for (Map.Entry<String, Integer> topicEntry : referenceMap.entrySet()) {
      topicMap.put(topicEntry.getKey(), new AtomicInteger(topicEntry.getValue()));
    }
    return topicMap;
  }

  public String[] getConsumerTags(ConsumerCacheKey key) {
    return new String[] {"consumer-id:" + key.getConsumerId(), "topic-name:" + key.getTopicName()};
  }

}
