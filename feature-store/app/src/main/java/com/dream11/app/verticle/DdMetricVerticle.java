package com.dream11.app.verticle;

import com.dream11.app.jobs.DdMetricsJob;
import com.dream11.common.app.AppContext;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DdMetricVerticle extends AbstractVerticle {
  DdMetricsJob ddMetricsJob = AppContext.getInstance(DdMetricsJob.class);

  @Override
  public Completable rxStart() {
    return Completable.complete().doOnComplete(this::executeAtStartup);
  }
  public void executeAtStartup() {
    ddMetricsJob.pushMetricsToDD().subscribe();
    vertx.setPeriodic(5_000, timerId -> ddMetricsJob.pushMetricsToDD().subscribe());
  }
  @Override
  public Completable rxStop() {
    return super.rxStop();
  }
}
