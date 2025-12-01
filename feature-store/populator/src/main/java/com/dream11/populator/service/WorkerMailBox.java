package com.dream11.populator.service;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkerMailBox {
  public static final String HEALTHCHECK_MESSAGE = "HEALTHCHECK_MESSAGE";
  public static final String KILL_VERTICLE_MESSAGE = "KILL_VERTICLE_MESSAGE";

  private final Vertx vertx;
  private final ClusterManager clusterManager;

  public WorkerMailBox(Vertx vertx, ClusterManager clusterManager) {
    this.vertx = vertx;
    this.clusterManager = clusterManager;
  }

  public static WorkerMailBox create(Vertx vertx, ClusterManager clusterManager) {
    return new WorkerMailBox(vertx, clusterManager);
  }

  public void start(String consumerId, Supplier<Completable> killFunction) {
    vertx
        .eventBus()
        .<String>consumer(
            consumerId,
            message -> {
              switch (message.body()) {
                case (HEALTHCHECK_MESSAGE):
                  healthcheckHandler(message);
                  break;
                case (KILL_VERTICLE_MESSAGE):
                  killFunctionHandler(message, killFunction);
                  break;
                default:
                  throw new RuntimeException(
                      String.format(
                          "message handler not implemented for message: %s", message.body()));
              }
            });
  }

  private void killFunctionHandler(Message<String> message, Supplier<Completable> killFunction) {
    killFunction.get()
        .doOnSubscribe(ignore -> message.rxReply(ConsumerMessage.UN_DEPLOYED).subscribe())
        .subscribe();
  }

  private void healthcheckHandler(Message<String> message) {
    clusterManager
        .rxHealthy()
        .map(r -> r ? ConsumerMessage.HEALTHY.name() : ConsumerMessage.UNHEALTHY.name())
        .onErrorResumeNext(e -> Single.just(ConsumerMessage.UNHEALTHY.name()))
        .flatMap(message::rxReply)
        .ignoreElement()
        .subscribe();
  }

  public enum ConsumerMessage {
    HEALTHY,
    UNHEALTHY,
    UN_DEPLOYED
  }
}
