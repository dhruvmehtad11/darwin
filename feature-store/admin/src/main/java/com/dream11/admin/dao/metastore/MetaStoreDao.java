package com.dream11.admin.dao.metastore;

import static com.dream11.core.constant.Constants.EMPTY_JSON_OBJECT_LIST;
import static com.dream11.core.constant.query.MysqlQuery.*;
import static com.dream11.core.error.ServiceError.ENTITY_NOT_FOUND_EXCEPTION;
import static com.dream11.core.error.ServiceError.FEATURE_GROUP_NOT_FOUND_EXCEPTION;
import static com.dream11.core.util.CassandraRowUtils.getEntityFeaturesTupleList;
import static com.dream11.core.util.CassandraRowUtils.getFeatureGroupFeaturesTupleList;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.TenantConfig;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Transaction;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MetaStoreDao {

  private final MysqlClient d11MysqlClient;

  public Single<Transaction> getMysqlTransaction() {
    return d11MysqlClient.getMasterMysqlClient().rxBegin();
  }

  public Completable addCassandraEntityMetadata(
      String name,
      JsonObject cassandraEntity,
      String owner,
      JsonArray tags,
      String description,
      Transaction tx) {
    return tx.preparedQuery(addCassandraEntityMetaDataQuery)
        .rxExecute(Tuple.of(name, cassandraEntity, owner, tags, description))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Completable addCassandraFeatureGroupMetadata(
      String name,
      String version,
      Boolean versionEnabled,
      CassandraFeatureGroup.FeatureGroupType featureGroupType,
      JsonObject cassandraFeatureGroup,
      String entity_name,
      String owner,
      JsonArray tags,
      String description,
      Transaction tx) {
    return tx.preparedQuery(addCassandraFeatureGroupMetaDataQuery)
        .rxExecute(
            Tuple.tuple(
                List.of(
                    name,
                    version,
                    versionEnabled,
                    featureGroupType,
                    cassandraFeatureGroup,
                    entity_name,
                    owner,
                    tags,
                    description)))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Completable addCassandraEntityFeaturesMetadata(
      String name, List<CassandraFeatureColumn> cassandraFeatureColumns, Transaction tx) {
    return tx.preparedQuery(addCassandraEntityFeatures)
        .rxExecuteBatch(getEntityFeaturesTupleList(name, cassandraFeatureColumns))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Completable addCassandraFeatureGroupFeaturesMetadata(
      String name,
      String version,
      List<CassandraFeatureColumn> cassandraFeatureColumns,
      Transaction tx) {
    return tx.preparedQuery(addCassandraFeatureGroupFeatures)
        .rxExecuteBatch(getFeatureGroupFeaturesTupleList(name, version, cassandraFeatureColumns))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Single<JsonObject> getCassandraEntityMetadata(String name) {
    return executeGetCassandraEntityMetadataSubQuery(name)
        .filter(r -> !r.isEmpty())
        .switchIfEmpty(Single.error(new ApiRestException(ENTITY_NOT_FOUND_EXCEPTION)))
        .map(r -> r.get(0));
  }

  public Single<JsonObject> getCassandraFeatureGroupMetadata(String name, String version) {
    return executeGetCassandraFeatureGroupMetadataSubQuery(name, version)
        .flattenAsObservable(r -> r)
        .firstElement()
        .switchIfEmpty(Single.error(new ApiRestException(FEATURE_GROUP_NOT_FOUND_EXCEPTION)));
  }

  public Completable updateCassandraFeatureGroupMetadata(
      String name, String version, CassandraFeatureGroupMetadata.State state) {
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(updateCassandraFeatureGroupMetaDataQuery)
        .rxExecute(Tuple.of(state, name, version))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Completable updateCassandraEntityMetadata(
      String name, CassandraEntityMetadata.State state) {
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(updateCassandraEntityMetaDataQuery)
        .rxExecute(Tuple.of(state, name))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Single<Boolean> checkCassandraEntityMetadataExists(String name) {
    return executeGetCassandraEntityMetadataSubQuery(name).map(r -> !r.isEmpty());
  }

  public Single<Boolean> checkCassandraFeatureGroupMetadataExists(String name, String version) {
    return executeGetCassandraFeatureGroupMetadataSubQuery(name, version).map(r -> !r.isEmpty());
  }

  private Single<List<JsonObject>> executeGetCassandraEntityMetadataSubQuery(String name) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getCassandraEntityMetaDataQuery)
        .rxExecute(Tuple.of(name))
        .map(MysqlUtils::rowSetToJsonList)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Single.error(e);
            });
  }

  private Single<List<JsonObject>> executeGetCassandraFeatureGroupMetadataSubQuery(
      String name, String version) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getCassandraFeatureGroupMetaDataQuery)
        .rxExecute(Tuple.of(name, version))
        .map(MysqlUtils::rowSetToJsonList)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Single.error(e);
            });
  }

  public Single<List<JsonObject>> getAllFeatureGroupsRegisteredWithAnEntity(String entityName) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getAllFeaturesForAnEntityQuery)
        .rxExecute(Tuple.of(entityName))
        .map(MysqlUtils::rowSetToJsonList)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Single.error(e);
            });
  }

  public Observable<JsonObject> getAllEntities() {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .query(getAllCassandraEntities)
        .rxExecute()
        .map(MysqlUtils::rowSetToJsonList)
        .filter(r -> !r.isEmpty())
        .switchIfEmpty(Single.just(EMPTY_JSON_OBJECT_LIST))
        .flattenAsObservable(r -> r)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Observable.error(e);
            });
  }

  public Observable<JsonObject> getAllFeatureGroups() {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .query(getAllCassandraFeatureGroups)
        .rxExecute()
        .map(MysqlUtils::rowSetToJsonList)
        .filter(r -> !r.isEmpty())
        .switchIfEmpty(Single.just(EMPTY_JSON_OBJECT_LIST))
        .flattenAsObservable(r -> r)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Observable.error(e);
            });
  }

  // locking during tenant migration
  public Observable<JsonObject> getAllFeatureGroupVersions(Transaction tx, String name) {
    return tx.query(getAllCassandraFeatureGroupVersions)
        .rxExecute()
        .map(MysqlUtils::rowSetToJsonList)
        .filter(r -> !r.isEmpty())
        .switchIfEmpty(Single.just(EMPTY_JSON_OBJECT_LIST))
        .flattenAsObservable(r -> r)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Observable.error(e);
            });
  }

  public Observable<JsonObject> getUpdatedFeatureGroupVersions(Long timestamp) {
    return getCassandraUpdatedData(timestamp, getUpdatedCassandraFeatureGroupVersions).map(r -> r);
  }

  public Observable<JsonObject> getUpdatedFeatureGroups(Long timestamp) {
    return getCassandraUpdatedData(timestamp, getUpdatedCassandraFeatureGroups).map(r -> r);
  }

  public Observable<JsonObject> getUpdatedEntities(Long timestamp) {
    return getCassandraUpdatedData(timestamp, getUpdatedCassandraEntities);
  }

  private Observable<JsonObject> getCassandraUpdatedData(Long timestamp, String query) {
    timestamp /= 1000; // mysql datetime operates in seconds

    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(query)
        .rxExecute(Tuple.of(timestamp))
        .map(MysqlUtils::rowSetToJsonList)
        .filter(r -> !r.isEmpty())
        .switchIfEmpty(Single.just(EMPTY_JSON_OBJECT_LIST))
        .flattenAsObservable(r -> r)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Observable.error(e);
            });
  }

  public Completable addTenant(JsonObject tenantConfig, String fgName) {
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(updateFgTenantQuery)
        .rxExecute(Tuple.of(tenantConfig, fgName))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error(String.format("error adding tenant: %s", e.getMessage()), e);
              return Completable.error(e);
            });
  }

  public Completable updateEntityTtl(String name, JsonObject entity) {
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(updateEntityTtlQuery)
        .rxExecute(Tuple.of(entity, name))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error(String.format("error adding tenant: %s", e.getMessage()), e);
              return Completable.error(e);
            });
  }
}
