package darwincatalog.service.work;

import darwincatalog.entity.AssetDirectoryEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.jdbc.Work;

public class DeleteDirectoryWork implements Work {
  private final List<AssetDirectoryEntity> assetDirectoryEntities;

  public DeleteDirectoryWork(List<AssetDirectoryEntity> assetDirectoryEntities) {
    this.assetDirectoryEntities = assetDirectoryEntities;
  }

  @Override
  public void execute(Connection connection) throws SQLException {
    String updateSql =
        "update asset_directory set count = count - 1 where asset_name = ? and asset_prefix = ?";
    String deleteSql =
        "delete from asset_directory where asset_name = ? and asset_prefix = ? and count <= 0";
    try (PreparedStatement updatePs = connection.prepareStatement(updateSql);
        PreparedStatement deletePs = connection.prepareStatement(deleteSql)) {
      for (AssetDirectoryEntity entry : assetDirectoryEntities) {
        updatePs.setString(1, entry.getAssetName());
        updatePs.setString(2, entry.getAssetPrefix());
        updatePs.addBatch();
        deletePs.setString(1, entry.getAssetName());
        deletePs.setString(2, entry.getAssetPrefix());
        deletePs.addBatch();
      }
      updatePs.executeBatch();
      deletePs.executeBatch();
    }
  }
}
