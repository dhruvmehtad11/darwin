package darwincatalog.monitor;

import static darwincatalog.util.Common.severityToDatadogMetricType;

import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.model.ConstructTagsPayload;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.openapitools.model.MetricType;

public interface Monitor {
  default boolean isMetricType(RuleEntity ruleEntity) {
    return ruleEntity.getType() == getMetricType();
  }

  boolean isAssetType(AssetEntity assetEntity);

  MetricType getMetricType();

  String getMetricName();

  Optional<Long> create(AssetEntity assetEntity, RuleEntity ruleEntity);

  void update(AssetEntity assetEntity, RuleEntity ruleEntity);

  Properties getProperties();

  default List<String> getMonitorTags(ConstructTagsPayload constructTagsPayload) {
    AssetEntity assetEntity = constructTagsPayload.getAssetEntity();
    List<String> downstreamConsumerNames = constructTagsPayload.getDownstreamConsumerNames();
    RuleEntity ruleEntity = constructTagsPayload.getRuleEntity();
    List<String> tags = new ArrayList<>();

    tags.add("asset_name:" + assetEntity.getFqdn());
    tags.add("asset_type:" + assetEntity.getType().getValue().toLowerCase());
    tags.add("service_name:" + getProperties().getApplicationName());
    tags.add("component_name:" + getProperties().getApplicationName());
    tags.add("component_type:data-asset");
    tags.add("env:" + getProperties().getDatadogMonitorEnv());
    tags.add("environment_name:" + getProperties().getDatadogMonitorEnvironment());
    tags.add("metric_category:" + getMetricType());
    tags.add("roster:" + assetEntity.getBusinessRoster());
    if (ruleEntity.getSeverity() == null) {
      tags.add("metric-type:" + getProperties().getDatadogDefaultMonitorType());
    } else {
      tags.add("metric-type:" + severityToDatadogMetricType(ruleEntity.getSeverity()));
    }
    if (assetEntity.getSourcePlatform() != null) {
      tags.add("service:" + assetEntity.getSourcePlatform());
    } else {
      tags.add("service:" + getProperties().getApplicationName());
    }
    for (String downstreamConsumer : downstreamConsumerNames) {
      tags.add("downstream:" + downstreamConsumer);
    }

    Collections.sort(tags);
    return tags;
  }
}
