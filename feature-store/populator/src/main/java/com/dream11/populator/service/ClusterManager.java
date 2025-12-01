package com.dream11.populator.service;

import com.dream11.common.app.AppContext;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.HelixConfig;
import com.dream11.populator.MainApplication;
import com.dream11.populator.statemodel.WorkerStateModelFactory;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.core.Promise;
import io.vertx.reactivex.core.Vertx;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.helix.HelixConstants;
import org.apache.helix.HelixDataAccessor;
import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.PropertyKey;
import org.apache.helix.api.config.RebalanceConfig;
import org.apache.helix.api.listeners.LiveInstanceChangeListener;
import org.apache.helix.constants.InstanceConstants;
import org.apache.helix.controller.rebalancer.waged.WagedRebalancer;
import org.apache.helix.examples.OnlineOfflineStateModelFactory;
import org.apache.helix.lock.LockScope;
import org.apache.helix.lock.helix.ZKDistributedNonblockingLock;
import org.apache.helix.manager.zk.ZKHelixAdmin;
import org.apache.helix.model.ClusterConfig;
import org.apache.helix.model.ExternalView;
import org.apache.helix.model.IdealState;
import org.apache.helix.model.InstanceConfig;
import org.apache.helix.model.LiveInstance;
import org.apache.helix.model.OnlineOfflineSMD;
import org.apache.helix.model.ResourceConfig;
import org.apache.helix.participant.StateMachineEngine;

@Slf4j
public class ClusterManager implements Shareable {
  private static final String NUM_CONSUMER_CAPACITY_KEY = "NUM_CONSUMER";
  private static final String VERTX_NODE_PREFIX = "VERTX_NODE__";
  private final ApplicationConfig applicationConfig =
      AppContext.getInstance(ApplicationConfig.class);
  @Getter private HelixManager manager;
  private HelixConfig config;
  @Getter private ZKHelixAdmin zkHelixAdmin;
  private Vertx vertx;
  private InstanceType instanceType;
  private ZKDistributedNonblockingLock addNodeLock;
  private final List<Consumer<List<String>>> liveInstanceChangeHandlers = new ArrayList<>();

  public static ClusterManager create(Vertx vertx, HelixConfig config, InstanceType instanceType) {
    return new ClusterManager(vertx, config, instanceType);
  }

  @SneakyThrows
  public ClusterManager(Vertx vertx, HelixConfig config, InstanceType instanceType) {
    this.instanceType = instanceType;
    this.vertx = vertx;
    this.config = config;
    this.manager =
        HelixManagerFactory.getZKHelixManager(
            config.getClusterName(), config.getInstanceName(), instanceType, config.getZkServer());
    this.zkHelixAdmin = new ZKHelixAdmin(config.getZkServer());

    this.addNodeLock =
        new ZKDistributedNonblockingLock(
            new AddOrPurgeNodeLockScope(config.getClusterName()),
            config.getZkServer(),
            AddOrPurgeNodeLockScope.DEFAULT_LOCK_TIMEOUT,
            AddOrPurgeNodeLockScope.DEFAULT_LOCK_MESSAGE,
            config.getInstanceName());
  }

  public Boolean healthy() {
    return manager.isConnected();
  }

  public Boolean isLeader() {
    return manager.isLeader();
  }

  // filtering out controller instances
  public Set<String> getLiveInstances() {
    Set<String> instancesWithCurrTag =
        new HashSet<>(
            zkHelixAdmin.getInstancesInClusterWithTag(
                config.getClusterName(), participantTagProvider()));

    HelixDataAccessor accessor = manager.getHelixDataAccessor();
    PropertyKey.Builder keyBuilder = accessor.keyBuilder();
    Set<String> liveNodes = new HashSet<>(accessor.getChildNames(keyBuilder.liveInstances()));
    liveNodes.retainAll(instancesWithCurrTag);
    return liveNodes;
  }

  public Set<String> getPhysicalNodeIds() {
    Set<String> liveNodes = getLiveInstances();
    Set<String> nodes = new HashSet<>();

    for (String liveNode : liveNodes) {
      List<String> tags =
          zkHelixAdmin.getInstanceConfig(config.getClusterName(), liveNode).getTags();
      for (String tag : tags) {
        String nodeId = extractVertxNodeFromTag(tag);
        if (!nodeId.isEmpty()) nodes.add(nodeId);
      }
    }
    return nodes;
  }

  public Single<Set<String>> rxGetLiveInstances() {
    return vertx
        .<Set<String>>rxExecuteBlocking(
            emitter -> {
              try {
                emitter.complete(getLiveInstances());
              } catch (Exception e) {
                log.error("error getting live nodes: ", e);
                emitter.fail(e);
              }
            },
            false)
        .toSingle();
  }

