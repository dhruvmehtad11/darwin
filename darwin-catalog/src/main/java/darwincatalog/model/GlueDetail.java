package darwincatalog.model;

import java.util.List;
import lombok.Data;

@Data
public class GlueDetail {
  private String databaseName;
  private String typeOfChange;
  private String tableName;
  private List<String> changedTables;
  private List<String> changedPartitions;
}
