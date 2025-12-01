package com.dream11.admin.dao.featuredao;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.constant.query.CassandraQuery.*;
import static com.dream11.core.dto.featuregroup.CassandraFeatureGroup.getFeatureNames;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.app.AppContext;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.entity.interfaces.Entity;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.featuregroup.interfaces.FeatureGroup;
import com.dream11.core.dto.featurevalue.interfaces.FeatureValue;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CassandraFeatureDao implements FeatureDao {
  private final CassandraClient d11CassandraClient;
  private final ApplicationConfig applicationConfig;

  private final AsyncLoadingCache<String, PreparedStatement> preparedStatementCache =
      CaffeineCacheFactory.createAsyncLoadingCache(
          AppContext.getInstance(Vertx.class),
          PREPARED_STATEMENT_CAFFEINE_CACHE_NAME,
          this::prepareStmt);

  private Single<PreparedStatement> prepareStmt(String query) {
    return d11CassandraClient.getCassandraClient().rxPrepare(query);
  }

  private Single<PreparedStatement> prepareStmtOrGetFromCache(String query) {
    return Single.create(
        emitter ->
            preparedStatementCache
                .get(query)
                .whenComplete(
                    (result, throwable) -> {
                      if (throwable != null) {
                        emitter.onError(throwable);
                      } else {
                        emitter.onSuccess(result);
                      }
                    }));
  }

  public static Completable migrateEntity(io.vertx.reactivex.cassandra.CassandraClient client, Entity entity, String keySpace) {
    CassandraEntity cassandraEntity = (CassandraEntity) entity;
    return client
        .rxExecute(
            getMigrateTableQuery(
                keySpace,
                cassandraEntity.getTableName(),
                cassandraEntity.getFeatures(),
                cassandraEntity.getPrimaryKeys(),
                cassandraEntity.getTtl()))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error("error migrating entity ", e);
              return Completable.error(e);
            });
  }

  public Completable createEntity(Entity entity) {
    CassandraEntity cassandraEntity = (CassandraEntity) entity;
    return d11CassandraClient
        .getCassandraClient()
        .rxExecute(
            getCreateTableQuery(
                applicationConfig.getCassandraOfsKeySpace(),
                cassandraEntity.getTableName(),
                cassandraEntity.getFeatures(),
                cassandraEntity.getPrimaryKeys(),
                cassandraEntity.getTtl()))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error("error creating entity ", e);
              return Completable.error(e);
            });
  }

  public Completable deleteEntity(CassandraEntity entity) {
    return d11CassandraClient
        .getCassandraClient()
        .rxExecute(getDeleteEntityQuery(applicationConfig.getCassandraOfsKeySpace(), entity.getTableName()))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error("error deleting entity ", e);
              return Completable.error(e);
            });
  }

  public Completable createFeatureGroup(FeatureGroup featureGroup, String version) {
    CassandraFeatureGroup cassandraFeatureGroup = (CassandraFeatureGroup) featureGroup;
    return d11CassandraClient
        .getCassandraClient()
        .rxExecute(
            getAlterTableQuery(
                applicationConfig.getCassandraOfsKeySpace(),
                cassandraFeatureGroup.getEntityName(),
                cassandraFeatureGroup.getFeatureGroupName(),
                version,
                cassandraFeatureGroup.getFeatures(),
                cassandraFeatureGroup.getVersionEnabled()))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error("error creating feature group ", e);
              return Completable.error(e);
            });
  }

  public static Completable migrateFeatureGroup(io.vertx.reactivex.cassandra.CassandraClient client, FeatureGroup featureGroup,
                                                String version, String keyspace) {
    CassandraFeatureGroup cassandraFeatureGroup = (CassandraFeatureGroup) featureGroup;
    return client.rxPrepare(GET_CASSANDRA_TABLE_COLUMNS_QUERY)
        .map(preparedStatement -> preparedStatement.bind(keyspace, ((CassandraFeatureGroup) featureGroup).getEntityName()))
        .flatMap(client::rxExecuteWithFullFetch)
        .flatMapCompletable(rows -> {
          Set<String> currentColumns = new HashSet<>();
          for(Row row: rows){
            currentColumns.add(row.getString(0));
          }

          String query = getMigrateFeatureGroupQuery(
              keyspace,
              cassandraFeatureGroup.getEntityName(),
              cassandraFeatureGroup.getFeatureGroupName(),
              version,
              cassandraFeatureGroup.getFeatures(),
              cassandraFeatureGroup.getVersionEnabled(),
              currentColumns);

          // in case the fg is moved back to a cluster containing that fg
          if(query.isEmpty())
            return Completable.complete();

          return client
              .rxExecute(query)
              .ignoreElement()
              .onErrorResumeNext(
                  e -> {
                    log.error("error migrating feature group ", e);
                    return Completable.error(e);
                  });
        });
  }

  public Completable deleteFeatureGroup(FeatureGroup featureGroup, String version) {
    CassandraFeatureGroup cassandraFeatureGroup = (CassandraFeatureGroup) featureGroup;
    return d11CassandraClient
        .getCassandraClient()
        .rxExecute(
            getRemoveColumnQuery(
                applicationConfig.getCassandraOfsKeySpace(),
                cassandraFeatureGroup.getEntityName(),
                cassandraFeatureGroup.getFeatureGroupName(),
                version,
                getFeatureNames(cassandraFeatureGroup),
                cassandraFeatureGroup.getVersionEnabled()))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error("error deleting feature group ", e);
              return Completable.error(e);
            });
  }

  public FeatureValue getFeatures() {
    return null;
  }
}
