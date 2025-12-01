package com.dream11.app;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;

import com.dream11.app.dao.CassandraDaoFactory;
import com.dream11.app.verticle.DdMetricVerticle;
import com.dream11.common.app.AbstractApplication;
import com.dream11.common.app.AppContext;
import com.dream11.common.app.Deployable;
import com.dream11.common.app.VerticleConfig;
import com.dream11.common.util.CompletableUtils;
import com.dream11.common.util.MaintenanceUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.config.reactivex.client.ConfigClient;
import com.dream11.app.verticle.CacheUpdationVerticle;
import com.dream11.app.verticle.RestVerticle;
import com.dream11.app.verticle.StartupVerticle;
import com.dream11.core.util.S3ClientUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.inject.Module;
import io.reactivex.Completable;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.reactivex.core.Vertx;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainApplication extends AbstractApplication {

  public static void main(String[] args) {
    MainApplication app = new MainApplication();
    app.startApplication();
  }

  @Override
  protected Module[] getGoogleGuiceModules(Vertx vertx) {
    return new Module[] {new MainModule(vertx)};
  }

  @Override
  protected Deployable[] getVerticlesToDeploy(Vertx vertx) {
    //todo: load-test this

    AppContext.getInstance(ObjectMapper.class)
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(ALLOW_NON_NUMERIC_NUMBERS);

    return new Deployable[] {
      new Deployable(
          VerticleConfig.builder().instances(getEventLoopSize() - 1).verticleType(0).build(),
          RestVerticle.class),
      new Deployable(
          VerticleConfig.builder().instances(1).verticleType(0).build(), StartupVerticle.class),
      new Deployable(
          VerticleConfig.builder().instances(1).verticleType(0).build(),
          CacheUpdationVerticle.class),
        new Deployable(
            VerticleConfig.builder().instances(1).verticleType(0).build(),
            DdMetricVerticle.class)
    };
  }

  @Override
  protected Vertx initVertx(){
    return Vertx.vertx(
        new VertxOptions()
            .setEventLoopPoolSize(getEventLoopSize())
            .setPreferNativeTransport(true)
            .setMetricsOptions(
                new MicrometerMetricsOptions()
                    .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                    .setEnabled(true)));
  }

  @Override
  protected Completable rxDeployVerticles() {
    S3AsyncClient s3AsyncClient = S3ClientUtils.createS3Client();
    SharedDataUtils.setInstance(this.vertx, s3AsyncClient);

    if (Objects.equals(System.getProperty("app.environment"), "test")
        || Objects.equals(System.getProperty("app.environment"), "local")
        || Objects.equals(System.getProperty("app.environment"), "darwin-local")) {
      return super.rxDeployVerticles();
    }

    ConfigClient configClient = ConfigClient.create(vertx);
    SharedDataUtils.setInstance(
        this.vertx, configClient); // Set in shared data so you can access anywhere in application
    return configClient.rxConnect().ignoreElement().andThen(super.rxDeployVerticles());
  }

  public Completable rxStopApplication(Integer healthCheckWaitPeriod) {
    return AppContext.getInstance(CassandraDaoFactory.class).close()
        .andThen(super.rxStopApplication(healthCheckWaitPeriod));
  }

  public Completable rxRestartApplication(Integer healthCheckWaitPeriod, Integer maxDelaySeconds) {
    return AppContext.getInstance(CassandraDaoFactory.class).close()
        .andThen(super.rxRestartApplication(healthCheckWaitPeriod, maxDelaySeconds));
  }
}
