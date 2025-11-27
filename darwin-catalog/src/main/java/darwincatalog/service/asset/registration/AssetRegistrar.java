package darwincatalog.service.asset.registration;

import java.util.Map;
import org.openapitools.model.AssetType;

public interface AssetRegistrar {
  boolean isType(AssetType type);

  void validateDetails(Map<String, String> detail);

  String generateFqdn(Map<String, String> detail);

  default String formFqdn(Map<String, String> detail) {
    validateDetails(detail);
    return generateFqdn(detail);
  }
}
