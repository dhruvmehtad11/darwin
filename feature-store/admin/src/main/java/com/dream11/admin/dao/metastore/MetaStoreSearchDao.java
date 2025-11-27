package com.dream11.admin.dao.metastore;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.constant.query.MysqlQuery.*;
import static com.dream11.core.util.MysqlUtils.convertStringToMysqlSearchPattern;

import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MetaStoreSearchDao {
  private final MysqlClient d11MysqlClient;

  public Observable<JsonObject> getDistinctCassandraOwners(String tableName) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getDistinctCassandraFeatureStoreOwnersQuery(tableName))
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

  public Observable<JsonObject> getDistinctCassandraTags(String tableName) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getDistinctCassandraFeatureStoreTagsQuery(tableName))
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

  public Observable<JsonObject> searchCassandraEntityUsingTags(List<String> tags) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getSearchCassandraEntityByTagsQuery(tags))
        .rxExecute(Tuple.tuple(new ArrayList<>(tags)))
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

  public Observable<JsonObject> searchCassandraEntityUsingOwners(List<String> owners) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getSearchCassandraEntityByOwnersQuery(owners))
        .rxExecute(Tuple.tuple(new ArrayList<>(owners)))
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

  public Observable<JsonObject> searchCassandraEntityUsingPattern(String pattern) {
    String searchPattern = convertStringToMysqlSearchPattern(pattern);
    return searchMysqlUsingPattern(searchCassandraEntityUsingPattern, searchPattern);
  }

  public Observable<JsonObject> searchCassandraFeatureGroupUsingPattern(String pattern) {
    String searchPattern = convertStringToMysqlSearchPattern(pattern);
    return searchMysqlUsingPattern(searchCassandraFeatureGroupUsingPattern, searchPattern);
  }

  public Observable<JsonObject> searchMysqlUsingPattern(String query, String pattern) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(query)
        .rxExecute(Tuple.of(pattern, pattern))
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

  public Observable<JsonObject> searchCassandraFeatureGroupUsingTags(List<String> tags) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getSearchCassandraFeatureGroupsByTagsQuery(tags))
        .rxExecute(Tuple.tuple(new ArrayList<>(tags)))
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

  public Observable<JsonObject> searchCassandraFeatureGroupUsingOwners(List<String> owners) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getSearchCassandraFeatureGroupsByOwnersQuery(owners))
        .rxExecute(Tuple.tuple(new ArrayList<>(owners)))
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

  public Observable<JsonObject> searchCassandraEntityInOrder(
      List<String> owners,
      List<String> tags,
      String pattern,
      Integer pageSize,
      Integer pageOffset) {
    return searchTableInOrder(
        CASSANDRA_ENTITY_METADATA_TABLE_NAME, owners, tags, pattern, pageSize, pageOffset);
  }

  public Observable<JsonObject> searchCassandraFeatureGroupInOrder(
      List<String> owners,
      List<String> tags,
      String pattern,
      Integer pageSize,
      Integer pageOffset) {
    return searchTableInOrder(
        CASSANDRA_FEATURE_GROUP_METADATA_TABLE_NAME, owners, tags, pattern, pageSize, pageOffset);
  }

  private Observable<JsonObject> searchTableInOrder(
      String tableName,
      List<String> owners,
      List<String> tags,
      String pattern,
      Integer pageSize,
      Integer pageOffset) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(searchAllInCassandraMetadata(tableName, owners, tags, pattern))
        .rxExecute(getSearchAllTuple(owners, tags, pattern, pageSize, pageOffset))
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

  public static Tuple getSearchAllTuple(
      List<String> owners,
      List<String> tags,
      String pattern,
      Integer pageSize,
      Integer pageOffset) {
    List<Object> li = new ArrayList<>();
    if (owners != null) li.addAll(owners);
    if (tags != null) li.addAll(tags);
    if (pattern != null) {
      li.add(convertStringToMysqlSearchPattern(pattern));
      li.add(
          convertStringToMysqlSearchPattern(pattern)); // adding twice for search by pattern query
    }
    li.add(pageSize);
    li.add(pageOffset);
    return Tuple.tuple(li);
  }
}
