package com.dream11.admin.jobs;

import com.dream11.admin.dao.HealthCheckDao;
import com.dream11.admin.service.CassandraMetaStoreService;
import com.dream11.core.config.ApplicationConfig;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CacheUpdationJob {
  private final CassandraMetaStoreService cassandraMetaStoreService;
  private final ApplicationConfig applicationConfig;

  public Completable handle() {
    Long timestamp = System.currentTimeMillis() - applicationConfig.getCassandraMetaStoreCacheUpdationTime();

    List<Completable> completableList =
        List.of(
            cassandraMetaStoreService.refreshUpdatedCassandraEntities(timestamp),
            cassandraMetaStoreService.refreshUpdatedCassandraFeatureGroups(timestamp),
            cassandraMetaStoreService.refreshUpdatedCassandraFeatureGroupVersions(timestamp));
    return Completable.merge(completableList);
  }
}