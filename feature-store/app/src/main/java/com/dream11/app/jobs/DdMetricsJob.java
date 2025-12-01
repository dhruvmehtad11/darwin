package com.dream11.app.jobs;

import com.dream11.app.service.FeatureGroupMetricService;
import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.core.constant.MetricsEnum;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKey;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.inject.Inject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class DdMetricsJob {
  private final StatsDClient nonBlockingStatsDClient;
  private final FeatureGroupMetricService featureGroupMetricService;

  public String[] getFeatureGroupTags(CassandraFeatureGroupCacheKey key) {
    return new String[] {"feature-group-name:" + key.getName(), "feature-group-version:" + key.getVersion()};
  }

  public void sendCounterMetric(String cacheKey, CassandraFeatureGroupCacheKey key, Counter counter) {
    nonBlockingStatsDClient.count(MetricsEnum.getMetricNameFromCacheKey(cacheKey), counter.count(), getFeatureGroupTags(key));
  }

  public Completable pushMetricsToDD() {
    featureGroupMetricService.getAllCounterMetricsCacheMap()
        .forEach((metricEnum, metricCache) -> metricCache.asMap().entrySet()
            .forEach(cacheValue ->
                CompletableFutureUtils.toSingle(cacheValue.getValue()).doOnSuccess(counter ->
                    sendCounterMetric(metricEnum.getMetricName(), cacheValue.getKey(), counter)).subscribe()));


    return Completable.complete();
  }

}
