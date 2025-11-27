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
@Table(name = "field_lineage")
public class FieldLineageEntity extends BaseEntity {

  @Column(name = "asset_lineage_id")
  private Long assetLineageId;

  @Column(name = "from_field_name")
  private String fromFieldName;

  @Column(name = "to_field_name")
  private String toFieldName;
}
