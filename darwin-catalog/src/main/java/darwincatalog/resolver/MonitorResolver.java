package darwincatalog.resolver;

import darwincatalog.config.FeatureFlag;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.monitor.Monitor;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MonitorResolver {
  private final FeatureFlag featureFlag;
  List<Monitor> monitors;

  public MonitorResolver(List<Monitor> monitors, FeatureFlag featureFlag) {
    this.monitors = monitors;
    this.featureFlag = featureFlag;
  }

  public List<Monitor> get(AssetEntity assetEntity, RuleEntity ruleEntity) {
    List<Monitor> result = new ArrayList<>();
    if (!featureFlag.isDatadogMonitorsEnabled()) {
      log.warn(
          "Datadog monitors flow is disabled. Skipping monitor resolution for asset {} and rule {}",
          assetEntity.getId(),
          ruleEntity.getId());
      return result;
    }
    result =
        monitors.stream()
            .filter(e -> e.isMetricType(ruleEntity))
            .filter(e -> e.isAssetType(assetEntity))
            .collect(java.util.stream.Collectors.toList());

    if (CollectionUtils.isEmpty(result)) {
      log.warn(
          "No monitor configuration found for the asset {} rule {}. Skipping monitor creation",
          assetEntity.getId(),
          ruleEntity.getId());
    }
    return result;
  }
}
