package darwincatalog.service.asset.registration;

import darwincatalog.entity.AssetEntity;
import darwincatalog.resolver.AssetRegistrarResolver;
import java.time.Instant;
import org.openapitools.model.RegisterAssetRequest;
import org.springframework.stereotype.Service;

@Service
public class AssetRegistrationService {

  private final AssetRegistrarResolver assetRegistrarResolver;

  public AssetRegistrationService(AssetRegistrarResolver assetRegistrarResolver) {
    this.assetRegistrarResolver = assetRegistrarResolver;
  }

  public AssetEntity createAssetEntity(RegisterAssetRequest request) {
    String fqdn = assetRegistrarResolver.formFqdn(request.getType(), request.getDetail());

    AssetEntity.AssetEntityBuilder builder =
        AssetEntity.builder()
            .fqdn(fqdn)
            .type(request.getType())
            .description(request.getDescription())
            .businessRoster(request.getBusinessRoster())
            .assetCreatedAt(Instant.now())
            .assetUpdatedAt(Instant.now());

    if (request.getMetadata() != null) {
      builder.metadata(request.getMetadata());
    }

    return builder.build();
  }
}
