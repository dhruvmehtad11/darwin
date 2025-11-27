package darwincatalog.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "config")
public class ConfigEntity {

  @Id
  @Column(name = "config_key", nullable = false)
  private String key;

  @Column(name = "config_value", columnDefinition = "TEXT")
  private String value;

  @Column(name = "value_type")
  private String valueType;
}