  public Completable rxPurgeAndRebalance() {
    return vertx
        .rxExecuteBlocking(
            emitter -> {
              try {
                acquireLockAndExecute(vertx, addNodeLock, emitter, this::purgeAndRebalance);
                log.info(
                    String.format(
                        "cluster %s instances purged and re-balanced", config.getClusterName()));
              } catch (Exception e) {
                log.error("error while purging and re-balancing: ", e);
                emitter.fail(e);
              }
            })
        .ignoreElement();
  }

  // purges and re-balances assignments
  public void purgeAndRebalance() {
    // always acquire addNode lock as purge function is not thread safe
    zkHelixAdmin.purgeOfflineInstances(config.getClusterName(), 10_000);
    rebalanceAllResources();
  }

  private void rebalanceAllResources() {
    for (String resourceName : zkHelixAdmin.getResourcesInCluster(config.getClusterName()))
      zkHelixAdmin.rebalance(config.getClusterName(), resourceName, 1);
  }

  public Single<List<String>> rxGetAllResources() {
    return vertx
        .<List<String>>rxExecuteBlocking(
            emitter -> {
              try {
                emitter.complete(this.getAllResources());
              } catch (Exception e) {
                emitter.fail(e);
              }
            },
            false)
        .toSingle();
  }

  private List<String> getAllResources() {
    return new ArrayList<>(zkHelixAdmin.getResourcesInCluster(config.getClusterName()));
  }

  public Single<Boolean> rxIsLeader() {
    return vertx
        .<Boolean>rxExecuteBlocking(
            emitter -> {
              try {
                emitter.complete(this.isLeader());
              } catch (Exception e) {
                emitter.fail(e);
              }
            },
            false)
        .toSingle();
  }

  public Single<Boolean> rxHealthy() {
    return vertx
        .<Boolean>rxExecuteBlocking(
            emitter -> {
              try {
                emitter.complete(this.healthy());
              } catch (Exception e) {
                emitter.fail(e);
              }
            },
            false)
        .toSingle();
  }

  public Completable rxStart() {
    return vertx
        .rxExecuteBlocking(
            emitter -> {
              try {
                acquireLockAndExecute(vertx, addNodeLock, emitter, this::addNode);
              } catch (Exception e) {
                emitter.fail(e);
              }
            },
            false)
        .ignoreElement()
        .andThen(rxStartManager())
        .doOnComplete(
            () -> {
              log.info("registering live instance change listener");
              for (Consumer<List<String>> function : liveInstanceChangeHandlers) {
                this.manager.addLiveInstanceChangeListener(
                    (LiveInstanceChangeListener)
                        (list, notificationContext) ->
                            function.accept(
                                list.stream()
                                    .map(LiveInstance::getInstanceName)
                                    .collect(Collectors.toList())));
              }
            });
  }

  private Completable rxStartManager() {
    return vertx
        .rxExecuteBlocking(
            emitter -> {
              try {
                startManager();
                emitter.complete();
              } catch (Exception e) {
                emitter.fail(e);
              }
            },
            false)
        .ignoreElement();
  }

  public Completable rxStop() {
    return Completable.create(
        emitter -> {
          try {
            manager.disconnect();
            emitter.onComplete();
          } catch (Exception e) {
            log.info("error starting helix cluster manager: " + e.getMessage());
            emitter.onError(e);
          }
        });
  }

  public Completable rxAddResource(String resourceName, Integer numPartitions) {
    return vertx
        .rxExecuteBlocking(
            emitter -> {
              try {
                addResource(resourceName, numPartitions);
                emitter.complete();
              } catch (Exception e) {
                log.error(
                    String.format(
                        "error adding resource %s to cluster %s: ",
                        resourceName, config.getClusterName()),
                    e);
                emitter.fail(e);
              }
            },
            false)
        .ignoreElement();
  }

  public Completable rxDropResource(String resourceName) {
    return vertx
        .rxExecuteBlocking(
            emitter -> {
              try {
                dropResource(resourceName);
                emitter.complete();
              } catch (Exception e) {
                log.error(
                    String.format(
                        "error dropping resource %s to cluster %s: ",
                        resourceName, config.getClusterName()),
                    e);
                emitter.fail(e);
              }
            },
            false)
        .ignoreElement();
  }

  @SneakyThrows
  private static void acquireLockAndExecute(
      Vertx vertx, ZKDistributedNonblockingLock lock, Promise<Object> promise, Action action) {
    long startTime = System.currentTimeMillis();
    vertx.setPeriodic(
        2_000,
        timerId -> {
          if (System.currentTimeMillis() - startTime > 2 * 60_000) {
            promise.fail(
                new RuntimeException(
                    String.format(
                        "timeout waiting for lock: %s", lock.getCurrentLockInfo().getMessage())));
          }

          if (lock.tryLock()) {
            try {
              action.run();
              promise.complete();
            } catch (Exception e) {
              promise.fail(e);
            } finally {
              releaseLock(lock);
              vertx.cancelTimer(timerId);
            }
          }
        });
  }

