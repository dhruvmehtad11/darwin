package com.dream11.consumer;

import com.dream11.common.guice.VertxAbstractModule;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.consumer.service.AdminClientService;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.inject.TypeLiteral;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.internal.crt.DefaultS3CrtAsyncClient;

public class MainModule extends VertxAbstractModule {
  Vertx vertx;

  public  MainModule(Vertx vertx) {
    super(vertx);
    this.vertx = vertx;
  }

  @Override
  protected void bindConfiguration() {
    bind(StatsDClient.class)
        .toInstance(SharedDataUtils.getInstance(this.vertx, NonBlockingStatsDClient.class));
    bind(ApplicationConfig.class).toProvider(ConfigManager.class).asEagerSingleton();
    bind(WebClient.class).toProvider(() -> ContextUtils.getInstance(WebClient.class));
    bind(new TypeLiteral<KafkaProducer<String, String>>() {
    })
        .toProvider(() -> ContextUtils.getInstance(KafkaProducer.class));
    bind(new TypeLiteral<KafkaConsumer<String, String>>() {
    })
        .toProvider(() -> ContextUtils.getInstance(KafkaConsumer.class));
    bind(AdminClient.class).toProvider(() -> ContextUtils.getInstance(KafkaAdminClient.class));
    bind(S3AsyncClient.class).toProvider(() -> ContextUtils.getInstance(DefaultS3CrtAsyncClient.class));
  }
}
