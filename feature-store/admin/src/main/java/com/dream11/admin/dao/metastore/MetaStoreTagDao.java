package com.dream11.admin.dao.metastore;

import static com.dream11.core.constant.query.MysqlQuery.insertEntityTagQuery;
import static com.dream11.core.constant.query.MysqlQuery.insertFeatureGroupTagQuery;

import com.dream11.mysql.reactivex.client.MysqlClient;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MetaStoreTagDao {
  private final MysqlClient d11MysqlClient;

  public Completable insertEntityTags(List<String> tags, String entityName) {
    if (tags.isEmpty()) return Completable.complete();
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(insertEntityTagQuery)
        .rxExecuteBatch(getListOfInsertEntityTagTuple(tags, entityName))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  public Completable insertFeatureGroupTags(
      List<String> tags, String featureGroupName, String featureGroupVersion) {
    if (tags.isEmpty()) return Completable.complete();
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(insertFeatureGroupTagQuery)
        .rxExecuteBatch(
            getListOfInsertFeatureGroupTagTuple(tags, featureGroupName, featureGroupVersion))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Completable.error(e);
            });
  }

  private static List<Tuple> getListOfInsertFeatureGroupTagTuple(
      List<String> tags, String featureGroupName, String featureGroupVersion) {
    List<Tuple> tuples = new ArrayList<>();
    for (String tag : tags) {
      tuples.add(Tuple.of(tag, featureGroupName, featureGroupVersion));
    }
    return tuples;
  }

  private static List<Tuple> getListOfInsertEntityTagTuple(List<String> tags, String entityName) {
    List<Tuple> tuples = new ArrayList<>();
    for (String tag : tags) {
      tuples.add(Tuple.of(tag, entityName));
    }
    return tuples;
  }
}
