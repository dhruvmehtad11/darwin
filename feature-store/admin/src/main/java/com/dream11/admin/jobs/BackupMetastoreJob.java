package com.dream11.admin.jobs;

import com.dream11.admin.service.CassandraMetaStoreService;
import com.dream11.admin.service.ConsumerManagerService;
import com.dream11.admin.service.KafkaAdminService;
import com.dream11.core.dto.consumer.AllConsumerGroupMetadataResponse;
import com.dream11.core.dto.kafka.AllKafkaTopicConfigResponse;
import com.dream11.core.dto.response.AllCassandraEntityResponse;
import com.dream11.core.dto.response.AllCassandraFeatureGroupResponse;
import com.dream11.core.dto.response.AllCassandraFeatureGroupVersionResponse;
import com.dream11.core.dto.response.GetTopicResponse;
import com.dream11.core.service.S3MetastoreService;
import com.dream11.job.AbstractCronJob;
import com.dream11.job.CronJob;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@CronJob(service = "darwin-ofs-v2-admin", schedule = "0 0 0 * * ?") // every day at midnight
public class BackupMetastoreJob extends AbstractCronJob {
  private final CassandraMetaStoreService cassandraMetaStoreService;
  private final S3MetastoreService s3MetastoreService;
  private final ConsumerManagerService consumerManagerService;
  private final KafkaAdminService kafkaAdminService;

  @Override
  public Completable handle() {
    Completable entityBackup =
        cassandraMetaStoreService
            .getAllEntities()
            .flatMap(r -> s3MetastoreService.putEntityMetadata(r).andThen(Observable.just(r)))
            .toList()
            .map(r -> AllCassandraEntityResponse.builder().entities(r).build())
            .flatMapCompletable(s3MetastoreService::putAllEntityMetadata);

    Completable fgBackup =
        cassandraMetaStoreService
            .getAllFeatureGroups()
            .flatMap(r -> s3MetastoreService.putFeatureGroupMetadata(r).andThen(Observable.just(r)))
            .toList()
            .map(r -> AllCassandraFeatureGroupResponse.builder().featureGroups(r).build())
            .flatMapCompletable(s3MetastoreService::putAllFeatureGroupMetadata);

    Completable fgVersionBackup =
        cassandraMetaStoreService
            .getAllFeatureGroupVersion()
            .flatMap(
                r ->
                    s3MetastoreService
                        .putFeatureGroupVersionMetadata(r)
                        .andThen(Observable.just(r)))
            .toList()
            .map(r -> AllCassandraFeatureGroupVersionResponse.builder().versions(r).build())
            .flatMapCompletable(s3MetastoreService::putAllFeatureGroupVersionMetadata);

    Completable fgSchemaBackup =
        cassandraMetaStoreService
            .getAllCassandraFeatureGroupSchema()
            .flatMapCompletable(
                r ->
                    s3MetastoreService.putFeatureGroupSchema(
                        r.getName(), r.getVersion(), r.getSchema()));

    Completable fgTopicBackup =
        cassandraMetaStoreService
            .getAllFeatureGroupsFromCache()
            .flatMapCompletable(
                fg ->
                    cassandraMetaStoreService.getFeatureGroupTenantTopic(fg.getName())
                        .map(
                            topic ->
                                GetTopicResponse.builder()
                                    .featureGroupName(fg.getName())
                                    .topic(topic)
                                    .build())
                        .flatMapCompletable(
                            getTopicResponse ->
                                s3MetastoreService.putFeatureGroupTopic(
                                    fg.getName(), getTopicResponse)));

    Completable consumerGroupConfigBackup =
        consumerManagerService.getAllConsumerMetadata()
            .map(listOfConsumerGroup -> AllConsumerGroupMetadataResponse.builder()
                .consumerGroupMetadata(listOfConsumerGroup).build()).flatMapCompletable(s3MetastoreService::putConsumerGroupConfig);

    Completable kafkaTopicConfigBackup =
        kafkaAdminService.getKafkaTopicConfigs().flatMapCompletable(s3MetastoreService::putKafkaTopicConfig);

    // cron library handles errors internally
    return Completable.merge(
        List.of(entityBackup, fgBackup, fgVersionBackup, fgSchemaBackup, fgTopicBackup, consumerGroupConfigBackup, kafkaTopicConfigBackup));
  }
}
