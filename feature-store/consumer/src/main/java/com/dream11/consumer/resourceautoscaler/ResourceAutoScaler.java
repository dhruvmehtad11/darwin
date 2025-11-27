package com.dream11.consumer.resourceautoscaler;

import com.dream11.consumer.service.ClusterManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public abstract class ResourceAutoScaler {
  private Vertx vertx;
  private ClusterManager clusterManager;

  public ResourceAutoScaler(Vertx vertx, ClusterManager clusterManager) {
    this.vertx = vertx;
    this.clusterManager = clusterManager;
  }

  public Single<Integer> getCurrentCapacity() {
    return clusterManager.rxGetLiveInstances().map(Set::size);
  }

  public Single<Integer> getRequiredCapacity(Map<String, Integer> consumerConfigMap) {
    return Single.just(consumerConfigMap.values().stream().reduce(0, Integer::sum));
  }

  // to keep a limit on adding nodes at once
  public abstract Completable upscale(Integer current, Integer required);

  public abstract Completable downscale(Integer current, Integer required, Set<String> localNodeIds);

  @Data
  @Builder
  public static class ScalingHelper {
    private Integer current;
    private Integer required;
    private Integer scalingFlag;
  }
}
