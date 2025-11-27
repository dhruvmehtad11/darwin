package darwincatalog.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.entity.ConfigEntity;
import darwincatalog.exception.AssetException;
import darwincatalog.repository.ConfigRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ConfigProvider {
  private final Map<String, Object> configMap = new HashMap<>();

  private final List<String> glueDatabaseWhitelist;

  public ConfigProvider(ConfigRepository configRepository) {
    List<ConfigEntity> allConfigs = configRepository.findAll();
    for (ConfigEntity config : allConfigs) {
      configMap.put(config.getKey(), getParsedValue(config));
    }

    glueDatabaseWhitelist = populateConfigList("glue_database_whitelist");
  }

  @SuppressWarnings("unchecked")
  public List<String> populateConfigList(String key) {
    Object value = configMap.get(key);
    if (value == null) {
      throw new AssetException("Config not found for key: " + key);
    }
    if (value instanceof List) {
      return (List<String>) value;
    }

    throw new AssetException("Expected a list for key" + key + " but got " + value.getClass());
  }

  @SuppressWarnings("unchecked")
  public Object getParsedValue(ConfigEntity config) {
    String value = config.getValue();
    String valueType = config.getValueType();
    switch (valueType) {
      case "STRING":
        return value;
      case "INTEGER":
        return Integer.parseInt(value);
      case "DOUBLE":
        return Double.parseDouble(value);
      case "BOOLEAN":
        return Boolean.parseBoolean(value);
      case "LIST":
      case "JSON":
        try {
          ObjectMapper mapper = new ObjectMapper();
          return mapper.readValue(value, List.class);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Failed to parse JSON config value", e);
        }
      default:
        throw new IllegalArgumentException("Unsupported value type: " + valueType);
    }
  }
}