  private static void releaseLock(ZKDistributedNonblockingLock lock) {
    if (lock.isCurrentOwner()) lock.unlock();
  }

  private String participantTagProvider() {
    return config.getClusterName() + "__" + InstanceType.PARTICIPANT.toString();
  }

  private static String vertxNodeTagProvider(String nodeId) {
    return VERTX_NODE_PREFIX + nodeId;
  }

  private static String extractVertxNodeFromTag(String tag) {
    if (!tag.contains(VERTX_NODE_PREFIX)) return "";
    return tag.substring(VERTX_NODE_PREFIX.length());
  }

  @SneakyThrows
  private void addNode() {
    InstanceConfig instanceConfig = new InstanceConfig(config.getInstanceName());
    if (Objects.equals(System.getProperty("app.environment"), "test")
        || Objects.equals(System.getProperty("app.environment"), "local")
        || Objects.equals(System.getProperty("app.environment"), "darwin-local")) {
      instanceConfig.setHostName("localhost");
    } else {
      instanceConfig.setHostName(InetAddress.getLocalHost().getHostAddress());
    }
    instanceConfig.setPort(String.valueOf(new ServerSocket(0).getLocalPort()));
    instanceConfig.setInstanceOperation(InstanceConstants.InstanceOperation.ENABLE);
    instanceConfig.setInstanceCapacityMap(Map.of(NUM_CONSUMER_CAPACITY_KEY, 1));
    instanceConfig.addTag(vertxNodeTagProvider(MainApplication.NODE_ID));
    instanceConfig.addTag(participantTagProvider());

    try {
      zkHelixAdmin.addInstance(config.getClusterName(), instanceConfig);
      log.info(
          "added node "
              + config.getInstanceName()
              + " to helix cluster: "
              + config.getClusterName());
    } catch (Exception e) {
      log.error("error adding node " + config.getInstanceName() + " to helix cluster: ", e);
      throw e;
    }
  }

  @SneakyThrows
  public void startManager() {
    try {
      preConnect();
      manager.connect();
      postConnect();
      log.info("started helix cluster manager on node: " + config.getInstanceName());
    } catch (Exception e) {
      log.error("error starting helix cluster manager: ", e);
      throw e;
    }
  }

  private void preConnect() {
    if (manager.getInstanceType().equals(InstanceType.PARTICIPANT)) {
      StateMachineEngine stateMach = manager.getStateMachineEngine();
      stateMach.registerStateModelFactory(
          OnlineOfflineSMD.name,
          new WorkerStateModelFactory(
              Vertx.currentContext(),
              config.getInstanceName(),
              applicationConfig.getPopulatorWorkerParallelism()));
    }
  }

  private void postConnect() {
    if (manager.getInstanceType().equals(InstanceType.PARTICIPANT)) {

    } else if (manager.getInstanceType().equals(InstanceType.CONTROLLER)) {
      //      HelixDataAccessor accessor = manager.getHelixDataAccessor();
      //      ClusterConfig clusterConfig =
      // accessor.getProperty(accessor.keyBuilder().clusterConfig());
      //      clusterConfig.setOfflineDurationForPurge(10_000);
      //      accessor.setProperty(accessor.keyBuilder().clusterConfig(), clusterConfig);
    }
    HelixDataAccessor accessor = manager.getHelixDataAccessor();
    ClusterConfig clusterConfig = accessor.getProperty(accessor.keyBuilder().clusterConfig());
    clusterConfig.setOfflineDurationForPurge(10_000);
    clusterConfig.setInstanceCapacityKeys(List.of(NUM_CONSUMER_CAPACITY_KEY));
    accessor.setProperty(accessor.keyBuilder().clusterConfig(), clusterConfig);
  }

  private void addResource(String resourceName, Integer numPartitions) {
    if (zkHelixAdmin.getResourcesInCluster(config.getClusterName()).contains(resourceName)) {
      IdealState idealState =
          zkHelixAdmin.getResourceIdealState(config.getClusterName(), resourceName);
      int currPartitions = idealState.getNumPartitions();

      if (currPartitions < numPartitions) {
        updateResourcePartitions(resourceName, numPartitions);
      } else if (currPartitions > numPartitions) {
        // partitions need to be deleted manually
        dropResource(resourceName);
        addResourceAndRebalance(resourceName, numPartitions);
      }
    } else {
      addResourceAndRebalance(resourceName, numPartitions);
    }
  }

