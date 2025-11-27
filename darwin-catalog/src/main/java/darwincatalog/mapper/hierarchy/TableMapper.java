package darwincatalog.mapper.hierarchy;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.entity.AssetEntity;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.AssetType;
import org.openapitools.model.TableDetail;
import org.springframework.stereotype.Component;

@Component
public class TableMapper extends AssetHierarchyMapper {

  @Override
  AssetType getAssetType() {
    return AssetType.TABLE;
  }

  @Override
  public AssetDetail getDetails(AssetEntity entity) {
    String name = entity.getFqdn();
    String[] components = name.split(HIERARCHY_SEPARATOR);
    TableDetail tableDetail = new TableDetail();
    if (components.length <= 3) {
      return tableDetail;
    }
    int levels = components.length - 3; // ignore org:type:subtype
    int index = components.length - 1;

    tableDetail.setOrg(components[0]);
    tableDetail.setType(components[2]);
    tableDetail.setTableName(components[index--]);
    if (levels >= 2) {
      tableDetail.setDatabaseName(components[index--]);
    }
    if (levels >= 3) {
      tableDetail.setCatalogName(components[index]);
    }
    return tableDetail;
  }
}
