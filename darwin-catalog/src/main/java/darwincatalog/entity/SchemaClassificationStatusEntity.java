package darwincatalog.entity;

import java.time.Instant;
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
import org.openapitools.model.ClassificationNotificationType;
import org.openapitools.model.SchemaClassificationCategory;
import org.openapitools.model.SchemaClassificationMethod;
import org.openapitools.model.SchemaClassificationStatus;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schema_classification_status")
public class SchemaClassificationStatusEntity extends BaseEntity {

  @Column(name = "schema_classification_id")
  private Long schemaClassificationId;

  @Column(name = "classification_type")
  @Enumerated(EnumType.STRING)
  private SchemaClassificationCategory schemaClassificationCategory;

  @Column(name = "classification_status")
  @Enumerated(EnumType.STRING)
  private SchemaClassificationStatus classificationStatus;

  @Column(name = "classification_method")
  @Enumerated(EnumType.STRING)
  private SchemaClassificationMethod classificationMethod;

  @Column(name = "notification_status")
  @Enumerated(EnumType.STRING)
  private ClassificationNotificationType notificationStatus;

  @Column(name = "classified_by")
  private String classifiedBy;

  @Column(name = "classified_at")
  private Instant classifiedAt;

  @Column(name = "classification_notes", columnDefinition = "text")
  private String classificationNotes;

  @Size(max = 255)
  @Column(name = "reviewed_by")
  private String reviewedBy;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "review_notes", columnDefinition = "text")
  private String reviewNotes;
}
