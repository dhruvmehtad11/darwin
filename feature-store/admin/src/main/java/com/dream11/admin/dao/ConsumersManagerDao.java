package com.dream11.admin.dao;

import static com.dream11.core.constant.query.MysqlQuery.getAllCassandraStoreTenantConsumerDetails;
import static com.dream11.core.constant.query.MysqlQuery.getCassandraStoreTenantConsumerDetails;
import static com.dream11.core.constant.query.MysqlQuery.insertIntoCassandraStoreTenantConsumerDetails;
import static com.dream11.core.constant.query.MysqlQuery.selectCassandraStoreTenantConsumerDetailsForUpdate;
import static com.dream11.core.constant.query.MysqlQuery.updateCassandraStoreTenantConsumerTopic;
import static com.dream11.core.constant.query.MysqlQuery.updateCassandraStoreTenantNumConsumers;
import static com.dream11.core.constant.query.MysqlQuery.updateCassandraStoreTenantNumPartitions;

import com.dream11.core.dto.consumer.FeatureGroupConsumerMap;
import com.dream11.core.dto.consumer.UpdateConsumerCountRequest;
import com.dream11.core.dto.consumer.UpdateTenantTopicRequest;
import com.dream11.core.dto.consumer.UpdateTopicPartitionRequest;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Transaction;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ConsumersManagerDao {
  private final MysqlClient d11MysqlClient;
  private final ObjectMapper objectMapper;

  public Single<Transaction> getMysqlTransaction() {
    return d11MysqlClient.getMasterMysqlClient().rxBegin();
  }

  public Completable atomicallyAddConsumerMetadata(
      FeatureGroupConsumerMap featureGroupConsumerMap,
      BiFunction<String, Integer, Completable> createTopicFunction) {
    return getMysqlTransaction()
        .flatMapCompletable(
            tx ->
                addConsumerMetadata(tx, featureGroupConsumerMap)
                    .andThen(
                        createTopicFunction.apply(
                            featureGroupConsumerMap.getTopicName(),
                            featureGroupConsumerMap.getNumPartitions()))
                    .andThen(tx.rxCommit())
                    .onErrorResumeNext(
                        e -> {
                          log.error(
                              String.format("error adding consumer metadata: %s", e.getMessage()),
                              e);
                          return tx.rxRollback()
                              .andThen(
                                  Completable.error(
                                      new ApiRestException(
                                          e.getMessage(),
                                          ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)));
                        }));
  }

  public Completable addConsumerMetadata(
      Transaction tx, FeatureGroupConsumerMap featureGroupConsumerMap) {
    return checkIfMetadataExists(featureGroupConsumerMap)
        .flatMapCompletable(
            ignore ->
                tx.preparedQuery(insertIntoCassandraStoreTenantConsumerDetails)
                    .rxExecute(
                        Tuple.of(
                            featureGroupConsumerMap.getTenantName(),
                            featureGroupConsumerMap.getTopicName(),
                            featureGroupConsumerMap.getNumPartitions(),
                            featureGroupConsumerMap.getNumConsumers()))
                    .ignoreElement());
  }

  public Completable addConsumerMetadata(FeatureGroupConsumerMap featureGroupConsumerMap) {
    return checkIfMetadataExists(featureGroupConsumerMap)
        .flatMapCompletable(
            ignore ->
                d11MysqlClient
                    .getMasterMysqlClient()
                    .preparedQuery(insertIntoCassandraStoreTenantConsumerDetails)
                    .rxExecute(
                        Tuple.of(
                            featureGroupConsumerMap.getTenantName(),
                            featureGroupConsumerMap.getTopicName(),
                            featureGroupConsumerMap.getNumPartitions(),
                            featureGroupConsumerMap.getNumConsumers()))
                    .ignoreElement());
  }

  private Single<Boolean> checkIfMetadataExists(FeatureGroupConsumerMap featureGroupConsumerMap) {
    return getConsumerMetadata(featureGroupConsumerMap.getTenantName())
        .isEmpty()
        .filter(r -> r)
        .switchIfEmpty(
            Single.error(
                new ApiRestException(
                    "consumer metadata already exists",
                    ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)));
  }

  public Observable<FeatureGroupConsumerMap> getAllConsumersMetadata() {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getAllCassandraStoreTenantConsumerDetails)
        .rxExecute()
        .map(MysqlUtils::rowSetToJsonList)
        .flattenAsObservable(r -> r)
        .map(r -> objectMapper.readValue(r.toString(), FeatureGroupConsumerMap.class));
  }

  public Maybe<FeatureGroupConsumerMap> getConsumerMetadata(String tenantName) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getCassandraStoreTenantConsumerDetails)
        .rxExecute(Tuple.of(tenantName))
        .map(MysqlUtils::rowSetToJsonList)
        .flattenAsObservable(r -> r)
        .firstElement()
        .map(r -> objectMapper.readValue(r.toString(), FeatureGroupConsumerMap.class));
  }

  public Single<FeatureGroupConsumerMap> getConsumesMetadataForUpdate(
      Transaction tx, String tenantName) {
    return tx.preparedQuery(selectCassandraStoreTenantConsumerDetailsForUpdate)
        .rxExecute(Tuple.of(tenantName))
        .map(MysqlUtils::rowSetToJsonList)
        .flattenAsObservable(r -> r)
        .firstElement()
        .switchIfEmpty(
            Single.error(new ApiRestException(ServiceError.OFS_V2_TENANT_NOT_FOUND_EXCEPTION)))
        .map(r -> objectMapper.readValue(r.toString(), FeatureGroupConsumerMap.class));
  }

  public Completable updateConsumerCount(Transaction tx, String tenantName, Integer count) {
    return tx.preparedQuery(updateCassandraStoreTenantNumConsumers)
        .rxExecute(Tuple.of(count, tenantName))
        .ignoreElement();
  }

  public Completable updateTenantTopic(Transaction tx, String tenantName, String topicName) {
    return tx.preparedQuery(updateCassandraStoreTenantConsumerTopic)
        .rxExecute(Tuple.of(topicName, tenantName))
        .ignoreElement();
  }

  public Completable updateTenantTopic(UpdateTenantTopicRequest request) {
    return getMysqlTransaction()
        .flatMapCompletable(
            tx ->
                getConsumesMetadataForUpdate(tx, request.getTenantName())
                    .flatMapCompletable(
                        entry ->
                            updateTenantTopic(tx, request.getTenantName(), request.getTopicName()))
                    .andThen(tx.rxCommit())
                    .onErrorResumeNext(
                        e -> {
                          log.error(
                              String.format(
                                  "error updating consumer tenant topic: %s", e.getMessage()),
                              e);
                          return tx.rxRollback().andThen(Completable.error(e));
                        }));
  }

  public Completable updateConsumerCount(UpdateConsumerCountRequest request) {
    return getMysqlTransaction()
        .flatMapCompletable(
            tx ->
                getConsumesMetadataForUpdate(tx, request.getTenantName())
                    .filter(r -> request.getNumConsumers() <= r.getNumPartitions())
                    .switchIfEmpty(
                        Single.error(
                            new ApiRestException(
                                "consumer count cannot be greater than num partitions",
                                ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)))
                    .flatMapCompletable(
                        entry ->
                            updateConsumerCount(
                                tx, request.getTenantName(), request.getNumConsumers()))
                    .andThen(tx.rxCommit())
                    .onErrorResumeNext(
                        e -> {
                          log.error(
                              String.format("error updating consumer count: %s", e.getMessage()),
                              e);
                          return tx.rxRollback().andThen(Completable.error(e));
                        }));
  }

  public Completable updateTopicPartitions(Transaction tx, String tenantName, Integer count) {
    return tx.preparedQuery(updateCassandraStoreTenantNumPartitions)
        .rxExecute(Tuple.of(count, tenantName))
        .ignoreElement();
  }

  public Completable updateTopicPartitions(
      UpdateTopicPartitionRequest request,
      BiFunction<String, Integer, Completable> updatePartitionsInKafka) {
    return getMysqlTransaction()
        .flatMapCompletable(
            tx ->
                getConsumesMetadataForUpdate(tx, request.getTenantName())
                    .flatMapCompletable(
                        featureGroupConsumerMap ->
                            updatePartitionsInKafka.apply(
                                featureGroupConsumerMap.getTopicName(), request.getNumPartitions()))
                    .andThen(
                        updateTopicPartitions(
                            tx, request.getTenantName(), request.getNumPartitions()))
                    .andThen(tx.rxCommit())
                    .onErrorResumeNext(
                        e -> {
                          log.error(
                              String.format("error updating topic partitions: %s", e.getMessage()),
                              e);
                          return tx.rxRollback()
                              .andThen(
                                  Completable.error(
                                      new ApiRestException(
                                          e.getMessage(),
                                          ServiceError.OFS_V2_KAFKA_ADMIN_EXCEPTION)));
                        }));
  }
}
