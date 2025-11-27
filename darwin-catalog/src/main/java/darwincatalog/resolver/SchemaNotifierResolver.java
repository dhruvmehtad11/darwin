package darwincatalog.resolver;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.SchemaClassificationStatusEntity;
import darwincatalog.service.notifier.SchemaUpdateNotifier;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchemaNotifierResolver {
  List<SchemaUpdateNotifier> schemaUpdateNotifiers;

  public SchemaNotifierResolver(List<SchemaUpdateNotifier> schemaUpdateNotifiers) {
    this.schemaUpdateNotifiers = schemaUpdateNotifiers;
  }

  public void notify(
      AssetEntity assetEntity, List<SchemaClassificationStatusEntity> statusEntities) {
    schemaUpdateNotifiers.stream()
        .filter(e -> e.isType(assetEntity))
        .findFirst()
        .ifPresent(
            notifier -> {
              log.info(
                  "Schema update notifier found for {}: {}",
                  assetEntity.getFqdn(),
                  notifier.getClass().getSimpleName());
              notifier.notify(assetEntity, statusEntities);
            });
  }
}
