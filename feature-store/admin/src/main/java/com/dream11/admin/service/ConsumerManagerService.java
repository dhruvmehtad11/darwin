package com.dream11.admin.service;

import static com.dream11.core.constant.Constants.*;

import com.dream11.admin.dao.ConsumersManagerDao;
import com.dream11.core.dto.consumer.AddFeatureGroupConsumerMetadataRequest;
import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import com.dream11.core.dto.consumer.FeatureGroupConsumerMap;
import com.dream11.core.dto.consumer.UpdateConsumerCountRequest;
import com.dream11.core.dto.consumer.UpdateTenantTopicRequest;
import com.dream11.core.dto.consumer.UpdateTopicPartitionRequest;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observables.GroupedObservable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsumerManagerService {
  private final KafkaAdminService kafkaAdminService;
  private final ConsumersManagerDao consumersManagerDao;
  private final CassandraMetaStoreService cassandraMetaStoreService;

  public Completable onboardFeatureGroupConsumer(AddFeatureGroupConsumerMetadataRequest request) {
    return cassandraMetaStoreService
            .getAllFeatureGroups()
            .groupBy(r -> r.getTenant().getConsumerTenant())
            .map(GroupedObservable::getKey)
            .filter(r -> r.equals(request.getTenantName()))
            .firstElement()
            .switchIfEmpty(
                    Single.error(new ApiRestException(ServiceError.OFS_V2_TENANT_NOT_FOUND_EXCEPTION)))
            .flatMapCompletable(
                    r ->
                            consumersManagerDao.atomicallyAddConsumerMetadata(
                                    FeatureGroupConsumerMap.builder()
                                            .tenantName(request.getTenantName())
                                            .topicName(request.getTopicName())
                                            .numPartitions(request.getNumPartitions())
                                            .numConsumers(DEFAULT_INIT_CONSUMERS)
                                            .build(),
                                    kafkaAdminService::createTopic));
  }

  public Completable registerConsumer(FeatureGroupConsumerMap featureGroupConsumerMap) {
    return consumersManagerDao
            .addConsumerMetadata(
                    FeatureGroupConsumerMap.builder()
                            .tenantName(featureGroupConsumerMap.getTenantName())
                            .topicName(featureGroupConsumerMap.getTopicName())
                            .numPartitions(featureGroupConsumerMap.getNumPartitions())
                            .numConsumers(DEFAULT_INIT_CONSUMERS)
                            .build())
            .onErrorResumeNext(
                    e -> {
                      log.error("error registering consumer", e);
                      return Completable.error(
                              new ApiRestException(
                                      e.getMessage(), ServiceError.OFS_V2_CONSUMER_REGISTRATION_EXCEPTION));
                    });
  }

  public Single<List<ConsumerGroupMetadata>> getAllConsumerMetadata() {
    return consumersManagerDao
            .getAllConsumersMetadata()
            .map(
                    r ->
                            ConsumerGroupMetadata.builder()
                                    .tenantName(r.getTenantName())
                                    .topicName(r.getTopicName())
                                    .numConsumers(r.getNumConsumers())
                                    .build())
            .toList();
  }

  public Completable updateConsumerCount(UpdateConsumerCountRequest request) {
    return consumersManagerDao.updateConsumerCount(request);
  }

  public Completable updateTopicPartitions(UpdateTopicPartitionRequest request) {
    return consumersManagerDao.updateTopicPartitions(
            request, kafkaAdminService::updateTopicPartitions);
  }

  public Completable updateTopicName(UpdateTenantTopicRequest request) {
    return consumersManagerDao.updateTenantTopic(request);
  }
}