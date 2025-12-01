package com.dream11.app.verticle;

import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.dream11.app.jobs.StartupJob;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;

import java.util.Random;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class StartupVerticle extends AbstractVerticle {
  @NonFinal
  CassandraClient d11CassandraClient;
  @NonFinal
  WebClient d11WebClient;

  private final Random random = new Random();

  @Override
  public Completable rxStart() {

    this.d11CassandraClient = CassandraClient.create(vertx);
    ContextUtils.setInstance(d11CassandraClient);

    this.d11WebClient = WebClient.create(vertx);
    ContextUtils.setInstance(d11WebClient);

    val list =
        ImmutableList.<Completable>builder()
            .add(d11CassandraClient.rxConnect())
            .build();

    return Completable.merge(list).doOnComplete(() -> this.executeAtStartup().subscribe());
  }

  private Completable executeAtStartup() {
    StartupJob startupJob = AppContext.getInstance(StartupJob.class);

    vertx.setTimer(
        1_000 + random.nextInt(2000),
        timerId -> {
          startupJob.handle().subscribe();
        });
    return Completable.complete();
  }

  @Override
  public Completable rxStop() {
    d11WebClient.close();
    d11CassandraClient.close();
    return super.rxStop();
  }
}
