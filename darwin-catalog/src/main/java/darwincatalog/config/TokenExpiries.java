package darwincatalog.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "contractcentral.consumer.token")
@Getter
@Setter
public class TokenExpiries {
  private Map<String, Long> expiries = new HashMap<>();
}
