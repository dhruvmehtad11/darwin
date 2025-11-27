package com.dream11.consumer.service;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.google.inject.Inject;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.kafka.KafkaConsumerMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsumerManager {
  private Long consumerWatcherId = null;
  private final Vertx vertx = AppContext.getInstance(Vertx.class);

  @SuppressWarnings("unchecked")
  private final KafkaConsumer<String, String> consumer =
      ContextUtils.getInstance(KafkaConsumer.class);

  private final ConsumerProcessor consumerProcessor =
      AppContext.getInstance(ConsumerProcessor.class);
  private String consumerId;
  private ClusterManager clusterManager;
  private Set<String> currentAssignments = new HashSet<>();
  private final ConsumerMetricService consumerMetricService = AppContext.getInstance(ConsumerMetricService.class);

  public ConsumerManager(String consumerId, ClusterManager clusterManager) {
    this.consumerId = consumerId;
    this.clusterManager = clusterManager;
    this.consumerProcessor.setConsumerId(consumerId);

    new KafkaConsumerMetrics(List.of(Tag.of("client.id", consumerId)))
        .bindTo(BackendRegistries.getDefaultNow());
  }

  public static ConsumerManager create(String consumerId, ClusterManager clusterManager) {
    return new ConsumerManager(consumerId, clusterManager);
  }

  public Completable rxStart() {
    return getTopics()
        .flatMapCompletable(consumerProcessor::rxStart)
        .doOnComplete(this::startConsumerWatcher)
        .doOnError(error -> log.error("Failed to initialize consumer", error));
  }

  public Completable rxStop() {
    if (consumerWatcherId != null) {
      vertx.cancelTimer(consumerWatcherId);
    }
    return consumer.rxClose();
  }

  private void startConsumerWatcher() {
    consumer.partitionsRevokedHandler(
        partitions -> {
          log.info(
              String.format("consumer %s unsubscribed from topics: %s", consumerId, partitions));
          clusterManager
              .rxGetAssignmentMap()
              .map(r -> r.get(consumerId).keySet())
              .flatMapCompletable(consumerProcessor::reSubscribe)
              .doOnComplete(
                  () -> log.info(String.format("Consumer %s reSubscribed to topics", consumerId)))
              .doOnError(
                  error -> log.error("Error restarting consumer after partitions revoked", error))
              .subscribe();
        });

    consumer.partitionsAssignedHandler(
        partitions -> {
          log.info(String.format("consumer %s assigned to topics: %s", consumerId, partitions));
        });

    consumer.exceptionHandler(e -> {
      log.error("consumer error handler invoked: ", e);
    });

    this.consumerWatcherId =
        vertx.setPeriodic(
            5_000,
            timerId -> {
              getTopics()
                  .filter(r -> !Objects.equals(r, currentAssignments))
                  .flatMapCompletable(
                      r ->
                          consumerProcessor
                              .reSubscribe(r)
                              .doOnComplete(
                                  () -> {
                                    currentAssignments = r;
                                    log.info(
                                        String.format(
                                            "Consumer %s reSubscribed to topics", consumerId));
                                  })
                              .doOnError(
                                  error -> log.error("Error during config change handling", error)))
                  .subscribe();
            });
  }

  private Single<Set<String>> getTopics() {
    return clusterManager.rxGetAssignmentMap()
        .doOnSuccess(consumerMetricService::setAssignmentsGauge)
        .filter(r -> r.containsKey(consumerId))
        .map(r -> r.get(consumerId).keySet())
        .switchIfEmpty(Single.just(new HashSet<>()));
  }
}
