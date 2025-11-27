package com.dream11.populator.service;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.dto.helper.Data;
import com.dream11.core.dto.populator.PopulatorGroupMetadata;
import com.dream11.populator.deltareader.TableReader;
import com.dream11.webclient.reactivex.client.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControllerService {
  private static final ConcurrentHashSet<String> localConsumers = new ConcurrentHashSet<>();

  private final ObjectMapper objectMapper = AppContext.getInstance(ObjectMapper.class);
  private final ApplicationConfig applicationConfig =
      AppContext.getInstance(ApplicationConfig.class);
  private final Vertx vertx;
  private final TableReader tableReader;
  private final WebClient webClient;
  private final ClusterManager clusterManager;

  private final WorkerMetricService workerMetricService =
      AppContext.getInstance(WorkerMetricService.class);
  private Long controllerPollerTimerId;

  @Getter private ConcurrentHashMap<String, Integer> workerConfigMap = new ConcurrentHashMap<>();

  public ControllerService(Vertx vertx, WebClient webClient, ClusterManager clusterManager) {
    this.vertx = vertx;
    this.tableReader = ContextUtils.getInstance(TableReader.class);
    this.webClient = webClient;
    this.clusterManager = clusterManager;
  }

  public static ControllerService create(
      Vertx vertx, WebClient webClient, ClusterManager clusterManager) {
    return new ControllerService(vertx, webClient, clusterManager);
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
    return getConsumerGroupsMetadataFromAdmin()
        .flatMap(
            newConfigs ->
                clusterManager
                    .rxGetAllResources()
                    .flatMap(
                        currentResources -> {
                          Set<String> currentSet = new HashSet<>(currentResources);
                          newConfigs.stream()
                              .map(PopulatorGroupMetadata::getTenantName)
                              .collect(Collectors.toList())
                              .forEach(currentSet::remove);
                          return Observable.fromIterable(currentSet)
                              .flatMapCompletable(clusterManager::rxDropResource)
                              .andThen(Single.just(newConfigs));
                        }))
        .flattenAsObservable(r -> r)
        .flatMapCompletable(
            r -> {
              workerConfigMap.put(r.getTenantName(), r.getNumWorkers());
              return clusterManager.rxAddResource(r.getTenantName(), r.getNumWorkers());
            })
        .doOnComplete(() -> {
          workerMetricService.setRequiredAssignmentsGauge(this.workerConfigMap);
        });
  }

  private Maybe<Boolean> checkIfLeader() {
    return clusterManager.rxIsLeader().filter(r -> r);
  }

  public Single<List<PopulatorGroupMetadata>> getConsumerGroupsMetadataFromAdmin() {
    return webClient
        .getWebClient()
        .getAbs(
            applicationConfig.getOfsAdminHost()
                + applicationConfig.getOfsAdminPopulatorMetadataEndpoint())
        .rxSend()
        .map(
            r ->
                objectMapper.readValue(
                    r.bodyAsJsonObject().toString(),
                    new TypeReference<Data<List<PopulatorGroupMetadata>>>() {}))
        .map(Data::getData);
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
        .<String>rxSend(consumerId, WorkerMailBox.HEALTHCHECK_MESSAGE)
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
