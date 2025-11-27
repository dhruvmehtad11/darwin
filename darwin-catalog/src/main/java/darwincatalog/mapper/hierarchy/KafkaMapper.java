package darwincatalog.mapper.hierarchy;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.entity.AssetEntity;
import org.openapitools.model.AssetType;
import org.openapitools.model.KafkaDetail;
import org.springframework.stereotype.Component;

@Component
public class KafkaMapper extends AssetHierarchyMapper {

  @Override
  AssetType getAssetType() {
    return AssetType.KAFKA;
  }

  @Override
  public KafkaDetail getDetails(AssetEntity entity) {
    String name = entity.getFqdn();
    String[] components = name.split(HIERARCHY_SEPARATOR);
    KafkaDetail kafkaDetail = new KafkaDetail();
    if (components.length <= 2) {
      return kafkaDetail;
    }
    int levels = components.length - 2; // ignore org:type
    int index = components.length - 1;

    kafkaDetail.setOrg(components[0]);
    kafkaDetail.setTopicName(components[index--]);
    if (levels >= 2) {
      kafkaDetail.setClusterName(components[index]);
    }
    return kafkaDetail;
  }
}
