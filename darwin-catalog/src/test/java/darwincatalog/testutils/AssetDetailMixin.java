package darwincatalog.testutils;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openapitools.model.DashboardDetail;
import org.openapitools.model.ServiceDetail;
import org.openapitools.model.StreamDetail;
import org.openapitools.model.TableDetail;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "detail")
@JsonSubTypes({
  @JsonSubTypes.Type(value = StreamDetail.class, name = "stream"),
  @JsonSubTypes.Type(value = TableDetail.class, name = "table"),
  @JsonSubTypes.Type(value = DashboardDetail.class, name = "dashboard"),
  @JsonSubTypes.Type(value = ServiceDetail.class, name = "service")
})
public class AssetDetailMixin {}
