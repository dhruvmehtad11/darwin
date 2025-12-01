package darwincatalog.mapper.hierarchy;

import darwincatalog.entity.AssetEntity;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.AssetType;

@Slf4j
public abstract class AssetHierarchyMapper {

  abstract AssetType getAssetType();

  public abstract AssetDetail getDetails(AssetEntity entity);

  public boolean isType(AssetType assetType) {
    return assetType == getAssetType();
  }
}
