package darwincatalog.util;

import static darwincatalog.util.Constants.END_CONSUMER;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.MonitorsApi;
import com.datadog.api.client.v1.model.Monitor;
import com.datadog.api.client.v1.model.MonitorUpdateRequest;
import com.datadog.api.client.v2.api.MetricsApi;
import com.datadog.api.client.v2.model.IntakePayloadAccepted;
import com.datadog.api.client.v2.model.MetricPayload;
import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.exception.DatadogException;
import darwincatalog.exception.MonitorNotFoundException;
import darwincatalog.model.ConstructMessagePayload;
import darwincatalog.repository.AssetLineageRepository;
import darwincatalog.repository.AssetRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class DatadogHelper {
  private final MonitorsApi monitorsApi;
  private final Properties properties;
  private final AssetLineageRepository assetLineageRepository;
  private final AssetRepository assetRepository;
  private final MetricsApi metricsApi;

  public DatadogHelper(
      MonitorsApi monitorsApi,
      Properties properties,
      AssetLineageRepository assetLineageRepository,
      AssetRepository assetRepository,
      MetricsApi metricsApi) {
    this.monitorsApi = monitorsApi;
    this.properties = properties;
    this.assetLineageRepository = assetLineageRepository;
    this.assetRepository = assetRepository;
    this.metricsApi = metricsApi;
  }

  public Optional<Monitor> getMonitorOptional(Long monitorId) {
    Optional<Monitor> result = Optional.empty();
    try {
      Monitor monitor = monitorsApi.getMonitor(monitorId);
      result = Optional.of(monitor);
    } catch (ApiException ex) {
      log.warn("Monitor with id {} does not exists on datadog", monitorId);
    }
    return result;
  }

  public Monitor getMonitor(Long monitorId) {
    return getMonitorOptional(monitorId).orElseThrow(() -> new MonitorNotFoundException(monitorId));
  }

  public void updateMonitor(Long monitorId, MonitorUpdateRequest monitorUpdateRequest) {
    try {
      monitorsApi.updateMonitor(monitorId, monitorUpdateRequest);
    } catch (ApiException e) {
      throw new DatadogException(e);
    }
  }

  public long createMonitor(Monitor monitor) {
    try {
      Monitor newMonitor = monitorsApi.createMonitor(monitor);
      return newMonitor.getId();
    } catch (ApiException e) {
      throw new DatadogException(e);
    }
  }

  public void deleteMonitor(Long monitorId) {
    try {
      monitorsApi.deleteMonitor(monitorId);
    } catch (ApiException e) {
      throw new DatadogException(e);
    }
  }

  public String appendSlackWebhooks(String message, List<String> datadogSlackWebhooks) {
    StringBuilder sb = new StringBuilder(message);
    datadogSlackWebhooks.forEach(
        slackChannel -> {
          sb.append("\n");
          sb.append("@slack-");
          sb.append(slackChannel);
        });
    return sb.toString();
  }

  public String appendCustomWebhooks(String message) {
    StringBuilder sb = new StringBuilder(message);
    properties
        .getDatadogCustomWebhooks()
        .forEach(
            slackChannel -> {
              sb.append("\n");
              sb.append("@webhook-");
              sb.append(slackChannel);
            });
    return sb.toString();
  }

  public List<String> endConsumers(AssetEntity assetEntity) {
    List<Long> downstreamAssetIds =
        assetLineageRepository.findByFromAssetEntity(assetEntity.getId());

    return assetRepository.findAllById(downstreamAssetIds).stream()
        .filter(e -> END_CONSUMER.contains(e.getType()))
        .map(AssetEntity::getFqdn)
        .collect(java.util.stream.Collectors.toList());
  }

  public List<String> getTags(Monitor monitor) {
    List<String> tags = monitor.getTags();
    tags = tags == null ? new ArrayList<>() : tags;
    Collections.sort(tags);
    return tags;
  }

  public String getCorrectnessMessage(ConstructMessagePayload constructMessagePayload) {
    String messageTemplate =
        "{{#is_alert}} :rotating_light: *Completeness SLA breach alert* :rotating_light:\n\n"
            + "*APM Breach*: Table Completeness (row count mismatch: {{value}} rows)\n\n"
            + "*P0 Asset Impacted*: %s\n\n"
            + ":warning: *Downstream Asset/s Impacted*: %s\n\n"
            + "{{/is_alert}}\n\n"
            + "{{#is_recovery}} :white_check_mark: Completeness is recovered{{/is_recovery}}\n\n"
            + "Platform: `%s`";
    List<String> downstreamConsumerNames = constructMessagePayload.getDownstreamConsumerNames();
    downstreamConsumerNames =
        downstreamConsumerNames.isEmpty() ? List.of("Unknown") : downstreamConsumerNames;
    String message =
        String.format(
            messageTemplate,
            constructMessagePayload.getAssetEntity().getFqdn(),
            downstreamConsumerNames.stream()
                .map(StringUtils::capitalize)
                .collect(java.util.stream.Collectors.toList()),
            constructMessagePayload.getAssetEntity().getSourcePlatform());
    message = appendSlackWebhooks(message, constructMessagePayload.getSlackChannels());
    message = appendCustomWebhooks(message);
    return message;
  }

  public IntakePayloadAccepted submitMetrics(MetricPayload body) {
    try {
      log.info("datadog request body: {}", body.toString());
      IntakePayloadAccepted response = metricsApi.submitMetrics(body);
      log.info("datadog response: {}", response);
      return response;
    } catch (ApiException e) {
      log.error("Failed to submit metric request: ", e);
      throw new DatadogException(e);
    }
  }
}
