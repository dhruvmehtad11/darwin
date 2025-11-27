package com.dream11.populator.verticle;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.populator.deltareader.TableReader;
import com.dream11.populator.service.ClusterManager;
import com.dream11.populator.service.OfsKafkaWriterService;
import com.dream11.populator.service.WorkerMailBox;
import com.dream11.populator.service.ControllerService;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.ConsumerConfig;
import com.dream11.core.config.HelixConfig;
import com.dream11.core.config.KafkaAdminConfig;
import com.dream11.core.config.ProducerConfig;
import com.dream11.core.util.ConsumerUtils;
import com.dream11.core.util.KafkaAdminUtils;
import com.dream11.core.util.ProducerUtils;
import com.dream11.webclient.reactivex.client.WebClient;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import lombok.experimental.NonFinal;
import org.apache.hadoop.conf.Configuration;
import org.apache.helix.InstanceType;
import org.apache.kafka.clients.admin.AdminClient;
import java.util.UUID;

import static com.dream11.core.config.ConfigReader.readKafkaAdminConfigFromFile;

public class HelixParticipantWorkerVerticle extends AbstractVerticle {
  private final String consumerId = UUID.randomUUID().toString();

  @NonFinal  private ClusterManager clusterManager;

  @NonFinal
  private WebClient webClient;
  @NonFinal private KafkaProducer<String, String> producer;
  @NonFinal private WorkerMailBox consumerMailBox;

  @Override
  public Completable rxStart() {
    HelixConfig config = ConfigReader.readHelixConfigFromFile();
    config.setClusterName(AppContext.getInstance(ApplicationConfig.class).getPopulatorHelixClusterName());
    config.setInstanceName(consumerId);

    this.clusterManager = ClusterManager.create(vertx, config, InstanceType.PARTICIPANT);
    ContextUtils.setInstance(clusterManager);

    TableReader tableReader =
        new TableReader(
            vertx,
            this.clusterManager.getManager(),
            this.clusterManager.getZkHelixAdmin(),
            AppContext.getInstance(ApplicationConfig.class));
    ContextUtils.setInstance(tableReader);

    this.consumerMailBox = WorkerMailBox.create(vertx, clusterManager);
    ContextUtils.setInstance(consumerMailBox);

    initClients();

    OfsKafkaWriterService ofsKafkaWriterService = AppContext.getInstance(OfsKafkaWriterService.class);
    ContextUtils.setInstance(ofsKafkaWriterService);

    return clusterManager
        .rxStart()
        .doOnComplete(() -> {
          tableReader.start(InstanceType.PARTICIPANT);
          consumerMailBox.start(consumerId, this::rxStop);
          ControllerService.addConsumerId(consumerId);
        });
  }

  @Override
  public Completable rxStop() {
    return this.clusterManager.rxStop()
        .doOnComplete(() -> ControllerService.removeConsumerId(consumerId));
  }

  private void initClients() {
    this.webClient = WebClient.create(vertx);
    ContextUtils.setInstance(webClient);

    this.producer = getKafkaProducer();
    ContextUtils.setInstance(producer);
  }

  private KafkaProducer<String, String> getKafkaProducer() {
    ProducerConfig producerConfig = ConfigReader.readProducerConfigFromFile();
    return KafkaProducer.create(vertx, ProducerUtils.getProperties(producerConfig));
  }
}
