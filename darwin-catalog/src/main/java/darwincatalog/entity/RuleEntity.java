package darwincatalog.entity;

import javax.persistence.Column;
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
import org.openapitools.model.Comparator;
import org.openapitools.model.MetricType;
import org.openapitools.model.Severity;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule")
public class RuleEntity extends BaseEntity {

  @Column(name = "asset_id")
  private Long assetId;

  @Column(name = "monitor_id", nullable = false)
  private Long monitorId;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private MetricType type;

  @Size(max = 255)
  @Column(name = "schedule")
  private String schedule;

  @Size(max = 255)
  @Column(name = "left_expression")
  private String leftExpression;

  @Enumerated(EnumType.STRING)
  @Column(name = "comparator")
  private Comparator comparator;

  @Size(max = 255)
  @Column(name = "right_expression")
  private String rightExpression;

  @Column(name = "health_status")
  private Boolean healthStatus;

  @Column(name = "slack_channel")
  private String slackChannel;

  @Column(name = "severity")
  @Enumerated(EnumType.STRING)
  private Severity severity;
}
