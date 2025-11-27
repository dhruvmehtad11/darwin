package darwincatalog.entity;

import javax.persistence.Column;
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
@Table(name = "asset_lineage")
public class AssetLineageEntity extends BaseEntity {

  @Column(name = "from_asset_id")
  private Long fromAssetId;

  @Column(name = "to_asset_id")
  private Long toAssetId;
}
