package darwincatalog.model;

import darwincatalog.entity.AssetEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstructQueryPayload {
  private AssetEntity assetEntity;
  private double threshold;
}
