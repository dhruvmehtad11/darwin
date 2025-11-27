package com.dream11.consumer.rest;

import com.dream11.common.util.CompletableFutureUtils;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path("/metrics")
public class Metrics {
  private final PrometheusMeterRegistry meterRegistry = (PrometheusMeterRegistry) BackendRegistries.getDefaultNow();

  @GET
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.TEXT_PLAIN)
  public CompletionStage<String> metrics() {
    return Vertx.currentContext().rxExecuteBlocking(promise -> {
      try {
        promise.complete(meterRegistry.scrape());
      } catch (Exception e) {
        log.error("error fetching metrics from prometheus-meter-registry", e);
        promise.fail(e);
      }
    }).toSingle().map(r -> (String) r).to(CompletableFutureUtils::fromSingle);
  }
}
