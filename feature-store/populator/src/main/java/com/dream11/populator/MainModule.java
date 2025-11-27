package com.dream11.populator;

import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.guice.VertxAbstractModule;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;

public class MainModule extends VertxAbstractModule {
  Vertx vertx;

  public MainModule(Vertx vertx) {
    super(vertx);
    this.vertx = vertx;
  }

  @Override
  protected void bindConfiguration() {
    bind(StatsDClient.class)
        .toInstance(SharedDataUtils.getInstance(this.vertx, NonBlockingStatsDClient.class));
    bind(ApplicationConfig.class).toProvider(ConfigManager.class).asEagerSingleton();
    bind(WebClient.class).toProvider(() -> ContextUtils.getInstance(WebClient.class));
    bind(new TypeLiteral<KafkaProducer<String, String>>() {})
        .toProvider(() -> ContextUtils.getInstance(KafkaProducer.class));
  }
}
