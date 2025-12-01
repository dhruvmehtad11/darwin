package com.dream11.consumer.verticle;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.consumer.service.ClusterManager;
import com.dream11.consumer.service.ConsumerMailBox;
import com.dream11.consumer.service.ConsumerManager;
import com.dream11.consumer.service.ControllerService;
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
import org.apache.helix.InstanceType;
import org.apache.kafka.clients.admin.AdminClient;
import java.util.UUID;

import static com.dream11.core.config.ConfigReader.readKafkaAdminConfigFromFile;

public class HelixParticipantConsumerVerticle extends AbstractVerticle {
  private final String consumerId = UUID.randomUUID().toString();

  @NonFinal private ConsumerManager consumerManager;
  @NonFinal  private ClusterManager clusterManager;

  @NonFinal
  private WebClient webClient;
  @NonFinal private KafkaConsumer<String, String> consumer;
  @NonFinal private KafkaProducer<String, String> producer;
  @NonFinal private AdminClient kafkaAdminClient;
  @NonFinal private ConsumerMailBox consumerMailBox;

  @Override
  public Completable rxStart() {
    HelixConfig config = ConfigReader.readHelixConfigFromFile();
    config.setClusterName(AppContext.getInstance(ApplicationConfig.class).getConsumerHelixClusterName());
    config.setInstanceName(consumerId);

    this.clusterManager = ClusterManager.create(vertx, config, InstanceType.PARTICIPANT);
    ContextUtils.setInstance(clusterManager);

    this.consumerMailBox = ConsumerMailBox.create(vertx, clusterManager);
    ContextUtils.setInstance(consumerMailBox);

    initClients();

    return clusterManager
        .rxStart()
        .andThen(consumerManager.rxStart())
        .doOnComplete(() -> {
          consumerMailBox.start(consumerId, this::rxStop);
          ControllerService.addConsumerId(consumerId);
        });
  }

  @Override
  public Completable rxStop() {
    return this.clusterManager.rxStop()
        .andThen(consumerManager.rxStop())
        .doOnComplete(() -> ControllerService.removeConsumerId(consumerId));
  }

  private void initClients() {
    this.webClient = WebClient.create(vertx);
    ContextUtils.setInstance(webClient);

    KafkaAdminConfig kafkaAdminConfig = readKafkaAdminConfigFromFile();
    this.kafkaAdminClient = AdminClient.create(KafkaAdminUtils.getProperties(kafkaAdminConfig));
    ContextUtils.setInstance(kafkaAdminClient);

    this.consumer = getKafkaConsumer();
    ContextUtils.setInstance(consumer);

    this.producer = getKafkaProducer();
    ContextUtils.setInstance(producer);

    this.consumerManager = ConsumerManager.create(consumerId, clusterManager);
  }

  private KafkaConsumer<String, String> getKafkaConsumer() {
    ConsumerConfig config = ConfigReader.readConsumerConfigFromFile();
    config.setClientId(consumerId);
    return KafkaConsumer.create(vertx, ConsumerUtils.getProperties(config));
  }

  private KafkaProducer<String, String> getKafkaProducer() {
    ProducerConfig producerConfig = ConfigReader.readProducerConfigFromFile();
    return KafkaProducer.create(vertx, ProducerUtils.getProperties(producerConfig));
  }
}
