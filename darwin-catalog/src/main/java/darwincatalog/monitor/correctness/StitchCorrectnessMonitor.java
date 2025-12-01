package darwincatalog.monitor.correctness;

import static darwincatalog.util.Constants.STITCH_CORRECTNESS_METRIC;

import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.model.ConstructMessagePayload;
import darwincatalog.model.ConstructQueryPayload;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.util.DatadogHelper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.TableDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class StitchCorrectnessMonitor extends CorrectnessMonitor {
  private final AssetHierarchyResolver assetHierarchyResolver;
  private final DatadogHelper datadogHelper;
  private final Properties properties;

  public StitchCorrectnessMonitor(
      AssetHierarchyResolver assetHierarchyResolver,
      Properties properties,
      DatadogHelper datadogHelper) {
    super(properties, datadogHelper);
    this.assetHierarchyResolver = assetHierarchyResolver;
    this.datadogHelper = datadogHelper;
    this.properties = properties;
  }

  @Override
  public boolean isAssetType(AssetEntity assetEntity) {
    return "datastitch".equals(assetEntity.getSourcePlatform())
        && assetHierarchyResolver.isTableType(assetEntity);
  }

  @Override
  public String getMetricName() {
    return STITCH_CORRECTNESS_METRIC;
  }

  @Override
  String constructQuery(ConstructQueryPayload constructQueryPayload) {
    AssetEntity assetEntity = constructQueryPayload.getAssetEntity();
    double threshold = constructQueryPayload.getThreshold();
    TableDetail tableDetail = (TableDetail) assetHierarchyResolver.getDetails(assetEntity);
    return String.format(
        "min(last_15m):max:%s{org:%s, host:%s, asset_type:%s, table_type:%s, asset_name:%s} != %s",
        getMetricName(),
        tableDetail.getOrg(),
        getProperties().getApplicationName(),
        assetEntity.getType().toString(),
        tableDetail.getType(),
        assetEntity.getFqdn(),
        threshold);
  }

  @Override
  String constructMessage(ConstructMessagePayload constructMessagePayload) {
    String messageTemplate =
        "{{#is_alert}} :rotating_light: *Correctness SLA breach alert* :rotating_light:\n\n"
            + "*APM Breach*: Table Correctness\n\n"
            + "*P0 Asset Impacted*: %s\n\n"
            + ":warning: *Downstream Asset/s Impacted*: %s\n\n"
            + "{{/is_alert}}\n\n"
            + "{{#is_recovery}} :white_check_mark: Correctness is recovered{{/is_recovery}}\n\n"
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
    message = datadogHelper.appendSlackWebhooks(message, properties.getDatadogSlackWebhooks());
    message = datadogHelper.appendCustomWebhooks(message);
    return message;
  }
}
