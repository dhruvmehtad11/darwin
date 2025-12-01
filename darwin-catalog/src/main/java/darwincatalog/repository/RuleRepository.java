package darwincatalog.repository;

import darwincatalog.entity.RuleEntity;
import java.util.List;
import java.util.Optional;
import org.openapitools.model.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {
  List<RuleEntity> findByAssetId(Long assetId);

  Optional<RuleEntity> findByAssetIdAndTypeAndLeftExpression(
      Long assetId, MetricType type, String leftExpression);

  Optional<RuleEntity> findByMonitorId(Long monitorId);
}
