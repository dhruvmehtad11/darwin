package darwincatalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.model.CommonInternalClassificationStatusReadInner;
import org.openapitools.model.SchemaStructure;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchemaUpdateNotification {

  @JsonProperty(value = "version")
  private Integer version;

  @JsonProperty(value = "schema_json")
  private SchemaStructure schemaJson;

  @JsonProperty(value = "classification_status")
  private List<CommonInternalClassificationStatusReadInner> schemaClassificationStatuses;
}
