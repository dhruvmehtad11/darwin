package darwincatalog.model;

import darwincatalog.entity.AssetEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstructMessagePayload {
  private AssetEntity assetEntity;
  private List<String> downstreamConsumerNames;
  private List<String> slackChannels;
}
