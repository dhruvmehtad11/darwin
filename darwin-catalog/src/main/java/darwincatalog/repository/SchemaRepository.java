package darwincatalog.repository;

import darwincatalog.entity.SchemaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemaRepository extends JpaRepository<SchemaEntity, Long> {

  @Query(
      value =
          "select * from asset_schema where asset_id = :asset_id order by version_id desc limit 1",
      nativeQuery = true)
  Optional<SchemaEntity> findByAssetIdWithMaxVersionId(@Param("asset_id") Long assetId);
}
