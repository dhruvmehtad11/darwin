package darwincatalog.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
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
@Table(name = "asset_tag_relation")
public class AssetTagRelationEntity extends BaseEntity {

  @Column(name = "asset_id")
  private Long assetId;

  @Size(max = 255)
  @Column(name = "tag")
  private String tag;
}
