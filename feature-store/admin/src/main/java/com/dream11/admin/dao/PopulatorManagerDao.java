package com.dream11.admin.dao;

import static com.dream11.core.constant.query.MysqlQuery.getAllCassandraStoreTenantPopulatorDetails;
import static com.dream11.core.constant.query.MysqlQuery.insertIntoCassandraStoreTenantPopulatorDetails;

import com.dream11.core.dto.populator.FeatureGroupPopulatorMap;
import com.dream11.core.dto.populator.PopulatorGroupMetadata;
import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.reactivex.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PopulatorManagerDao {
  private final MysqlClient d11MysqlClient;
  private final ObjectMapper objectMapper;

  public Completable addPopulatorMetadata(PopulatorGroupMetadata populatorGroupMetadata) {
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(insertIntoCassandraStoreTenantPopulatorDetails)
        .rxExecute(
            Tuple.of(
                populatorGroupMetadata.getTenantName(),
                populatorGroupMetadata.getNumWorkers(),
                populatorGroupMetadata.getNumWorkers()))
        .ignoreElement();
  }

  public Observable<FeatureGroupPopulatorMap> getAllPopulatorMetadata() {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getAllCassandraStoreTenantPopulatorDetails)
        .rxExecute()
        .map(MysqlUtils::rowSetToJsonList)
        .flattenAsObservable(r -> r)
        .map(r -> objectMapper.readValue(r.toString(), FeatureGroupPopulatorMap.class));
  }
}
