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
@Table(name = "asset_directory")
public class AssetDirectoryEntity extends BaseEntity {

  @Size(max = 255)
  @Column(name = "asset_name")
  private String assetName;

  @Size(max = 255)
  @Column(name = "asset_prefix")
  private String assetPrefix;

  @Column(name = "depth")
  private Integer depth;

  @Column(name = "is_terminal")
  private Boolean isTerminal;

  @Column(name = "count")
  private Boolean count;
}
