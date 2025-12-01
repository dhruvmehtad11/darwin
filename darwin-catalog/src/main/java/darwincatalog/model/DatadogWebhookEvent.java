package darwincatalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatadogWebhookEvent {
  @JsonProperty("tags")
  private String tags;

  @JsonProperty("invoked_by")
  private String invokedBy;

  @JsonProperty("alert_title")
  private String alertTitle;

  @JsonProperty("alert_transition_state")
  private String alertTransitionState;

  @JsonProperty("created_at")
  private Long createdAt;

  @JsonProperty("monitor_url")
  private String monitorUrl;

  @JsonProperty("INCIDENT_UUID")
  private String incidentUuid;

  @JsonProperty("monitor_id")
  private Long monitorId;
}
