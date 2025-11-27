package com.dream11.populator.statemodel;

import com.dream11.common.util.ContextUtils;
import com.dream11.populator.deltareader.TableReader;
import com.dream11.populator.model.TableConfig;
import com.dream11.populator.model.TableFileState;
import com.dream11.populator.service.OfsKafkaWriterService;
import io.delta.kernel.data.Row;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Context;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.helix.NotificationContext;
import org.apache.helix.model.Message;
import org.apache.helix.participant.statemachine.StateModel;
import org.apache.helix.participant.statemachine.StateModelFactory;

@Slf4j
public class WorkerStateModelFactory extends StateModelFactory<StateModel> {
  private final String instanceName;
  private final Context context;
  private final Integer parallelism;

  public WorkerStateModelFactory(Context context, String instanceName, Integer parallelism) {
    this.instanceName = instanceName;
    this.context = context;
    this.parallelism = parallelism;
  }

  @Override
  public StateModel createNewStateModel(String resourceName, String partitionName) {
    log.debug(
        "Creating new StateModel for resource: {} and partition: {}", resourceName, partitionName);
    return new OnlineOfflineStateModel(
        context, instanceName, resourceName, partitionName, parallelism);
  }

  public static class OnlineOfflineStateModel extends StateModel {
    private final OfsKafkaWriterService ofsKafkaWriterService;
    private final String instanceName;
    private final String partitionName;
    private final String resourceName;
    private final Context context;
    private final TableReader tableReader;
    private final Integer partitionId;
    private final Integer parallelism;
    private Long filePoller;

    public OnlineOfflineStateModel(
        Context context,
        String instanceName,
        String resourceName,
        String partitionName,
        Integer parallelism) {
      super();
      this.instanceName = instanceName;
      this.resourceName = resourceName;
      this.partitionName = partitionName;
      this.context = context;
      log.info(
          "Initialized OnlineOfflineStateModel for instance: {}, resource: {}, partition: {}",
          instanceName,
          resourceName,
          partitionName);
      this.tableReader = ContextUtils.getInstance(context, TableReader.class);
      this.ofsKafkaWriterService = ContextUtils.getInstance(context, OfsKafkaWriterService.class);
      this.partitionId =
          Integer.parseInt(partitionName.split("_")[partitionName.split("_").length - 1]);
      this.parallelism = parallelism;
    }

    public void onBecomeOnlineFromOffline(Message message, NotificationContext context)
        throws Exception {
      log.info(
          "Transitioning from OFFLINE to ONLINE for resource: {}, partition: {}",
          resourceName,
          partitionName);
      this.startWorker();
    }

    public void onBecomeOfflineFromOnline(Message message, NotificationContext context) {
      log.info(
          "Transitioning from ONLINE to OFFLINE for resource: {}, partition: {}",
          resourceName,
          partitionName);
      if (filePoller != null) {
        this.context.owner().cancelTimer(filePoller);
        filePoller = null;
      }
    }

    public void onBecomeDroppedFromOffline(Message message, NotificationContext context) {
      log.warn(
          "Transitioning from OFFLINE to DROPPED for resource: {}, partition: {}",
          resourceName,
          partitionName);
      // Add cleanup logic here if necessary.
    }

    private void startWorker() {
      AtomicInteger running = new AtomicInteger(0);
      filePoller =
          this.context
              .owner()
              .setPeriodic(
                  1_000,
                  l -> {
                    if (running.compareAndSet(0, 1)) {
                      context.runOnContext(v -> read(running));
                    }
                  });
    }

    private void read(AtomicInteger running) {
      Flowable.fromIterable(tableReader.getFilesForCurrentPartition(resourceName, partitionId))
          .parallel(parallelism)
          .concatMap(
              tableFileState ->
                  tableReader
                      .getScanFileFromS3(tableFileState.getTableName(), tableFileState.getFileId())
                      .flatMapCompletable(
                          scanFile ->
                              tableReader
                                  .rxUpdateFileState(
                                      tableFileState.getTableName(),
                                      tableFileState.getFileId(),
                                      TableFileState.State.RUNNING)
                                  .andThen(
                                      tableReader.rxProcessBatch(
                                          tableFileState.getTableName(),
                                          scanFile.getStateJson(),
                                          scanFile.getFileJson()))
                                  .flatMapCompletable(
                                      row -> this.processRow(row, tableFileState.getTableName()))
                                  .andThen(
                                      tableReader.rxUpdateFileState(
                                          tableFileState.getTableName(),
                                          tableFileState.getFileId(),
                                          TableFileState.State.COMPLETED)))
                      .onErrorResumeNext(
                          e -> {
                            log.error(
                                "error processing batch for table: {} file: {}",
                                tableFileState.getTableName(),
                                tableFileState.getFileId(),
                                e);
                            return tableReader.rxUpdateFileState(
                                tableFileState.getTableName(),
                                tableFileState.getFileId(),
                                TableFileState.State.FAILED);
                          })
                      .toFlowable())
          .sequential()
          .doFinally(() -> running.set(0))
          .subscribe();
    }

    private Completable processRow(Row row, String tableName) {
      TableConfig tableConfig = tableReader.getTableFactory().get(tableName);
      String featureGroupName = tableConfig.getFeatureGroupName();
      String featureGroupVersion = tableConfig.getFeatureGroupVersion();
      String topicName = tableConfig.getTopicName();
      return ofsKafkaWriterService.sendRecordToKafka(
          row,
          tableConfig.getSchema(),
          featureGroupName,
          featureGroupVersion,
          topicName,
          tableConfig.getRunId(),
          tableConfig.getReplicateWrites(),
          instanceName,
          tableName);
    }
  }
}
