package com.dream11.populator;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS;

import com.dream11.common.app.AbstractApplication;
import com.dream11.common.app.AppContext;
import com.dream11.common.app.Deployable;
import com.dream11.common.app.VerticleConfig;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.config.reactivex.client.ConfigClient;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.HelixConfig;
import com.dream11.populator.deltareader.TableReader;
import com.dream11.populator.service.ClusterManager;
import com.dream11.populator.verticle.HelixControllerVerticle;
import com.dream11.populator.verticle.HelixParticipantWorkerVerticle;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Module;
import io.reactivex.Completable;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.reactivex.core.Vertx;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainApplication extends AbstractApplication {

  public static final String NODE_ID = UUID.randomUUID().toString();

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
    // todo: load-test this

    AppContext.getInstance(ObjectMapper.class)
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(ALLOW_NON_NUMERIC_NUMBERS);

    return new Deployable[] {
      new Deployable(
          VerticleConfig.builder().instances(getEventLoopSize()).verticleType(0).build(),
          HelixParticipantWorkerVerticle.class),
      new Deployable(
          VerticleConfig.builder().instances(2).verticleType(0).build(),
          HelixControllerVerticle.class)
    };
  }

  @Override
  protected Vertx initVertx() {
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
}
