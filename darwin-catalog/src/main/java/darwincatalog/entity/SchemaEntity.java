package darwincatalog.entity;

import static darwincatalog.util.Common.generateCanonicalHash;

import com.fasterxml.jackson.core.JsonProcessingException;
import darwincatalog.util.JsonMapConverter;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asset_schema")
@Slf4j
public class SchemaEntity extends BaseEntity {

  @Column(name = "asset_id")
  private Long assetId;

  @Column(name = "version_id")
  private Integer versionId;

  @Column(name = "schema_json", columnDefinition = "json")
  @Convert(converter = JsonMapConverter.class)
  private Map<String, Object> schemaJson;

  @Column(name = "schema_hash", nullable = false)
  private String schemaHash;

  @Column(name = "previous_schema_id")
  private Long previousSchemaId;

  @PrePersist
  @PreUpdate
  public void computeSchemaHash() throws NoSuchAlgorithmException, JsonProcessingException {
    if (MapUtils.isNotEmpty(schemaJson)) {
      // let the persistence fail if error occurred during hash computation
      this.schemaHash = generateCanonicalHash(schemaJson);
    }
  }
}
