package com.dream11.consumer.verticle;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.consumer.service.AdminClientService;
import com.dream11.consumer.service.ClusterManager;
import com.dream11.consumer.service.ControllerService;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.HelixConfig;
import com.dream11.core.util.S3ClientUtils;
import com.dream11.rest.AbstractRestVerticle;
import com.dream11.webclient.reactivex.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import java.util.UUID;
import org.apache.helix.InstanceType;
import software.amazon.awssdk.services.s3.S3AsyncClient;

public class HelixControllerVerticle extends AbstractRestVerticle {
  private final String consumerId = UUID.randomUUID().toString();
  private WebClient webClient;
  private ClusterManager clusterManager;
  private ApplicationConfig applicationConfig;
  private ObjectMapper objectMapper;
  private ControllerService controllerService;
  private AdminClientService adminClientService;

  public HelixControllerVerticle() {
    super("com.dream11.consumer");
  }

  @Override
  public Completable rxStart() {
    this.applicationConfig = AppContext.getInstance(ApplicationConfig.class);
    this.objectMapper = AppContext.getInstance(ObjectMapper.class);


    HelixConfig config = ConfigReader.readHelixConfigFromFile();
    config.setClusterName(
        AppContext.getInstance(ApplicationConfig.class).getConsumerHelixClusterName());
    config.setInstanceName(consumerId);

    this.clusterManager = ClusterManager.create(vertx, config, InstanceType.CONTROLLER);
    ContextUtils.setInstance(clusterManager);

    this.webClient = WebClient.create(vertx);
    ContextUtils.setInstance(webClient);

    S3AsyncClient s3AsyncClient = S3ClientUtils.createS3Client();
    ContextUtils.setInstance(s3AsyncClient);

    this.controllerService = ControllerService.create(vertx, clusterManager);
    ContextUtils.setInstance(controllerService);

    return clusterManager
        .rxStart()
        .andThen(super.rxStart())
        .doOnComplete(() -> controllerService.start());
  }

  @Override
  public Completable rxStop() {
    return this.clusterManager.rxStop();
  }
}
