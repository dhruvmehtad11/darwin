package com.dream11.admin.dao;

import static com.dream11.core.constant.Constants.JOB_HEALTH_CACHE_NAME;
import static com.dream11.core.constant.query.CassandraQuery.CASSANDRA_HEALTH_CHECK;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.dream11.caffeine.CaffeineCacheFactory;
import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.common.util.JsonUtils;
import com.dream11.common.util.ListUtils;
import com.dream11.common.util.MaintenanceUtils;
import com.dream11.common.util.SingleUtils;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class HealthCheckDao {

  final CassandraClient d11CassandraClient;

  final MysqlClient d11MysqlClient;

  public Single<JsonObject> mysqlHealthCheck() {
    val masterMysqlClient = d11MysqlClient.getMasterMysqlClient();
    return masterMysqlClient
        .query("SELECT 1;")
        .rxExecute()
        .map(rowSet -> JsonUtils.jsonFrom("response", "1"))
        .compose(SingleUtils.applyDebugLogs(log));
  }

  public Single<JsonObject> cassandraHealthCheck() {
    val cassandraClient = d11CassandraClient.getCassandraClient();
    val healthCheckQuery = new SimpleStatement(CASSANDRA_HEALTH_CHECK);
    return cassandraClient
        .rxExecuteWithFullFetch(healthCheckQuery)
        .map(rows -> JsonUtils.jsonFrom("rows", ListUtils.map(Row::toString, rows)))
        .compose(SingleUtils.applyDebugLogs(log));
  }

  public Single<JsonObject> maintenanceHealthCheck() {
    val isUnderMaintenance = MaintenanceUtils.isUnderMaintenance(Vertx.currentContext().owner()).get();
    val response = JsonUtils.jsonFrom("isUnderMaintenance", String.valueOf(isUnderMaintenance));
    return isUnderMaintenance ? Single.error(new RuntimeException(response.toString())) : Single.just(response);
  }

}
