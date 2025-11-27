package com.dream11.admin;

import com.dream11.admin.verticle.CacheUpdationVerticle;
import com.dream11.admin.verticle.CronVerticle;
import com.dream11.admin.verticle.StartupVerticle;
import com.dream11.common.app.Deployable;
import com.dream11.common.app.VerticleConfig;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.config.reactivex.client.ConfigClient;
import com.dream11.admin.verticle.RestVerticle;
import com.dream11.core.util.S3ClientUtils;
import com.dream11.job.AbstractCronApplication;
import com.dream11.job.config.ClusterModule;
import com.google.inject.Module;
import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Slf4j
public class MainApplication extends AbstractCronApplication {
  private static final String packageName = "com.dream11.admin";

  public MainApplication() {
    super(packageName);
  }

  public static void main(String[] args) {
    System.out.println(System.getProperties());
    MainApplication app = new MainApplication();
    app.startApplication();
  }

  @Override
  protected Module[] getGoogleGuiceModules(Vertx vertx) {
    return new Module[] {new MainModule(vertx), new ClusterModule(this.clusterManager)};
  }

  @Override
  protected Deployable[] getVerticlesToDeploy(Vertx vertx) {
    return new Deployable[] {
      new Deployable(
          VerticleConfig.builder().instances(1).verticleType(0).build(), StartupVerticle.class),
      new Deployable(
          VerticleConfig.builder().instances(1).verticleType(0).build(),
          CacheUpdationVerticle.class),
      new Deployable(
          VerticleConfig.builder().instances(2).verticleType(0).build(), CronVerticle.class),
      new Deployable(
          VerticleConfig.builder().instances(getEventLoopSize() - 1).verticleType(0).build(),
          RestVerticle.class)
    };
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
}
