package darwincatalog.resolver;

import darwincatalog.entity.AssetEntity;
import darwincatalog.exception.NoHierarchyMapperException;
import darwincatalog.mapper.hierarchy.AssetHierarchyMapper;
import java.util.List;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.TableDetail;
import org.springframework.stereotype.Component;

@Component
public class AssetHierarchyResolver {

  private final List<AssetHierarchyMapper> mappers;

  public AssetHierarchyResolver(List<AssetHierarchyMapper> mappers) {
    this.mappers = mappers;
  }

  public AssetDetail getDetails(AssetEntity entity) {
    return mappers.stream()
        .filter(e -> e.isType(entity.getType()))
        .findFirst()
        .orElseThrow(
            () ->
                new NoHierarchyMapperException(
                    "No hierarchy mapper found for asset "
                        + entity.getFqdn()
                        + " type "
                        + entity.getType()))
        .getDetails(entity);
  }

  public boolean isTableType(AssetEntity entity) {
    AssetDetail assetDetail = getDetails(entity);
    return assetDetail instanceof TableDetail;
  }
}
