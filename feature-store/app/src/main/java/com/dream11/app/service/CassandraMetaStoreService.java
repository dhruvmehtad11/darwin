package com.dream11.app.service;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.error.ServiceError.*;
import static com.dream11.core.util.JsonConversionUtils.convertJsonSingle;

import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.helper.CassandraFeatureGroupEntityPair;
import com.dream11.core.dto.helper.cachekeys.CassandraEntityCacheKey;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKey;
import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.VersionMetadata;
import com.dream11.core.dto.response.GetCassandraFeatureGroupSchemaResponse;
import com.dream11.core.error.ApiRestException;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CassandraMetaStoreService {
  private final CassandraOfsV2AdminService cassandraOfsV2AdminService = AppContext.getInstance(CassandraOfsV2AdminService.class);

  private final AsyncLoadingCache<CassandraFeatureGroupCacheKey, CassandraFeatureGroupMetadata>
      featureGroupMetaDataCache =
          CaffeineCacheFactory.createAsyncLoadingCache(
              AppContext.getInstance(Vertx.class),
              FEATURE_GROUP_METADATA_CACHE,
              cassandraOfsV2AdminService::getCassandraFeatureGroupMetadata);

  private final AsyncLoadingCache<String, VersionMetadata> featureGroupVersionCache =
      CaffeineCacheFactory.createAsyncLoadingCache(
          AppContext.getInstance(Vertx.class),
          FEATURE_GROUP_VERSION_CACHE,
          cassandraOfsV2AdminService::getCassandraFeatureGroupLatestVersion);

  private final AsyncLoadingCache<CassandraEntityCacheKey, CassandraEntityMetadata>
      entityMetaDataCache =
          CaffeineCacheFactory.createAsyncLoadingCache(
              AppContext.getInstance(Vertx.class),
              ENTITY_METADATA_CACHE,
              cassandraOfsV2AdminService::getCassandraEntityMetadata);

  public Single<CassandraFeatureGroupMetadata> getCassandraFeatureGroupMetaData(
      String name, String version) {
    return getCassandraFeatureGroupMetaDataFromCache(name, version);
  }

  private Single<CassandraFeatureGroupMetadata> getCassandraFeatureGroupMetaDataFromCache(
      String name, String version) {
    Single<VersionMetadata> versionSingle;
    if (version == null) versionSingle = getCassandraFeatureGroupVersion(name);
    else versionSingle = Single.just(VersionMetadata.builder().latestVersion(version).build());

    return versionSingle.flatMap(
        versionToGet ->
            CompletableFutureUtils.toSingle(
                featureGroupMetaDataCache.get(
                    CassandraFeatureGroupCacheKey.builder()
                        .name(name)
                        .version(versionToGet.getLatestVersion())
                        .build())));
  }

  public Single<VersionMetadata> getCassandraFeatureGroupVersion(String name) {
    return getCassandraFeatureGroupVersionFromCache(name);
  }

  private Single<VersionMetadata> getCassandraFeatureGroupVersionFromCache(String name) {
    return CompletableFutureUtils.toSingle(featureGroupVersionCache.get(name))
        .filter(versionMetadata -> versionMetadata.getLatestVersion() != null)
        .switchIfEmpty(Single.error(new ApiRestException(FEATURE_GROUP_VERSION_NOT_FOUND_EXCEPTION)));
  }

  public Single<CassandraEntityMetadata> getCassandraEntityMetaData(String name) {
    return getCassandraEntityMetaDataFromCache(name);
  }

  private Single<CassandraEntityMetadata> getCassandraEntityMetaDataFromCache(String name) {
    return CompletableFutureUtils.toSingle(
        entityMetaDataCache.get(CassandraEntityCacheKey.builder().name(name).build()));
  }

  public Single<GetCassandraFeatureGroupSchemaResponse> getCassandraFeatureGroupSchema(
      String name, String version) {

    return getCassandraFeatureGroupEntityPair(name, version).map(this::getSchemaFromPair);
  }

  public GetCassandraFeatureGroupSchemaResponse getSchemaFromPair(
      CassandraFeatureGroupEntityPair cassandraFeatureGroupEntityPair) {
    List<CassandraFeatureColumn> features =
        new ArrayList<>(
            cassandraFeatureGroupEntityPair.getEntityMetadata().getEntity().getFeatures());
    features.addAll(
        cassandraFeatureGroupEntityPair.getFeatureGroupMetadata().getFeatureGroup().getFeatures());

    return GetCassandraFeatureGroupSchemaResponse.builder()
        .schema(features)
        .primaryKeys(
            cassandraFeatureGroupEntityPair.getEntityMetadata().getEntity().getPrimaryKeys())
        .version(cassandraFeatureGroupEntityPair.getFeatureGroupMetadata().getVersion())
        .featureGroupType(cassandraFeatureGroupEntityPair.getFeatureGroupMetadata().getFeatureGroupType())
        .build();
  }

  public Single<CassandraFeatureGroupEntityPair> getCassandraFeatureGroupEntityPair(
      String name, String version) {

    return getCassandraFeatureGroupMetaData(name, version)
        .flatMap(
            cassandraFeatureGroupMetadata -> {
              if (cassandraFeatureGroupMetadata
                  .getState()
                  .equals(CassandraFeatureGroupMetadata.State.ARCHIVED))
                return Single.error(new ApiRestException(FEATURE_GROUP_STATE_INVALID_EXCEPTION));

              return getCassandraEntityMetaData(
                      cassandraFeatureGroupMetadata.getFeatureGroup().getEntityName())
                  .map(
                      cassandraEntityMetadata ->
                          CassandraFeatureGroupEntityPair.builder()
                              .featureGroupMetadata(cassandraFeatureGroupMetadata)
                              .entityMetadata(cassandraEntityMetadata)
                              .build());
            });
  }

  public Single<CassandraEntity> getCassandraEntity(String name) {
    return getCassandraEntityMetaData(name).map(CassandraEntityMetadata::getEntity);
  }

  private Observable<CassandraEntityMetadata> getAllEntities() {
    return cassandraOfsV2AdminService.getAllCassandraEntityMetadata().flattenAsObservable(r -> r);
  }

  private Observable<CassandraFeatureGroupMetadata> getAllFeatureGroups() {
    return cassandraOfsV2AdminService
        .getAllCassandraFeatureGroupMetadata()
        .flattenAsObservable(r -> r);
  }

  private Observable<VersionMetadata> getAllFeatureGroupVersion() {
    return cassandraOfsV2AdminService
        .getAllCassandraFeatureGroupLatestVersions()
        .flattenAsObservable(r -> r);
  }

  public Completable loadCassandraEntityCache() {
    return getAllEntities()
        .flatMapCompletable(
            r -> {
              entityMetaDataCache.put(
                  CassandraEntityCacheKey.builder().name(r.getName()).build(),
                  CompletableFutureUtils.fromSingle(Single.just(r)));
              return Completable.complete();
            });
  }

  public Completable loadCassandraFeatureGroupCache() {
    return getAllFeatureGroups()
        .flatMapCompletable(
            r -> {
              featureGroupMetaDataCache.put(
                  CassandraFeatureGroupCacheKey.builder()
                      .name(r.getName())
                      .version(r.getVersion())
                      .build(),
                  CompletableFutureUtils.fromSingle(Single.just(r)));
              return Completable.complete();
            });
  }

  public Completable loadCassandraFeatureGroupVersionCache() {
    return getAllFeatureGroupVersion()
        .flatMapCompletable(
            r -> {
              featureGroupVersionCache.put(
                  r.getName(),
                  CompletableFutureUtils.fromSingle(Single.just(r)));
              return Completable.complete();
            });
  }

  public Completable refreshUpdatedCassandraEntities(Long timestamp) {
    return cassandraOfsV2AdminService
        .getAllUpdatedCassandraEntityMetadata(timestamp)
        .flattenAsObservable(r -> r)
        .flatMapCompletable(
            r -> {
              entityMetaDataCache.put(
                  CassandraEntityCacheKey.builder().name(r.getName()).build(),
                  CompletableFutureUtils.fromSingle(Single.just(r)));
              return Completable.complete();
            });
  }

  public Completable refreshUpdatedCassandraFeatureGroups(Long timestamp) {
    return cassandraOfsV2AdminService
        .getAllUpdatedCassandraFeatureGroupMetadata(timestamp)
        .flattenAsObservable(r -> r)
        .flatMapCompletable(
            r -> {
              featureGroupMetaDataCache.put(
                  CassandraFeatureGroupCacheKey.builder()
                      .name(r.getName())
                      .version(r.getVersion())
                      .build(),
                  CompletableFutureUtils.fromSingle(Single.just(r)));
              return Completable.complete();
            });
  }

  public Completable refreshUpdatedCassandraFeatureGroupVersions(Long timestamp) {
    return cassandraOfsV2AdminService
        .getAllUpdatedCassandraFeatureGroupLatestVersions(timestamp)
        .flattenAsObservable(r -> r)
        .flatMapCompletable(
            r -> {
              featureGroupVersionCache.put(
                  r.getName(),
                  CompletableFutureUtils.fromSingle(Single.just(r)));
              return Completable.complete();
            });
  }
}
