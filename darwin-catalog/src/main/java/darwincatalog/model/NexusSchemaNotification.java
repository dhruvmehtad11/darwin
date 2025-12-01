package darwincatalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NexusSchemaNotification {
  @JsonProperty(value = "asset_fqdn")
  private String assetFqdn;

  @JsonProperty(value = "asset_schema")
  private SchemaUpdateNotification schemaUpdateNotification;
}
