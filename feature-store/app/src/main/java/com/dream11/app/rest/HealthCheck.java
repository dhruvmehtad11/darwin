package com.dream11.app.rest;

import com.dream11.app.jobs.CacheUpdationJob;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.app.dao.HealthCheckDao;
import com.dream11.rest.healthcheck.HealthCheckResponse;
import com.dream11.rest.healthcheck.HealthCheckUtil;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/healthcheck")
public class HealthCheck {

  final HealthCheckDao healthCheckDao;

  @GET
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiResponse(content = @Content(schema = @Schema(implementation = HealthCheckResponse.class)))
  public CompletionStage<HealthCheckResponse> healthcheck() {
    val map =
        ImmutableMap.<String, Single<JsonObject>>builder()
            .put("maintenance", healthCheckDao.maintenanceHealthCheck())
            .put("cassandra", healthCheckDao.cassandraHealthCheck())
            .build();
    return HealthCheckUtil.handler(map).to(CompletableFutureUtils::fromSingle);
  }
}
