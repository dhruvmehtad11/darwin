package com.dream11.app.dao;

import static com.dream11.core.constant.Constants.CASSANDRA_CLIENT_FACTORY_CACHE;

import com.dream11.app.dao.featuredao.CassandraFeatureDao;
import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.util.CassandraClientUtils;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.cassandra.CassandraClient;
import io.vertx.reactivex.core.Vertx;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraDaoFactory {
  private final Vertx vertx = AppContext.getInstance(Vertx.class);
  private final ApplicationConfig applicationConfig =
      AppContext.getInstance(ApplicationConfig.class);
  private final AsyncLoadingCache<String, CassandraFeatureDao> factory =
      CaffeineCacheFactory.createAsyncLoadingCache(
          vertx, CASSANDRA_CLIENT_FACTORY_CACHE, this::createDao);

  public Single<CassandraFeatureDao> createDao(String key) {
    return Single.just(
        new CassandraFeatureDao(
            CassandraClientUtils.createClient(
                vertx, applicationConfig.getGenericCassandraHost(), key),
            key));
  }

  public Single<CassandraFeatureDao> getDao(String key) {
    return CompletableFutureUtils.toSingle(factory.get(key));
  }

  public Completable close() {
    return Completable.merge(
        factory.asMap().values().stream()
            .map(
                r ->
                    Single.<CassandraClient>create(
                            emitter ->
                                r.whenComplete(
                                    (cassandraFeatureDao, throwable) -> {
                                      if (throwable != null) emitter.onError(throwable);
                                      else {
                                        emitter.onSuccess(cassandraFeatureDao.getCassandraClient());
                                      }
                                    }))
                        .flatMapCompletable(
                            client -> {
                              if (client.isConnected()) {
                                return client.rxClose();
                              }
                              return Completable.complete();
                            }))
            .collect(Collectors.toList()));
  }
}
