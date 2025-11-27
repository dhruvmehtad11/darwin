package darwincatalog.monitor.correctness;

import com.datadog.api.client.v1.model.MonitorOptions;
import com.datadog.api.client.v1.model.MonitorOptionsNotificationPresets;
import com.datadog.api.client.v1.model.MonitorThresholds;
import com.datadog.api.client.v1.model.MonitorType;
import com.datadog.api.client.v1.model.MonitorUpdateRequest;
import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.model.ConstructMessagePayload;
import darwincatalog.model.ConstructQueryPayload;
import darwincatalog.model.ConstructTagsPayload;
import darwincatalog.monitor.Monitor;
import darwincatalog.util.DatadogHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.MetricType;

@Slf4j
public abstract class CorrectnessMonitor implements Monitor {
  @Getter private final Properties properties;
  private final DatadogHelper datadogHelper;

  protected CorrectnessMonitor(Properties properties, DatadogHelper datadogHelper) {
    this.properties = properties;
    this.datadogHelper = datadogHelper;
  }

  abstract String constructQuery(ConstructQueryPayload constructQueryPayload);

  abstract String constructMessage(ConstructMessagePayload constructMessagePayload);

  @Override
  public MetricType getMetricType() {
    return MetricType.CORRECTNESS;
  }

  @Override
  public boolean isMetricType(RuleEntity ruleEntity) {
    return ruleEntity.getType() == getMetricType()
        && getMetricName().equals(ruleEntity.getLeftExpression());
  }

  @Override
  public Optional<Long> create(AssetEntity assetEntity, RuleEntity ruleEntity) {
    Optional<Long> result = Optional.empty();
    double threshold = Double.parseDouble(ruleEntity.getRightExpression());

    if (ruleEntity.getMonitorId() != null) {
      update(assetEntity, ruleEntity);
    } else {
      long newMonitorId = createMonitor(assetEntity, ruleEntity, threshold);
      result = Optional.of(newMonitorId);
    }

    return result;
  }

  @Override
  public void update(AssetEntity assetEntity, RuleEntity ruleEntity) {
    Long monitorId = ruleEntity.getMonitorId();
    com.datadog.api.client.v1.model.Monitor monitor = datadogHelper.getMonitor(monitorId);

    List<String> downstreamConsumerNames = datadogHelper.endConsumers(assetEntity);

    List<String> newTags =
        getMonitorTags(
            ConstructTagsPayload.builder()
                .assetEntity(assetEntity)
                .ruleEntity(ruleEntity)
                .downstreamConsumerNames(downstreamConsumerNames)
                .build());
    List<String> slackChannels = new ArrayList<>();
    slackChannels.add(ruleEntity.getSlackChannel());
    slackChannels.addAll(properties.getDatadogCustomWebhooks());
    double threshold = Double.parseDouble(ruleEntity.getRightExpression());
    String message =
        constructMessage(
            ConstructMessagePayload.builder()
                .downstreamConsumerNames(downstreamConsumerNames)
                .assetEntity(assetEntity)
                .slackChannels(slackChannels)
                .build());

    String newQuery =
        constructQuery(
            ConstructQueryPayload.builder().assetEntity(assetEntity).threshold(threshold).build());
    MonitorUpdateRequest monitorUpdateRequest = new MonitorUpdateRequest();
    List<String> oldTags = datadogHelper.getTags(monitor);
    boolean monitorChanged = false;

    if (!newTags.equals(oldTags)) {
      monitorChanged = true;
      monitorUpdateRequest.tags(newTags);
    }
    if (!message.equals(monitor.getMessage())) {
      monitorChanged = true;
      monitorUpdateRequest.message(message);
    }
    if (!newQuery.equals(monitor.getQuery())) {
      monitorChanged = true;
      monitorUpdateRequest
          .query(newQuery)
          .options(
              new MonitorOptions()
                  .thresholds(new MonitorThresholds().critical(threshold))
                  .notificationPresetName(MonitorOptionsNotificationPresets.HIDE_ALL)
                  .includeTags(false));
    }

    if (monitorChanged) {
      datadogHelper.updateMonitor(monitorId, monitorUpdateRequest);
      log.info("Monitor {} updated with new {}", monitorId, monitorUpdateRequest);
    } else {
      log.info(
          "No changes detected for rule {} monitor {}. Skipping update",
          ruleEntity.getId(),
          monitorId);
    }
  }

  protected long createMonitor(AssetEntity assetEntity, RuleEntity ruleEntity, double threshold) {
    String name = String.format("Correctness of %s", assetEntity.getFqdn());
    List<String> downstreamConsumerNames = datadogHelper.endConsumers(assetEntity);
    List<String> tags =
        getMonitorTags(
            ConstructTagsPayload.builder()
                .assetEntity(assetEntity)
                .ruleEntity(ruleEntity)
                .downstreamConsumerNames(downstreamConsumerNames)
                .build());

    long priority = 2L;
    List<String> slackChannels = new ArrayList<>();
    slackChannels.add(ruleEntity.getSlackChannel());
    slackChannels.addAll(properties.getDatadogCustomWebhooks());
    String message =
        constructMessage(
            ConstructMessagePayload.builder()
                .downstreamConsumerNames(downstreamConsumerNames)
                .assetEntity(assetEntity)
                .slackChannels(slackChannels)
                .build());

    String query =
        constructQuery(
            ConstructQueryPayload.builder().assetEntity(assetEntity).threshold(threshold).build());
    com.datadog.api.client.v1.model.Monitor body =
        new com.datadog.api.client.v1.model.Monitor()
            .name(name)
            .type(MonitorType.QUERY_ALERT)
            .query(query)
            .message(message)
            .tags(tags)
            .priority(priority)
            .options(
                new MonitorOptions()
                    .thresholds(new MonitorThresholds().critical(threshold))
                    .notificationPresetName(MonitorOptionsNotificationPresets.HIDE_ALL)
                    .includeTags(false));

    long newMonitorId = datadogHelper.createMonitor(body);
    log.info(
        "New completeness monitor {} created for {} with tags: {}",
        newMonitorId,
        assetEntity.getFqdn(),
        tags);
    return newMonitorId;
  }
}
