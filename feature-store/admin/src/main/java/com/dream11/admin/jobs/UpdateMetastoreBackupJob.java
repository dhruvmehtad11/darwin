package com.dream11.admin.jobs;

import com.dream11.admin.service.CassandraMetaStoreService;
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
@CronJob(service = "darwin-ofs-v2-admin", schedule = "0 * * * * ?") // every 60 sec
public class UpdateMetastoreBackupJob extends AbstractCronJob {
  private final CassandraMetaStoreService cassandraMetaStoreService;
  private final S3MetastoreService s3MetastoreService;

  @Override
  public Completable handle() {
    long updatesSince =
        System.currentTimeMillis() - 2 * 60 * 1_000; // job's every 1 min so updates in last 2 min

    Completable entityUpdates =
        cassandraMetaStoreService
            .getUpdatedCassandraEntities(updatesSince)
            .flatMap(r -> s3MetastoreService.putEntityMetadata(r).andThen(Observable.just(r)))
            .toList()
            .filter(r -> !r.isEmpty())
            .map(r -> AllCassandraEntityResponse.builder().entities(r).build())
            .flatMapCompletable(s3MetastoreService::putAllEntityMetadata);

    Completable fgUpdates =
        cassandraMetaStoreService
            .getUpdatedCassandraFeatureGroups(updatesSince)
            .flatMap(r -> s3MetastoreService.putFeatureGroupMetadata(r).andThen(Observable.just(r)))
            .toList()
            .filter(r -> !r.isEmpty())
            .map(r -> AllCassandraFeatureGroupResponse.builder().featureGroups(r).build())
            .flatMapCompletable(s3MetastoreService::putAllFeatureGroupMetadata);

    Completable fgVersionUpdates =
        cassandraMetaStoreService
            .getUpdatedCassandraFeatureGroupVersions(updatesSince)
            .flatMap(
                r ->
                    s3MetastoreService
                        .putFeatureGroupVersionMetadata(r)
                        .andThen(Observable.just(r)))
            .toList()
            .filter(r -> !r.isEmpty())
            .map(r -> AllCassandraFeatureGroupVersionResponse.builder().versions(r).build())
            .flatMapCompletable(s3MetastoreService::putAllFeatureGroupVersionMetadata);

    Completable fgSchemaUpdates =
        cassandraMetaStoreService
            .getUpdatedCassandraFeatureGroupSchemas(updatesSince)
            .flatMapCompletable(
                r ->
                    s3MetastoreService.putFeatureGroupSchema(
                        r.getName(), r.getVersion(), r.getSchema()));

    Completable fgTopicUpdates =
        cassandraMetaStoreService
            .getUpdatedCassandraFeatureGroups(updatesSince)
            .flatMapCompletable(
                fg ->
                    cassandraMetaStoreService
                        .getFeatureGroupTenantTopic(fg.getName())
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

    // cron library handles errors internally
    return Completable.merge(
        List.of(entityUpdates, fgUpdates, fgVersionUpdates, fgSchemaUpdates, fgTopicUpdates));
  }
}
