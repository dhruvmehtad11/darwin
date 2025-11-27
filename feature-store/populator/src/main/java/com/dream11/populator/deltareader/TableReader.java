package com.dream11.populator.deltareader;

import static com.dream11.populator.util.HadoopUtils.getDefaultConf;
import static io.delta.kernel.internal.util.Utils.singletonCloseableIterator;

import com.dream11.common.app.AppContext;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.util.S3ClientUtils;
import com.dream11.populator.model.TableConfig;
import com.dream11.populator.model.TableFileState;
import com.dream11.populator.model.TableReaderMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.delta.kernel.*;
import io.delta.kernel.data.ColumnarBatch;
import io.delta.kernel.data.Row;
import io.delta.kernel.defaults.engine.DefaultEngine;
import io.delta.kernel.engine.Engine;
import io.delta.kernel.internal.DeltaLogActionUtils;
import io.delta.kernel.internal.TableImpl;
import io.delta.kernel.internal.data.ScanStateRow;
import io.delta.kernel.internal.fs.Path;
import io.delta.kernel.types.StructType;
import io.delta.kernel.utils.CloseableIterator;
import io.delta.kernel.utils.FileStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.helix.AccessOption;
import org.apache.helix.HelixManager;
import org.apache.helix.InstanceType;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.zookeeper.datamodel.ZNRecord;
import org.apache.zookeeper.data.Stat;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Data
@Slf4j
public class TableReader implements Shareable {
  // todo: might need to add compute if absent from zk
  @Getter
  private final ConcurrentHashMap<String, TableConfig> tableFactory = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<String, ConcurrentHashSet<String>> tenantTableMap =
      new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper = AppContext.getInstance(ObjectMapper.class);
  private final ConcurrentHashMap<String, AtomicBoolean> tableProcessor = new ConcurrentHashMap<>();

  private final Vertx vertx;
  private final HelixManager manager;
  private final ZKHelixAdmin zkHelixAdmin;
  private final Engine engine;
  private final ApplicationConfig applicationConfig;

  private final S3AsyncClient s3AsyncClient;

  public TableReader(
      Vertx vertx,
      HelixManager manager,
      ZKHelixAdmin zkHelixAdmin,
      ApplicationConfig applicationConfig) {
    this.vertx = vertx;
    this.manager = manager;
    this.zkHelixAdmin = zkHelixAdmin;
    Configuration configuration = getDefaultConf();
    this.engine = DefaultEngine.create(configuration);
    this.applicationConfig = applicationConfig;
    this.s3AsyncClient = S3ClientUtils.createS3Client();
  }

  public void start(InstanceType instanceType) {
    vertx.setPeriodic(
        5_000,
        l -> {
          vertx
              .rxExecuteBlocking(
                  promise -> {
                    try {
                      refreshTableFactory();
                      // only leader processes table data
                      if (instanceType == InstanceType.CONTROLLER && manager.isLeader())
                        tableFactory.keySet().forEach(this::processTableMetadata);
                      promise.complete();
                    } catch (Exception e) {
                      log.error(e.getMessage(), e);
                      promise.fail(e);
                    }
                  })
              .subscribe();
        });
  }


  private void refreshTableFactory() {
    List<String> tables =
        manager
            .getHelixPropertyStore()
            .getChildNames("/METADATA_ROOT_PATH", AccessOption.PERSISTENT);
    if (tables == null) return;

    for (String table : tables) {
      TableReaderMetadata readerMetadata = getTableReaderMetadata(table);
      if (!tenantTableMap.containsKey(readerMetadata.getTenantName()))
        tenantTableMap.put(readerMetadata.getTenantName(), new ConcurrentHashSet<>());

      tenantTableMap.get(readerMetadata.getTenantName()).add(readerMetadata.getName());
      tableFactory.put(
          readerMetadata.getName(),
          TableConfig.builder()
              .name(readerMetadata.getName())
              .path(readerMetadata.getPath())
              .table(TableImpl.forPath(engine, readerMetadata.getPath()))
              .schema(getTableSchema(readerMetadata.getPath()))
              .featureGroupName(readerMetadata.getFeatureGroupName())
              .featureGroupVersion(readerMetadata.getFeatureGroupVersion())
              .topicName(readerMetadata.getTopicName())
              .runId(readerMetadata.getRunId())
              .replicateWrites(readerMetadata.getReplicateWrites())
              .startVersion(readerMetadata.getStartVersion())
              .endVersion(readerMetadata.getEndVersion())
              .build());
    }
  }

