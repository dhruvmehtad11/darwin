package darwincatalog.monitor.freshness;

import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.util.DatadogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// we can have the table freshness monitors PER platform (i.e, DatabeamTableFreshnessMonitor,
// etc.), but as all of them are using the same datadog query, we can have a single implementation
@Component
@Slf4j
public class TableFreshnessMonitor extends FreshnessMonitor {
  private final AssetHierarchyResolver assetHierarchyResolver;

  public TableFreshnessMonitor(
      AssetHierarchyResolver assetHierarchyResolver,
      DatadogHelper datadogHelper,
      Properties properties) {
    super(properties, assetHierarchyResolver, datadogHelper);
    this.assetHierarchyResolver = assetHierarchyResolver;
  }

  @Override
  public boolean isAssetType(AssetEntity assetEntity) {
    return assetHierarchyResolver.isTableType(assetEntity);
  }
}
