package darwincatalog.monitor.completeness;

import static darwincatalog.util.Constants.ROW_COUNT_DRIFT;

import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.model.ConstructMessagePayload;
import darwincatalog.model.ConstructQueryPayload;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.util.DatadogHelper;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.TableDetail;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabeamRowDriftMonitor extends CompletenessMonitor {
  private final AssetHierarchyResolver assetHierarchyResolver;
  private final DatadogHelper datadogHelper;

  public DatabeamRowDriftMonitor(
      AssetHierarchyResolver assetHierarchyResolver,
      DatadogHelper datadogHelper,
      Properties properties) {
    super(properties, datadogHelper);
    this.assetHierarchyResolver = assetHierarchyResolver;
    this.datadogHelper = datadogHelper;
  }

  @Override
  public boolean isAssetType(AssetEntity assetEntity) {
    return "databeam".equals(assetEntity.getSourcePlatform())
        && assetHierarchyResolver.isTableType(assetEntity);
  }

  public String getMetricName() {
    return ROW_COUNT_DRIFT;
  }

  @Override
  String constructMessage(ConstructMessagePayload constructMessagePayload) {
    return datadogHelper.getCorrectnessMessage(constructMessagePayload);
  }

  @Override
  String constructQuery(ConstructQueryPayload constructQueryPayload) {
    AssetEntity assetEntity = constructQueryPayload.getAssetEntity();
    double rowCountMismatchThreshold = constructQueryPayload.getThreshold();
    TableDetail tableDetail = (TableDetail) assetHierarchyResolver.getDetails(assetEntity);
    return String.format(
        "min(last_15m):max:%s{org:%s, host:%s, asset_type:%s, table_type:%s, asset_name:%s} > %s",
        getMetricName(),
        tableDetail.getOrg(),
        getProperties().getApplicationName(),
        assetEntity.getType().toString(),
        tableDetail.getType(),
        assetEntity.getFqdn(),
        rowCountMismatchThreshold);
  }
}