  private void dropResource(String resourceName) {
    zkHelixAdmin.dropResource(config.getClusterName(), resourceName);
  }

  private void updateResourcePartitions(String resourceName, Integer numPartitions) {
    IdealState idealState =
        zkHelixAdmin.getResourceIdealState(config.getClusterName(), resourceName);
    updateResourcePartitions(resourceName, numPartitions, idealState);
  }

  private void updateResourcePartitions(
      String resourceName, Integer numPartitions, IdealState idealState) {
    idealState.setNumPartitions(numPartitions);
    zkHelixAdmin.setResourceIdealState(config.getClusterName(), resourceName, idealState);
    zkHelixAdmin.rebalance(config.getClusterName(), resourceName, 1);
  }

  private static final String STATE_MODEL_NAME = OnlineOfflineSMD.name;
  private static final String MODE = RebalanceConfig.RebalanceMode.FULL_AUTO.name();
  private static final String STRATEGY =
      "org.apache.helix.controller.rebalancer.strategy.CrushEdRebalanceStrategy";

  private void addResourceAndRebalance(String resourceName, Integer numPartitions) {
    //    zkHelixAdmin.addResource(
    //        config.getClusterName(), resourceName, numPartitions, STATE_MODEL_NAME, MODE,
    // STRATEGY);
    addWeightedResource(resourceName, numPartitions);
    zkHelixAdmin.rebalance(config.getClusterName(), resourceName, 1);
  }

  private void addWeightedResource(String resourceName, Integer numPartitions) {
    IdealState idealState = new IdealState(resourceName);
    idealState.setNumPartitions(numPartitions);
    idealState.setStateModelDefRef(STATE_MODEL_NAME);
    IdealState.RebalanceMode mode =
        idealState.rebalanceModeFromString(MODE, IdealState.RebalanceMode.SEMI_AUTO);
    idealState.setRebalanceMode(mode);
    idealState.setRebalanceStrategy(STRATEGY);
    idealState.setReplicas("1");
    idealState.setStateModelFactoryName(HelixConstants.DEFAULT_STATE_MODEL_FACTORY);
    idealState.setRebalancerClassName(WagedRebalancer.class.getName());

    ResourceConfig.Builder resourceConfigBuilder = new ResourceConfig.Builder(resourceName);

    resourceConfigBuilder.setPartitionCapacity(Map.of(NUM_CONSUMER_CAPACITY_KEY, 1));
    zkHelixAdmin.addResourceWithWeight(
        config.getClusterName(), idealState, resourceConfigBuilder.build());
  }

  public Single<Map<String, Map<String, Integer>>> rxGetAssignmentMap() {
    return vertx
        .<Map<String, Map<String, Integer>>>rxExecuteBlocking(
            emitter -> {
              try {
                emitter.complete(getAssignmentMap());
              } catch (Exception e) {
                emitter.fail(e);
              }
            },
            false)
        .toSingle();
  }

  private Map<String, Map<String, Integer>> getAssignmentMap() {
    List<String> resources = zkHelixAdmin.getResourcesInCluster(config.getClusterName());
    Map<String, Map<String, Integer>> assignments = new HashMap<>();

    for (String resource : resources) {
      ExternalView externalView =
          zkHelixAdmin.getResourceExternalView(config.getClusterName(), resource);
      if (externalView != null && externalView.getPartitionSet() != null) {
        for (String partition : externalView.getPartitionSet()) {
          Map<String, String> instanceStateMap = externalView.getStateMap(partition);
          for (Map.Entry<String, String> entry : instanceStateMap.entrySet()) {
            if (!assignments.containsKey(entry.getKey())) {
              assignments.put(entry.getKey(), new HashMap<>());
            }
            if (!assignments.get(entry.getKey()).containsKey(resource)) {
              assignments.get(entry.getKey()).put(resource, 0);
            }
            assignments
                .get(entry.getKey())
                .put(resource, assignments.get(entry.getKey()).get(resource) + 1);
          }
        }
      }
    }
    return assignments;
  }

  public void addLiveInstanceChangeHandlers(Consumer<List<String>> function) {
    this.liveInstanceChangeHandlers.add(function);
  }

  private static class AddOrPurgeNodeLockScope implements LockScope {
    // auto release after 30 sec
    public static final Long DEFAULT_LOCK_TIMEOUT = 30_000L;
    public static final String DEFAULT_LOCK_MESSAGE = "add-node-lock";
    private static final String BASE_LOCK_PATH = "/add-node-lock/LOCK/";
    private final String LOCK_PATH;

    public AddOrPurgeNodeLockScope(String clusterName) {
      this.LOCK_PATH = BASE_LOCK_PATH + clusterName;
    }

    @Override
    public String getPath() {
      return LOCK_PATH;
    }
  }
}
