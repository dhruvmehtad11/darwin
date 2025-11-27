package com.dream11.admin.service;

import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.KafkaAdminConfig;
import com.dream11.core.dto.kafka.AllKafkaTopicConfigResponse;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.core.util.KafkaAdminUtils;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import io.vertx.reactivex.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.ConfigEntry;
import com.dream11.core.dto.kafka.KafkaTopicMetadata;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;

import static com.dream11.core.config.ConfigReader.readKafkaAdminConfigFromFile;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class KafkaAdminService {
  private final ApplicationConfig applicationConfig;

  // doing this to avoid kafka dependency at bootstrap
  public Completable createTopic(String topicName, Integer numPartitions) {
    KafkaAdminConfig kafkaAdminConfig = readKafkaAdminConfigFromFile();
    AdminClient kafkaAdminClient = AdminClient.create(KafkaAdminUtils.getProperties(kafkaAdminConfig));

    return Completable.fromFuture(
            kafkaAdminClient
                .createTopics(
                    List.of(
                        new NewTopic(
                            topicName,
                            numPartitions,
                            applicationConfig.getDefaultKafkaTopicReplicationFactor())
                            .configs(
                                Map.of(
                                    "retention.ms",
                                    applicationConfig
                                        .getDefaultKafkaTopicRetentionMs()
                                        .toString()))))
                .all())
        .onErrorResumeNext(
            e ->
                Completable.error(
                    new ApiRestException(
                        e.getMessage(), ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)))
        .doFinally(kafkaAdminClient::close);
  }

  public Completable updateTopicPartitions(String topicName, Integer numPartitions) {
    KafkaAdminConfig kafkaAdminConfig = readKafkaAdminConfigFromFile();
    AdminClient kafkaAdminClient = AdminClient.create(KafkaAdminUtils.getProperties(kafkaAdminConfig));

    return Completable.create(
            emitter ->
                kafkaAdminClient
                    .createPartitions(Map.of(topicName, NewPartitions.increaseTo(numPartitions)))
                    .all()
                    .whenComplete(
                        (r, e) -> {
                          if (e != null) {
                            emitter.onError(e);
                          } else {
                            emitter.onComplete();
                          }
                        }))
        .onErrorResumeNext(
            e ->
                Completable.error(
                    new ApiRestException(
                        e.getMessage(), ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)))
        .doFinally(kafkaAdminClient::close);
  }

  @SneakyThrows
  public Single<AllKafkaTopicConfigResponse> getKafkaTopicConfigs() {
    KafkaAdminConfig kafkaAdminConfig = readKafkaAdminConfigFromFile();
    AdminClient kafkaAdminClient = AdminClient.create(KafkaAdminUtils.getProperties(kafkaAdminConfig));

    return
        Single.<Set<String>>create(emitter ->
                kafkaAdminClient.listTopics().names().whenComplete((result, error) -> {
                  if (error != null) {
                    emitter.onError(new ApiRestException(error.getMessage(), ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION));
                  } else {
                    emitter.onSuccess(result);
                  }
                }))
            .flatMap(topics -> getTopicMetadata(kafkaAdminClient, topics))
            .map(kafkaTopicMetadataList -> AllKafkaTopicConfigResponse.builder()
                .kafkaTopicMetadata(kafkaTopicMetadataList).build())
            .onErrorResumeNext(e -> Single.error(new ApiRestException(e.getMessage(), ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)))
            .doFinally(kafkaAdminClient::close);

  }

  private Single<List<KafkaTopicMetadata>> getTopicMetadata(AdminClient kafkaAdminClient, Set<String> topics) {
    return Single.<Map<String, TopicDescription>>create(emitter -> kafkaAdminClient.describeTopics(topics).all()
        .whenComplete((result, error) -> {
          if (error != null) {
            log.error("Error while fetching topic metadata", error);
            emitter.onError(new ApiRestException(error.getMessage(), ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION));
          } else {
            emitter.onSuccess(result);
          }
        })).zipWith(getTopicRetentionMs(kafkaAdminClient, topics), (describeTopicsResult, topicRetentionMsMap) ->
        describeTopicsResult.entrySet().stream()
            .map(entry -> KafkaTopicMetadata.builder()
                .topicName(entry.getKey())
                .partitions(entry.getValue().partitions().size())
                .replicationFactor(entry.getValue().partitions().get(0).replicas().size())
                .retentionMs(topicRetentionMsMap.get(entry.getKey()))
                .build())
            .collect(Collectors.toList())
    );
  }


  private Single<Map<String, Long>> getTopicRetentionMs(AdminClient kafkaAdminClient, Set<String> topics) {
    List<ConfigResource> resources =
        topics.stream().map(topic -> new ConfigResource(ConfigResource.Type.TOPIC, topic)).collect(Collectors.toList());
    Map<String, Long> retentionMsMap = new HashMap<>();
    return Single.create(emitter -> kafkaAdminClient.describeConfigs(resources).all().thenApply(configs -> {
      configs.forEach((resource, config) -> {
        String topicName = resource.name();
        ConfigEntry retentionConfig = config.get("retention.ms");
        if (retentionConfig != null) {
          retentionMsMap.put(topicName, Long.parseLong(retentionConfig.value()));
        }
      });
      return retentionMsMap;
    }).whenComplete((result, error) -> {
      if (error != null) {
        log.error("Error while fetching topic retention ms", error);
        emitter.onError(new ApiRestException(error.getMessage(), ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION));
      } else {
        emitter.onSuccess(result);
      }
    }));
  }
}
