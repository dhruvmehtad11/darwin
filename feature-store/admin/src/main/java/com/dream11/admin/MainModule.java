package com.dream11.admin;

import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.guice.VertxAbstractModule;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.webclient.reactivex.client.WebClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.vertx.reactivex.core.Vertx;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.internal.crt.DefaultS3CrtAsyncClient;

public class MainModule extends VertxAbstractModule {
  private Vertx vertx;

  public MainModule(Vertx vertx) {

    super(vertx);
    this.vertx = vertx;
  }

  @Override
  protected void bindConfiguration() {
    bind(StatsDClient.class)
        .toInstance(SharedDataUtils.getInstance(this.vertx, NonBlockingStatsDClient.class));
    bind(CassandraClient.class).toProvider(() -> ContextUtils.getInstance(CassandraClient.class));
    bind(MysqlClient.class).toProvider(() -> ContextUtils.getInstance(MysqlClient.class));
    bind(WebClient.class).toProvider(() -> ContextUtils.getInstance(WebClient.class));
    bind(ApplicationConfig.class).toProvider(ConfigManager.class).asEagerSingleton();
    bind(S3AsyncClient.class).toProvider(() -> SharedDataUtils.getInstance(vertx, DefaultS3CrtAsyncClient.class));
  }
}
