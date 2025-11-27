package darwincatalog.repository;

import darwincatalog.entity.FieldLineageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldLineageRepository extends JpaRepository<FieldLineageEntity, Long> {

  List<FieldLineageEntity> findAllByAssetLineageId(Long assetLineageId);
}
