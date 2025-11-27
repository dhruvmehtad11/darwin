package darwincatalog.resolver;

import darwincatalog.exception.NoAssetRegistrarFoundException;
import darwincatalog.service.asset.registration.AssetRegistrar;
import java.util.List;
import java.util.Map;
import org.openapitools.model.AssetType;
import org.springframework.stereotype.Component;

@Component
public class AssetRegistrarResolver {
  private List<AssetRegistrar> registrars;

  public AssetRegistrarResolver(List<AssetRegistrar> registrars) {
    this.registrars = registrars;
  }

  public String formFqdn(AssetType type, Map<String, String> detail) {
    return registrars.stream()
        .filter(e -> e.isType(type))
        .findFirst()
        .map(e -> e.formFqdn(detail))
        .orElseThrow(
            () -> new NoAssetRegistrarFoundException("No asset registrar found for type: " + type));
  }
}
