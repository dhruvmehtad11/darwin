package darwincatalog.monitor.freshness;

import static darwincatalog.util.Constants.FRESHNESS_METRIC_NAME;

import com.datadog.api.client.v1.model.MonitorOptions;
import com.datadog.api.client.v1.model.MonitorOptionsNotificationPresets;
import com.datadog.api.client.v1.model.MonitorThresholds;
import com.datadog.api.client.v1.model.MonitorType;
import com.datadog.api.client.v1.model.MonitorUpdateRequest;
import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.model.ConstructTagsPayload;
import darwincatalog.monitor.Monitor;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.util.DatadogHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.MetricType;
import org.openapitools.model.TableDetail;
import org.springframework.util.StringUtils;

@Slf4j
public abstract class FreshnessMonitor implements Monitor {
  @Getter private final Properties properties;

  private final AssetHierarchyResolver assetHierarchyResolver;
  private final DatadogHelper datadogHelper;

  protected static final double FRESHNESS_PLACEHOLDER_THRESHOLD = -1;
  protected static final String MESSAGE_TEMPLATE =
      "{{#is_no_data}} :rotating_light: *Freshness SLA breach alert* :rotating_light:\n\n"
          + "*APM Breach*: %s Freshness\n\n"
          + "*P0 Asset Impacted*: %s\n\n"
          + ":warning: *Downstream Asset/s Impacted*: %s\n\n"
          + "{{/is_no_data}}\n\n"
          + "{{#is_recovery}} :white_check_mark: Freshness is recovered{{/is_recovery}}\n\n"
          + "Platform: `%s`";

  FreshnessMonitor(
      Properties properties,
      AssetHierarchyResolver assetHierarchyResolver,
      DatadogHelper datadogHelper) {
    this.properties = properties;
    this.assetHierarchyResolver = assetHierarchyResolver;
    this.datadogHelper = datadogHelper;
  }

  @Override
  public MetricType getMetricType() {
    return MetricType.FRESHNESS;
  }

  @Override
  public String getMetricName() {
    return FRESHNESS_METRIC_NAME;
  }

  @Override
  public void update(AssetEntity assetEntity, RuleEntity ruleEntity) {
    Long monitorId = ruleEntity.getMonitorId();
    com.datadog.api.client.v1.model.Monitor monitor = datadogHelper.getMonitor(monitorId);

    List<String> downstreamConsumerNames = datadogHelper.endConsumers(assetEntity);

    double threshold = Double.parseDouble(ruleEntity.getRightExpression()) / (1000 * 60);
    List<String> newTags =
        getMonitorTags(
            ConstructTagsPayload.builder()
                .assetEntity(assetEntity)
                .ruleEntity(ruleEntity)
                .downstreamConsumerNames(downstreamConsumerNames)
                .build());
    String message =
        constructMessage(assetEntity, ruleEntity.getSlackChannel(), downstreamConsumerNames);

    String newQuery = constructQuery(assetEntity, threshold);
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
      log.info("Existing monitor {} has the same query. Skipping update", monitorId);
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

  @Override
  public Optional<Long> create(AssetEntity assetEntity, RuleEntity ruleEntity) {
    Optional<Long> result = Optional.empty();
    double slaInMinutes = Double.parseDouble(ruleEntity.getRightExpression()) / (1000 * 60);

    if (ruleEntity.getMonitorId() != null) {
      update(assetEntity, ruleEntity);
    } else {
      long newMonitorId = createMonitor(assetEntity, ruleEntity, slaInMinutes);
      result = Optional.of(newMonitorId);
    }

    return result;
  }

  protected long createMonitor(
      AssetEntity assetEntity, RuleEntity ruleEntity, double slaInMinutes) {
    String name = String.format("Freshness of %s", assetEntity.getFqdn());
    List<String> downstreamConsumerNames = datadogHelper.endConsumers(assetEntity);
    List<String> tags =
        getMonitorTags(
            ConstructTagsPayload.builder()
                .assetEntity(assetEntity)
                .ruleEntity(ruleEntity)
                .downstreamConsumerNames(downstreamConsumerNames)
                .build());
    long priority = 2L;
    String message =
        constructMessage(assetEntity, ruleEntity.getSlackChannel(), downstreamConsumerNames);

    String query = constructQuery(assetEntity, slaInMinutes);
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
                    .thresholds(new MonitorThresholds().critical(FRESHNESS_PLACEHOLDER_THRESHOLD))
                    .notifyNoData(true)
                    .noDataTimeframe((long) slaInMinutes)
                    .notificationPresetName(MonitorOptionsNotificationPresets.HIDE_ALL)
                    .includeTags(false));

    long newMonitorId = datadogHelper.createMonitor(body);
    log.info(
        "New freshness monitor {} created for {} with tags: {}",
        newMonitorId,
        assetEntity.getFqdn(),
        tags);
    return newMonitorId;
  }

  protected String constructQuery(AssetEntity assetEntity, double slaInMinutes) {
    int slaInteger = (int) Math.max(1, slaInMinutes);
    TableDetail tableDetail = (TableDetail) assetHierarchyResolver.getDetails(assetEntity);
    return String.format(
        "min(last_%sm):max:%s{org:%s, host:%s, asset_type:%s, table_type:%s, asset_name:%s} < %s",
        slaInteger,
        getMetricName(),
        tableDetail.getOrg(),
        getProperties().getApplicationName(),
        assetEntity.getType().toString(),
        tableDetail.getType(),
        assetEntity.getFqdn(),
        FRESHNESS_PLACEHOLDER_THRESHOLD);
  }

  String constructMessage(
      AssetEntity assetEntity, String slackChannel, List<String> downstreamConsumerNames) {
    downstreamConsumerNames =
        downstreamConsumerNames.isEmpty() ? List.of("Unknown") : downstreamConsumerNames;
    String message =
        String.format(
            MESSAGE_TEMPLATE,
            StringUtils.capitalize(assetEntity.getType().getValue()),
            assetEntity.getFqdn(),
            downstreamConsumerNames.stream()
                .map(StringUtils::capitalize)
                .collect(java.util.stream.Collectors.toList()),
            assetEntity.getSourcePlatform());
    List<String> slackChannels = new ArrayList<>();
    slackChannels.add(slackChannel);
    slackChannels.addAll(properties.getDatadogCustomWebhooks());
    message = datadogHelper.appendSlackWebhooks(message, slackChannels);
    message = datadogHelper.appendCustomWebhooks(message);
    return message;
  }
}
