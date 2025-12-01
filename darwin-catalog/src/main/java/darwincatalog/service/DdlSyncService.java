package darwincatalog.service;

import java.util.List;

public interface DdlSyncService {
  void addTables(String databaseName, List<String> newTables);

  void updateTable(String databaseName, String affectedTable);

  void deleteTables(String databaseName, List<String> tablesToDelete);
}
