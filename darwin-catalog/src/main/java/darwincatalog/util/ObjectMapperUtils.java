package darwincatalog.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.openapitools.model.SchemaStructure;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapperUtils {
  private final ObjectMapper objectMapper;

  public ObjectMapperUtils(@Qualifier("customMapper") ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Map<String, Object> convertSchemaToMap(SchemaStructure schemaObject) {
    return strictMapper().convertValue(schemaObject, new TypeReference<>() {});
  }

  public SchemaStructure convertMapToSchema(Map<String, Object> schemaObject) {
    return objectMapper.convertValue(schemaObject, SchemaStructure.class);
  }

  public static ObjectMapper strictMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    return mapper;
  }
}
