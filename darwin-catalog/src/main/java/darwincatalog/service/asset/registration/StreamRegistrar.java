package darwincatalog.service.asset.registration;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.exception.InvalidAttributeException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AssetType;
import org.springframework.stereotype.Component;

@Component
public class StreamRegistrar implements AssetRegistrar {
  @Override
  public boolean isType(AssetType type) {
    return type == AssetType.STREAM;
  }

  @Override
  public void validateDetails(Map<String, String> detail) {
    String org = detail.get("org");
    String streamName = detail.get("stream_name");

    if (StringUtils.isBlank(org)) {
      throw new InvalidAttributeException("Stream detail must include 'org'");
    }
    if (StringUtils.isBlank(streamName)) {
      throw new InvalidAttributeException("Stream detail must include 'stream_name'");
    }
  }

  @Override
  public String generateFqdn(Map<String, String> detail) {
    StringBuilder fqdn = new StringBuilder();
    fqdn.append(detail.get("org").toLowerCase()).append(HIERARCHY_SEPARATOR);
    fqdn.append("stream").append(HIERARCHY_SEPARATOR);

    String tenant = detail.get("tenant");
    if (StringUtils.isNotBlank(tenant)) {
      fqdn.append(tenant.toLowerCase()).append(HIERARCHY_SEPARATOR);
    }

    fqdn.append(detail.get("stream_name").toLowerCase());
    return fqdn.toString();
  }
}
