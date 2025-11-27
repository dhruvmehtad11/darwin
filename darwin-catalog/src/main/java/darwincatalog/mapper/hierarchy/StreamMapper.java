package darwincatalog.mapper.hierarchy;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.entity.AssetEntity;
import org.openapitools.model.AssetType;
import org.openapitools.model.StreamDetail;
import org.springframework.stereotype.Component;

@Component
public class StreamMapper extends AssetHierarchyMapper {

  @Override
  AssetType getAssetType() {
    return AssetType.STREAM;
  }

  @Override
  public StreamDetail getDetails(AssetEntity entity) {
    String name = entity.getFqdn();
    String[] components = name.split(HIERARCHY_SEPARATOR);
    StreamDetail streamDetail = new StreamDetail();
    if (components.length <= 2) {
      return streamDetail;
    }
    int levels = components.length - 2; // ignore org:type
    int index = components.length - 1;

    streamDetail.setOrg(components[0]);
    streamDetail.setStreamName(components[index--]);
    if (levels >= 2) {
      streamDetail.setTenant(components[index]);
    }
    return streamDetail;
  }
}
