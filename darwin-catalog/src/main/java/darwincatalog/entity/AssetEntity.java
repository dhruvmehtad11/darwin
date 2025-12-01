package darwincatalog.entity;

import darwincatalog.util.JsonMapConverter;
import java.time.Instant;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.model.AssetType;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asset")
public class AssetEntity extends BaseEntity {

  @Column(name = "fqdn")
  private String fqdn;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private AssetType type;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "source_platform")
  private String sourcePlatform;

  @Size(max = 255)
  @Column(name = "business_roster")
  private String businessRoster;

  @Size(max = 255)
  @Column(name = "current_incident")
  private String currentIncident;

  @Column(name = "asset_created_at")
  private Instant assetCreatedAt;

  @Column(name = "asset_updated_at")
  private Instant assetUpdatedAt;

  @Column(name = "quality_score")
  private String qualityScore;

  @Column(name = "metadata", columnDefinition = "json")
  @Convert(converter = JsonMapConverter.class)
  private Map<String, Object> metadata;
}
