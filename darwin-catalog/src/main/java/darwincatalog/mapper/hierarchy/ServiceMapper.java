package darwincatalog.mapper.hierarchy;

import darwincatalog.entity.AssetEntity;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.AssetType;
import org.openapitools.model.ServiceDetail;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper extends AssetHierarchyMapper {

  @Override
  AssetType getAssetType() {
    return AssetType.SERVICE;
  }

  @Override
  public AssetDetail getDetails(AssetEntity entity) {
    return new ServiceDetail();
  }
}