  public StructType getTableSchema(String path) {
    Table table = Table.forPath(engine, path);
    return table.getLatestSnapshot(engine).getSchema(engine);
  }

  public StructType getTableSchema(Table table) {
    return table.getLatestSnapshot(engine).getSchema(engine);
  }

  public Long getLatestVersion(String path) {
    Table table = Table.forPath(engine, path);
    return table.getLatestSnapshot(engine).getVersion(engine);
  }

  public Long getVersionAsOfTimestamp(String path, Long timestamp) {
    Table table = Table.forPath(engine, path);
    return table.getSnapshotAsOfTimestamp(engine, timestamp).getVersion(engine);
  }

  public void setTableReaderMetadata(TableReaderMetadata tableReaderMetadata) {
    ZNRecord record = new ZNRecord("READER_METADATA");

    record.setSimpleField("name", tableReaderMetadata.getName());
    record.setSimpleField("path", tableReaderMetadata.getPath());
    record.setSimpleField("tenant-name", tableReaderMetadata.getTenantName());
    record.setSimpleField("feature-group-name", tableReaderMetadata.getFeatureGroupName());
    record.setSimpleField("feature-group-version", tableReaderMetadata.getFeatureGroupVersion());
    record.setSimpleField("topic-name", tableReaderMetadata.getTopicName());
    record.setSimpleField("run-id", tableReaderMetadata.getRunId());
    record.setBooleanField("replicate-writes", tableReaderMetadata.getReplicateWrites());
    record.setLongField("start-version", tableReaderMetadata.getStartVersion());
    record.setLongField("end-version", tableReaderMetadata.getEndVersion());

    if (!manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/CONFIG", tableReaderMetadata.getName()),
            AccessOption.PERSISTENT)) {
      manager
          .getHelixPropertyStore()
          .create(
              String.format("/METADATA_ROOT_PATH/%s/CONFIG", tableReaderMetadata.getName()),
              record,
              AccessOption.PERSISTENT);
    } else {
      resetTable(tableReaderMetadata.getName());
      manager
          .getHelixPropertyStore()
          .set(
              String.format("/METADATA_ROOT_PATH/%s/CONFIG", tableReaderMetadata.getName()),
              record,
              AccessOption.PERSISTENT);
    }
  }

  public void resetTable(String name){
    removeCurrentVersion(name);
    removeEndVersion(name);
    removeStateMap(name);
  }

  public TableReaderMetadata getTableReaderMetadata(String name) {
    if (!manager
        .getHelixPropertyStore()
        .exists(String.format("/METADATA_ROOT_PATH/%s/CONFIG", name), AccessOption.PERSISTENT)) {
      throw new RuntimeException(
          String.format("table reader metadata not found for table %s", name));
    }

    Stat stat = new Stat();
    ZNRecord record =
        manager
            .getHelixPropertyStore()
            .get(
                String.format("/METADATA_ROOT_PATH/%s/CONFIG", name),
                stat,
                AccessOption.PERSISTENT);

    String path = record.getSimpleField("path");
    String tenantName = record.getSimpleField("tenant-name");
    String featureGroupName = record.getSimpleField("feature-group-name");
    String featureGroupVersion = record.getSimpleField("feature-group-version");
    String topicName = record.getSimpleField("topic-name");
    String runId = record.getSimpleField("run-id");
    Boolean replicateWrites = record.getBooleanField("replicate-writes", true);
    Long startVersion = record.getLongField("start-version", -1);
    Long endVersion = record.getLongField("end-version", -1);
    return TableReaderMetadata.builder()
        .name(name)
        .path(path)
        .tenantName(tenantName)
        .featureGroupName(featureGroupName)
        .featureGroupVersion(featureGroupVersion)
        .topicName(topicName)
        .runId(runId)
        .replicateWrites(replicateWrites)
        .startVersion(startVersion)
        .endVersion(endVersion)
        .build();
  }

  private void processTableMetadata(String tableName) {
    TableConfig tableConfig = tableFactory.get(tableName);

    long currentVersion = getOrSetCurrentVersion(tableName, tableConfig.getStartVersion());
    long endVersion = getOrSetEndVersion(tableName, tableConfig.getEndVersion());
    AtomicBoolean processing =
        tableProcessor.computeIfAbsent(tableConfig.getName(), k -> new AtomicBoolean(false));
    try {
      if (currentVersion <= endVersion) {
        if (!processing.get()) {
          processCurrentSnapshot(tableName, currentVersion);
          processing.set(true);
        } else {
          List<TableFileState> map = getCurrentStateMap(tableName);
          if (isCurrentVersionDone(map)) {
            incrementCurrentVersion(tableName);
            processing.set(false);
          }
        }
      }
    } catch (Exception e) {
      log.error(
          String.format(
              "error reading snapshot version %s for table %s",
              currentVersion, tableConfig.getName()));
    }
  }

  private Long getOrSetCurrentVersion(String tableName, long startVersion) {
    if (!manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName), AccessOption.PERSISTENT)) {
      ZNRecord record = new ZNRecord("CURRENT_VERSION");
      record.setLongField("current-version", startVersion);
      manager
          .getHelixPropertyStore()
          .create(
              String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
              record,
              AccessOption.PERSISTENT);
      return startVersion;
    }
    Stat stat = new Stat();
    return manager
        .getHelixPropertyStore()
        .get(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
            stat,
            AccessOption.PERSISTENT)
        .getLongField("current-version", startVersion);
  }

  private void removeCurrentVersion(String tableName) {
    if (manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName), AccessOption.PERSISTENT)) {
      manager
          .getHelixPropertyStore()
          .remove(
              String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
              AccessOption.PERSISTENT);
    }
  }

  private Long getOrSetEndVersion(String tableName, long endVersion) {
    if (!manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName), AccessOption.PERSISTENT)) {
      ZNRecord record = new ZNRecord("END_VERSION");
      record.setLongField("end-version", endVersion);
      manager
          .getHelixPropertyStore()
          .create(
              String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
              record,
              AccessOption.PERSISTENT);
      return endVersion;
    }
    Stat stat = new Stat();
    return manager
        .getHelixPropertyStore()
        .get(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
            stat,
            AccessOption.PERSISTENT)
        .getLongField("end-version", endVersion);
  }

  private void removeEndVersion(String tableName) {
    if (manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName), AccessOption.PERSISTENT)) {
      manager
          .getHelixPropertyStore()
          .remove(
              String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
              AccessOption.PERSISTENT);
    }
  }

  private void incrementCurrentVersion(String tableName) {
    manager
        .getHelixPropertyStore()
        .update(
            String.format("/METADATA_ROOT_PATH/%s/VERSION", tableName),
            record -> {
              long currentVersion = record.getLongField("current-version", -1);
              currentVersion++;
              record.setLongField("current-version", currentVersion);
              return record;
            },
            AccessOption.PERSISTENT);
  }

  private void processCurrentSnapshot(String tableName, long currentVersion) {
    Table table = tableFactory.get(tableName).getTable();
    List<ScanFile> scanFiles = getScanFiles(table, engine, currentVersion);
    addNewStateMap(tableName, scanFiles);
  }

  private static List<ScanFile> getScanFiles(Table table, Engine engine, long version) {
    Snapshot currSnapshot = table.getSnapshotAsOfVersion(engine, version);
    Scan scan = currSnapshot.getScanBuilder(engine).build();
    Row scanState = scan.getScanState(engine);
    List<ScanFile> scanFiles = new ArrayList<>();

    CloseableIterator<ColumnarBatch> fileIter =
        ((TableImpl) table)
            .getChanges(engine, version, version, Set.of(DeltaLogActionUtils.DeltaAction.ADD));

    while (fileIter.hasNext()) {
      try (CloseableIterator<Row> scanFileRows = fileIter.next().getRows()) {
        while (scanFileRows.hasNext()) {
          Row scanFileRow = scanFileRows.next();
          if (!scanFileRow.isNullAt(
              scanFileRow.getSchema().indexOf(DeltaLogActionUtils.DeltaAction.ADD.colName)))
            scanFiles.add(
                new ScanFile(
                    RowSerDe.serializeRowToJson(scanState),
                    RowSerDe.serializeRowToJson(scanFileRow)));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return scanFiles;
  }

  private void removeStateMap(String tableName) {
    if (manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
            AccessOption.PERSISTENT)) {
      manager
          .getHelixPropertyStore()
          .remove(
              String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
              AccessOption.PERSISTENT);
    }
  }

  private void addNewStateMap(String tableName, List<ScanFile> files) {
    List<String> stateMap = initStateMap(tableName, files);
    ZNRecord record = new ZNRecord("CURRENT_STATE");

    record.setListField("current-state", stateMap);
    if (!manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
            AccessOption.PERSISTENT)) {
      manager
          .getHelixPropertyStore()
          .create(
              String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
              record,
              AccessOption.PERSISTENT);
    } else
      manager
          .getHelixPropertyStore()
          .set(
              String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
              record,
              AccessOption.PERSISTENT);
  }

  private List<TableFileState> getCurrentStateMap(String tableName) {
    if (!manager
        .getHelixPropertyStore()
        .exists(
            String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
            AccessOption.PERSISTENT)) {
      return new ArrayList<>();
    }
    Stat stat = new Stat();
    return manager
        .getHelixPropertyStore()
        .get(
            String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
            stat,
            AccessOption.PERSISTENT)
        .getListField("current-state")
        .stream()
        .map(
            r -> {
              try {
                return objectMapper.readValue(r, TableFileState.class);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            })
        .collect(Collectors.toList());
  }

  public List<String> getTablesForCurrentTenant(String tenantName) {
    return new ArrayList<>(tenantTableMap.getOrDefault(tenantName, new ConcurrentHashSet<>()));
  }

  public List<TableFileState> getFilesForCurrentPartition(String tenantName, Integer workerId) {
    List<TableFileState> partitionMap = new ArrayList<>();

    // todo: find this if this should be alive workers
    int numWorkers =
        zkHelixAdmin.getResourceIdealState(manager.getClusterName(), tenantName).getNumPartitions();
    List<String> tables = getTablesForCurrentTenant(tenantName);

    for (String tableName : tables) {
      List<TableFileState> stateMap = getCurrentStateMap(tableName);
      for (int i = 0; i < stateMap.size(); i++) {
        if (i % numWorkers == workerId) {
          TableFileState fileState = stateMap.get(i);
          TableFileState.State state = fileState.getState();

          // todo: find this timeout
          long elapsedTime = System.currentTimeMillis() - fileState.getUpdatedAt();
          if (state == TableFileState.State.PENDING
              || (state == TableFileState.State.FAILED && fileState.getRetry() > 0)
              || (state == TableFileState.State.RUNNING
                  && elapsedTime > 5 * 60_000
                  && fileState.getRetry() > 0)) partitionMap.add(stateMap.get(i));
        }
      }
    }

    return partitionMap;
  }

  @SneakyThrows
  private List<String> initStateMap(String tableName, List<ScanFile> files) {
    List<String> map = new ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      putScanFileToS3(tableName, i, files.get(i));
      map.add(
          objectMapper.writeValueAsString(
              TableFileState.builder()
                  .tableName(tableName)
                  .fileId(i)
                  .state(TableFileState.State.PENDING)
                  .updatedAt(System.currentTimeMillis())
                  .build()));
    }
    return map;
  }

  // todo: add metrics
  private boolean isCurrentVersionDone(List<TableFileState> fileStates) {
    boolean allMatch = true;

    for (TableFileState fileState : fileStates) {
      if (!(fileState.getState().equals(TableFileState.State.COMPLETED)
          || (fileState.getState().equals(TableFileState.State.FAILED)
              && fileState.getRetry() == 0))) {
        allMatch = false;
      }
      // completed then delete the file
      else {
        removeScanFileFromS3(fileState.getTableName(), fileState.getFileId());
      }
    }

    return allMatch;
  }

  @SneakyThrows
  private void putScanFileToS3(String tableName, Integer fileId, ScanFile scanFile) {
    JsonObject jsonObject = new JsonObject(objectMapper.writeValueAsString(scanFile));
    // runs on main thread
    S3ClientUtils.putObject(
            s3AsyncClient,
            applicationConfig.getPopulatorScanFileBucket(),
            getScanFilePath(tableName, fileId),
            jsonObject)
        .blockingAwait();
  }

  @SneakyThrows
  private void removeScanFileFromS3(String tableName, Integer fileId) {
    // runs on main thread
    S3ClientUtils.deleteObject(
            s3AsyncClient,
            applicationConfig.getPopulatorScanFileBucket(),
            getScanFilePath(tableName, fileId))
        .blockingAwait();
  }

  @SneakyThrows
  public Single<ScanFile> getScanFileFromS3(String tableName, Integer fileId) {
    return S3ClientUtils.getObject(
            s3AsyncClient,
            applicationConfig.getPopulatorScanFileBucket(),
            getScanFilePath(tableName, fileId))
        .map(r -> objectMapper.readValue(r.toString(), ScanFile.class));
  }

  private String getScanFilePath(String tableName, Integer fileId) {
    return String.format("%s/%s/%s", tableName, fileId, "scan-file.json");
  }

  public Completable rxUpdateFileState(
      String tableName, Integer fileId, TableFileState.State state) {
    long start = System.currentTimeMillis();
    return Completable.create(
        emitter -> {
          AtomicInteger trying = new AtomicInteger(0);
          vertx.setPeriodic(
              2_000,
              timerId -> {
                if (trying.compareAndSet(0, 1)) {
                  if (updateFileState(tableName, fileId, state)) {
                    vertx.cancelTimer(timerId);
                    emitter.onComplete();
                  } else if (System.currentTimeMillis() - start >= 60_000) {
                    vertx.cancelTimer(timerId);
                    emitter.onError(new RuntimeException("timed out waiting to update file state"));
                  } else {
                    trying.set(0);
                  }
                }
              });
        });
  }

  public boolean updateFileState(String tableName, Integer fileId, TableFileState.State state) {
    Stat stat = new Stat();
    ZNRecord record =
        manager
            .getHelixPropertyStore()
            .get(
                String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
                stat,
                AccessOption.PERSISTENT);
    List<TableFileState> stateMap =
        record.getListField("current-state").stream()
            .map(
                r -> {
                  try {
                    return objectMapper.readValue(r, TableFileState.class);
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toList());

    stateMap.get(fileId).setState(state);
    stateMap.get(fileId).setUpdatedAt(System.currentTimeMillis());
    if (state == TableFileState.State.RUNNING) {
      int retry = stateMap.get(fileId).getRetry();
      retry--;
      stateMap.get(fileId).setRetry(retry);
    }

    List<String> updated =
        stateMap.stream()
            .map(
                r -> {
                  try {
                    return objectMapper.writeValueAsString(r);
                  } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(Collectors.toList());
    record.setListField("current-state", updated);
    return manager
        .getHelixPropertyStore()
        .set(
            String.format("/METADATA_ROOT_PATH/%s/STATE_MAP", tableName),
            record,
            stat.getVersion(),
            AccessOption.PERSISTENT);
  }

  @SneakyThrows
  public Flowable<Row> rxProcessBatch(String tableName, String state, String file) {
    Table table = tableFactory.get(tableName).getTable();
    ScanFile work = new ScanFile(state, file);
    Row scanState = work.getScanRow();
    Row scanFile = work.getScanFileRow();

    Row addFile =
        scanFile.getStruct(
            scanFile.getSchema().indexOf(DeltaLogActionUtils.DeltaAction.ADD.colName));
    String path = addFile.getString(addFile.getSchema().indexOf("path"));
    long size = addFile.getLong(addFile.getSchema().indexOf("size"));
    long modificationTime = addFile.getLong(addFile.getSchema().indexOf("modificationTime"));
    String absolutePath =
        new Path(new Path(URI.create(table.getPath(engine))), new Path(URI.create(path)))
            .toString();

    FileStatus fileStatus = FileStatus.of(absolutePath, size, modificationTime);
    StructType physicalReadSchema = ScanStateRow.getPhysicalDataReadSchema(engine, scanState);

    CloseableIterator<ColumnarBatch> physicalDataIter =
        engine
            .getParquetHandler()
            .readParquetFiles(
                singletonCloseableIterator(fileStatus), physicalReadSchema, Optional.empty());

    return getFlowableFromClosableIterator(physicalDataIter)
        .map(ColumnarBatch::getRows)
        .flatMap(TableReader::getFlowableFromClosableIterator);
  }

  private static <T> Flowable<T> getFlowableFromClosableIterator(CloseableIterator<T> closeableIterator){
    return Flowable.generate(
        () -> closeableIterator,
        (iterator, emitter) -> {
          if (iterator.hasNext()) {
            emitter.onNext(iterator.next());
          } else {
            emitter.onComplete();
          }
          return iterator;
        },
        iterator -> {
          try {
            if (iterator != null) {
              ((AutoCloseable) iterator).close();
            }
          } catch (Exception e) {
            log.error("error closing iterator", e);
          }
        });
  }
}
