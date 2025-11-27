package com.dream11.populator.verticle;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.common.util.SharedDataUtils;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.HelixConfig;
import com.dream11.populator.deltareader.TableReader;
import com.dream11.populator.service.ClusterManager;
import com.dream11.populator.service.ControllerService;
import com.dream11.rest.AbstractRestVerticle;
import com.dream11.webclient.reactivex.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import java.util.UUID;
import org.apache.hadoop.conf.Configuration;
import org.apache.helix.InstanceType;

public class HelixControllerVerticle extends AbstractRestVerticle {
  private final String consumerId = UUID.randomUUID().toString();
  private WebClient webClient;
  private ClusterManager clusterManager;
  private ControllerService controllerService;

  public HelixControllerVerticle() {
    super("com.dream11.populator");
  }

  @Override
  public Completable rxStart() {
    this.webClient = WebClient.create(vertx);
    ContextUtils.setInstance(webClient);

    return initController().andThen(super.rxStart()).doOnComplete(() -> controllerService.start());
  }

  private Completable initController() {
    HelixConfig config = ConfigReader.readHelixConfigFromFile();
    config.setClusterName(
        AppContext.getInstance(ApplicationConfig.class).getPopulatorHelixClusterName());
    config.setInstanceName(consumerId);

    this.clusterManager = ClusterManager.create(vertx, config, InstanceType.CONTROLLER);
    ContextUtils.setInstance(this.clusterManager);

    TableReader tableReader =
        new TableReader(
            vertx,
            this.clusterManager.getManager(),
            this.clusterManager.getZkHelixAdmin(),
            AppContext.getInstance(ApplicationConfig.class));
    ContextUtils.setInstance(tableReader);

    this.controllerService = ControllerService.create(vertx, webClient, clusterManager);
    ContextUtils.setInstance(controllerService);

    return this.clusterManager.rxStart().doOnComplete(() -> tableReader.start(InstanceType.CONTROLLER));
  }

  @Override
  public Completable rxStop() {
    return this.clusterManager.rxStop();
  }
}
