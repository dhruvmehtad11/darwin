package com.dream11.app.dao;

import static com.dream11.core.constant.Constants.DEFAULT_TENANT_NAME;
import static com.dream11.core.constant.query.CassandraQuery.CASSANDRA_HEALTH_CHECK;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.dream11.common.util.JsonUtils;
import com.dream11.common.util.ListUtils;
import com.dream11.common.util.MaintenanceUtils;
import com.dream11.common.util.SingleUtils;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class HealthCheckDao {

  final CassandraDaoFactory factory;

  public Single<JsonObject> cassandraHealthCheck() {
    val cassandraClient = factory.getDao(DEFAULT_TENANT_NAME);
    val healthCheckQuery = new SimpleStatement(CASSANDRA_HEALTH_CHECK);
    return cassandraClient
        .flatMap(r -> r.getCassandraClient()
        .rxExecuteWithFullFetch(healthCheckQuery)
        .map(rows -> JsonUtils.jsonFrom("rows", ListUtils.map(Row::toString, rows)))
        .compose(SingleUtils.applyDebugLogs(log)));
  }

  public Single<JsonObject> maintenanceHealthCheck() {
    val isUnderMaintenance = MaintenanceUtils.isUnderMaintenance(Vertx.currentContext().owner()).get();
    val response = JsonUtils.jsonFrom("isUnderMaintenance", String.valueOf(isUnderMaintenance));
    return isUnderMaintenance ? Single.error(new RuntimeException(response.toString())) : Single.just(response);
  }
}
