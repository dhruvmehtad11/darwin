package darwincatalog.testutils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.DashboardDetail;
import org.openapitools.model.KafkaDetail;
import org.openapitools.model.ServiceDetail;
import org.openapitools.model.StreamDetail;
import org.openapitools.model.TableDetail;
import org.springframework.lang.Nullable;

public abstract class AssetMixin {

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = StreamDetail.class, name = "stream"),
    @JsonSubTypes.Type(value = KafkaDetail.class, name = "kafka"),
    @JsonSubTypes.Type(value = TableDetail.class, name = "table"),
    @JsonSubTypes.Type(value = DashboardDetail.class, name = "dashboard"),
    @JsonSubTypes.Type(value = ServiceDetail.class, name = "service")
  })
  @JsonProperty("detail")
  @Nullable
  public abstract AssetDetail getDetail();
}
