package darwincatalog.service.notifier;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import java.util.List;

public interface SchemaUpdateNotifier {
  boolean isType(AssetEntity assetEntity);

  /**
   * @param assetEntity - asset for which schema evolution notifications are required
   * @param statusEntities - specific data category list for which notifications are required
   */
  void notify(AssetEntity assetEntity, List<SchemaClassificationStatusEntity> statusEntities);
}
