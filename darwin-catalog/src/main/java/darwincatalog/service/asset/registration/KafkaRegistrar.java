package darwincatalog.service.asset.registration;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.exception.InvalidAttributeException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AssetType;
import org.springframework.stereotype.Component;

@Component
public class KafkaRegistrar implements AssetRegistrar {
  @Override
  public boolean isType(AssetType type) {
    return type == AssetType.KAFKA;
  }

  @Override
  public void validateDetails(Map<String, String> detail) {
    String org = detail.get("org");
    String topicName = detail.get("topic_name");

    if (StringUtils.isBlank(org)) {
      throw new InvalidAttributeException("Kafka detail must include 'org'");
    }
    if (StringUtils.isBlank(topicName)) {
      throw new InvalidAttributeException("Kafka detail must include 'topic_name'");
    }
  }

  @Override
  public String generateFqdn(Map<String, String> detail) {
    StringBuilder fqdn = new StringBuilder();
    fqdn.append(detail.get("org")).append(HIERARCHY_SEPARATOR);
    fqdn.append("kafka").append(HIERARCHY_SEPARATOR);

    String clusterName = detail.get("cluster_name");
    if (StringUtils.isNotBlank(clusterName)) {
      fqdn.append(clusterName).append(HIERARCHY_SEPARATOR);
    }

    fqdn.append(detail.get("topic_name"));
    return fqdn.toString();
  }
}
