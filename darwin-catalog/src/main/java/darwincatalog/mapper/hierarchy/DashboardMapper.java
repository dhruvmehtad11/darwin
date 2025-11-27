package darwincatalog.mapper.hierarchy;

import darwincatalog.entity.AssetEntity;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.AssetType;
import org.openapitools.model.DashboardDetail;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper extends AssetHierarchyMapper {

  @Override
  AssetType getAssetType() {
    return AssetType.DASHBOARD;
  }

  @Override
  public AssetDetail getDetails(AssetEntity entity) {
    return new DashboardDetail();
  }
}
