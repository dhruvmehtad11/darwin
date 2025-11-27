package darwincatalog.service.work;

import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.entity.AssetDirectoryEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.hibernate.jdbc.Work;

public class AddDirectoryWork implements Work {
  private List<AssetDirectoryEntity> assetDirectoryEntities;

  public AddDirectoryWork(List<AssetDirectoryEntity> assetDirectoryEntities) {
    this.assetDirectoryEntities = assetDirectoryEntities;
  }

  @Override
  public void execute(Connection connection) throws SQLException {
    String sql =
        "insert into asset_directory(asset_name, asset_prefix, depth, is_terminal, sort_path) values (?,?,?,?,?) on duplicate key update count = count + 1";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      for (AssetDirectoryEntity entry : assetDirectoryEntities) {
        String sortPath = entry.getAssetPrefix() + HIERARCHY_SEPARATOR + entry.getAssetName();
        ps.setString(1, entry.getAssetName());
        ps.setString(2, entry.getAssetPrefix());
        ps.setInt(3, entry.getDepth());
        ps.setBoolean(4, entry.getIsTerminal());
        ps.setString(5, sortPath);
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }
}
