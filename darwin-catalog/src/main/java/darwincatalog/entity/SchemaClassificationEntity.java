package darwincatalog.entity;

import darwincatalog.util.JsonMapConverter;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schema_classification")
public class SchemaClassificationEntity extends BaseEntity {

  @Column(name = "asset_id")
  private Long assetId;

  @Column(name = "schema_id", nullable = false)
  private Long schemaId;

  @Column(name = "classified_schema_json", columnDefinition = "json")
  @Convert(converter = JsonMapConverter.class)
  private Map<String, Object> classifiedSchemaJson;
}
