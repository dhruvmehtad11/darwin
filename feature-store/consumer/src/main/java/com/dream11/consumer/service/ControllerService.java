package com.dream11.consumer.service;

import com.dream11.common.app.AppContext;
import com.dream11.consumer.resourceautoscaler.ResourceAutoScaler;
import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControllerService {
  private static final ConcurrentHashSet<String> localConsumers = new ConcurrentHashSet<>();
  private final Vertx vertx;
  private final ClusterManager clusterManager;
  private final ConsumerMetricService consumerMetricService =
      AppContext.getInstance(ConsumerMetricService.class);
  private final AdminClientService adminClientService =
      AppContext.getInstance(AdminClientService.class);
  private ResourceAutoScaler resourceAutoScaler;
  private Long controllerPollerTimerId;

  @Getter private Map<String, Integer> consumerConfigMap = new HashMap<>();

  public ControllerService(Vertx vertx, ClusterManager clusterManager) {
    this.vertx = vertx;
    this.clusterManager = clusterManager;
  }

  // todo fix this for all corner cases
  private Completable handleResourceScaling() {
    Single<Integer> required = resourceAutoScaler.getRequiredCapacity(consumerConfigMap);
    Single<Integer> current = resourceAutoScaler.getCurrentCapacity();

    return Single.zip(
            current,
            required,
            (a, b) ->
                ResourceAutoScaler.ScalingHelper.builder()
                    .current(a)
                    .required(b)
                    .scalingFlag(a.compareTo(b))
                    .build())
        .flatMapCompletable(
            r -> {
              if (r.getScalingFlag() < 0) {
                return resourceAutoScaler.upscale(r.getCurrent(), r.getRequired());
              }
              if (r.getScalingFlag() > 0) {
                return resourceAutoScaler.downscale(
                    r.getCurrent(), r.getRequired(), getLocalNodes());
              }
              return Completable.complete();
            });
  }

  public static ControllerService create(Vertx vertx, ClusterManager clusterManager) {
    return new ControllerService(vertx, clusterManager);
  }

  public void start() {
    this.startControllerPoller();
    clusterManager.addLiveInstanceChangeHandlers(
        li ->
            checkIfLeader()
                .flatMapCompletable(
                    ignore -> clusterManager.rxPurgeAndRebalance().andThen(getAndSetConfig()))
                .subscribe());
  }

  private void startControllerPoller() {
    controllerPollerTimerId =
        vertx.setPeriodic(
            30_000,
            timer ->
                checkIfLeader()
                    .flatMapCompletable(
                        ignore -> clusterManager.rxPurgeAndRebalance().andThen(getAndSetConfig()))
                    .subscribe());
  }

  private Completable getAndSetConfig() {
    return adminClientService
        .getConsumerGroupsMetadataFromAdmin()
        .flatMap(
            newConfigs ->
                clusterManager
                    .rxGetAllResources()
                    .flatMap(
                        currentResources -> {
                          Set<String> currentSet = new HashSet<>(currentResources);
                          newConfigs.stream()
                              .map(ConsumerGroupMetadata::getTopicName)
                              .collect(Collectors.toList())
                              .forEach(currentSet::remove);
                          return Observable.fromIterable(currentSet)
                              .flatMapCompletable(clusterManager::rxDropResource)
                              .andThen(Single.just(newConfigs));
                        }))
        .flattenAsObservable(r -> r)
        .flatMapCompletable(
            r -> {
              consumerConfigMap.put(r.getTopicName(), r.getNumConsumers());
              return clusterManager.rxAddResource(r.getTopicName(), r.getNumConsumers());
            })
        .doOnComplete(
            () -> consumerMetricService.setRequiredAssignmentsGauge(this.consumerConfigMap));
  }

  private Maybe<Boolean> checkIfLeader() {
    return clusterManager.rxIsLeader().filter(r -> r);
  }

  public Single<Map<String, String>> consumerHealthcheck() {
    return Observable.fromIterable(getLocalNodes())
        .flatMap(this::sendHealthcheckMessageToMailbox)
        .toMap(Map.Entry::getKey, Map.Entry::getValue)
        .map(r -> r);
  }

  private Observable<AbstractMap.SimpleEntry<String, String>> sendHealthcheckMessageToMailbox(
      String consumerId) {
    return vertx
        .eventBus()
        .<String>rxSend(consumerId, ConsumerMailBox.HEALTHCHECK_MESSAGE)
        .map(Message::body)
        .map(healthy -> new AbstractMap.SimpleEntry<>(consumerId, healthy))
        .toObservable();
  }

  public Set<String> getLocalNodes() {
    return new HashSet<>(localConsumers);
  }

  public static void addConsumerId(String consumerId) {
    localConsumers.add(consumerId);
  }

  public static void removeConsumerId(String consumerId) {
    localConsumers.remove(consumerId);
  }

  public ClusterManager getClusterManager() {
    return clusterManager;
  }
}
