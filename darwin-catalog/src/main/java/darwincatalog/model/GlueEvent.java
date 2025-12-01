package darwincatalog.model;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GlueEvent {
  private String version;
  private String id;
  private String source;
  private String account;
  private String time;
  private String region;
  private List<String> resources;

  @NotNull private GlueDetail detail;
}
