package darwincatalog.model;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstructTagsPayload {
  private AssetEntity assetEntity;
  private RuleEntity ruleEntity;
  private List<String> downstreamConsumerNames;
}
