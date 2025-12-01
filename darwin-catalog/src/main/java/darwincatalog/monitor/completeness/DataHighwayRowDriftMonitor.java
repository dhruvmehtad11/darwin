package darwincatalog.monitor.completeness;

import static darwincatalog.util.Common.getFqdn;
import static darwincatalog.util.Constants.DH_ROW_COUNT_DRIFT;
import static darwincatalog.util.Constants.DISTINCT_ROW_COUNT_PER_DATE;
import static darwincatalog.util.Constants.ROW_COUNT_PER_DATE;

import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.model.ConstructMessagePayload;
import darwincatalog.model.ConstructQueryPayload;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.service.ValidatorService;
import darwincatalog.util.DatadogHelper;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.AssetType;
import org.openapitools.model.TableDetail;
import org.springframework.stereotype.Component;

@Component
public class DataHighwayRowDriftMonitor extends CompletenessMonitor {
  private final AssetHierarchyResolver assetHierarchyResolver;
  private final ValidatorService validatorService;
  private final DatadogHelper datadogHelper;

  public DataHighwayRowDriftMonitor(
      Properties properties,
      DatadogHelper datadogHelper,
      AssetHierarchyResolver assetHierarchyResolver,
      ValidatorService validatorService) {
    super(properties, datadogHelper);
    this.assetHierarchyResolver = assetHierarchyResolver;
    this.validatorService = validatorService;
    this.datadogHelper = datadogHelper;
  }

  @Override
  public String getMetricName() {
    return DH_ROW_COUNT_DRIFT;
  }

  @Override
  public boolean isAssetType(AssetEntity assetEntity) {
    boolean result = false;
    AssetDetail assetDetail = assetHierarchyResolver.getDetails(assetEntity);
    if (assetDetail instanceof TableDetail) {
      TableDetail tableDetail = (TableDetail) assetDetail;
      result =
          "datahighway".equals(assetEntity.getSourcePlatform())
              && tableDetail.getDatabaseName() != null
              && tableDetail.getDatabaseName().endsWith("_processed");
    }
    return result;
  }

  @Override
  String constructQuery(ConstructQueryPayload constructQueryPayload) {
    AssetEntity assetEntity = constructQueryPayload.getAssetEntity();
    TableDetail tableDetail = (TableDetail) assetHierarchyResolver.getDetails(assetEntity);
    String processedTableMetricTags =
        String.format(
            "{org:%s, host:%s, asset_type:%s, table_type:%s, asset_name:%s}",
            tableDetail.getOrg(),
            getProperties().getApplicationName(),
            assetEntity.getType().toString(),
            tableDetail.getType(),
            assetEntity.getFqdn());

    assert tableDetail.getDatabaseName() != null;
    String rawDatabaseName = tableDetail.getDatabaseName().replace("_processed", "");
    String rawAssetName;
    if (StringUtils.isEmpty(tableDetail.getCatalogName())) {
      rawAssetName =
          getFqdn(
              tableDetail.getOrg(),
              AssetType.TABLE.getValue(),
              tableDetail.getType(),
              rawDatabaseName,
              tableDetail.getTableName());
    } else {
      rawAssetName =
          getFqdn(
              tableDetail.getOrg(),
              AssetType.TABLE.getValue(),
              tableDetail.getType(),
              tableDetail.getCatalogName(),
              rawDatabaseName,
              tableDetail.getTableName());
    }
    validatorService.verifyAssetExists(rawAssetName);

    String rawTableMetricTags =
        String.format(
            "{org:%s, host:%s, asset_type:%s, table_type:%s, asset_name:%s}",
            tableDetail.getOrg(),
            getProperties().getApplicationName(),
            assetEntity.getType().toString(),
            tableDetail.getType(),
            rawAssetName);
    return String.format(
        "max(last_30m):avg:%s%s - avg:%s%s != 0",
        ROW_COUNT_PER_DATE,
        processedTableMetricTags,
        DISTINCT_ROW_COUNT_PER_DATE,
        rawTableMetricTags);
  }

  @Override
  String constructMessage(ConstructMessagePayload constructMessagePayload) {
    return datadogHelper.getCorrectnessMessage(constructMessagePayload);
  }
}
