package darwincatalog.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class FeatureFlag {
  @Value("${contractcentral.datadog.monitor.enabled}")
  private boolean datadogMonitorsEnabled;

  @Value("${contractcentral.asset.verification.source.enabled}")
  private boolean assetVerificationSourceEnabled;
}
