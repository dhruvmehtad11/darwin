package com.dream11.admin.verticle;

import com.dream11.admin.jobs.CacheUpdationJob;
import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.common.collect.ImmutableList;
import com.timgroup.statsd.Event;
import com.timgroup.statsd.StatsDClient;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import java.util.Random;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CacheUpdationVerticle extends AbstractVerticle {
  @NonFinal CassandraClient d11CassandraClient;
  @NonFinal WebClient d11WebClient;
  @NonFinal MysqlClient d11MysqlClient;

  private final Random random = new Random();

  private final StatsDClient d11DDClient = AppContext.getInstance(StatsDClient.class);

  private Boolean paused = false;

  private void pauseProcessing() {
    this.paused = true;
  }

  private void resumeProcessing() {
    this.paused = false;
  }

  @Override
  public Completable rxStart() {

    this.d11CassandraClient = CassandraClient.create(vertx);
    ContextUtils.setInstance(d11CassandraClient);

    this.d11MysqlClient = MysqlClient.create(vertx);
    ContextUtils.setInstance(d11MysqlClient);

    this.d11WebClient = WebClient.create(vertx);
    ContextUtils.setInstance(d11WebClient);

    val list =
        ImmutableList.<Completable>builder()
            .add(d11MysqlClient.rxConnect())
            .add(d11CassandraClient.rxConnect())
            .build();

    return Completable.merge(list).doOnComplete(() -> this.executePeriodic().subscribe());
  }

  @Override
  public Completable rxStop() {
    d11MysqlClient.close();
    d11WebClient.close();
    d11CassandraClient.close();
    return super.rxStop();
  }

  private Completable executePeriodic() {
    vertx.setTimer(
        10_000
            + random.nextInt(2000), // cool down on startup and random salt to stagger the execution
        timerId -> {
          vertx.setPeriodic(
              30_000, // every 30sec
              context -> {
                if (!paused) {
                  this.triggerJob();
                }
              });
        });
    return Completable.complete();
  }

  private void triggerJob() {
    long startTime = System.currentTimeMillis();
    CacheUpdationJob job = AppContext.getInstance(CacheUpdationJob.class);
    String jobName = job.getClass().getSimpleName();

    this.pauseProcessing();
    job.handle()
        .doFinally(this::resumeProcessing)
        .subscribe(
            () -> recordSuccess(jobName, startTime), (err) -> recordError(err, jobName, startTime));
  }

  private void recordSuccess(String jobName, long startTime) {
    long timeTaken = System.currentTimeMillis() - startTime;
    log.info("Job:{} completed without errors in time:{}ms", jobName, timeTaken);
    this.d11DDClient.recordExecutionTime(jobName, timeTaken, new String[0]);
  }

  private void recordError(Throwable err, String jobName, long startTime) {
    long timeTaken = System.currentTimeMillis() - startTime;
    String tag = jobName + "_ERROR";
    Event event =
        Event.builder()
            .withTitle("Exception in " + jobName)
            .withText(err.toString())
            .withAlertType(Event.AlertType.ERROR)
            .withAggregationKey(tag)
            .build();
    this.d11DDClient.recordEvent(event, new String[] {tag});
    this.d11DDClient.recordExecutionTime(jobName + "_error", timeTaken, new String[0]);
    log.error(String.format("Job:%s completed with errors in time:%sms", jobName, timeTaken), err);
  }
}
