package darwincatalog.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "contractcentral.consumer.token")
@Getter
@Setter
public class TokenNames {
  private Map<String, Set<String>> names = new HashMap<>();
}
