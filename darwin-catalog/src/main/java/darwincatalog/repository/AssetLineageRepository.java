package darwincatalog.repository;

import static darwincatalog.repository.Queries.ASSET_LINEAGE_RECURSIVE_CTE;
import static darwincatalog.repository.Queries.DOWNSTREAM_ASSET_ID_RECURSIVE_CTE;

import darwincatalog.entity.AssetLineageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetLineageRepository extends JpaRepository<AssetLineageEntity, Long> {

  @Query(
      value = "select to_asset_id from asset_lineage where from_asset_id=:from_asset_id",
      nativeQuery = true)
  List<Long> findByFromAssetEntity(@Param("from_asset_id") long fromAssetId);

  @Query(value = ASSET_LINEAGE_RECURSIVE_CTE, nativeQuery = true)
  List<AssetLineageEntity> findAssetLineageByAssetId(@Param("asset_id") long assetId);

  List<AssetLineageEntity> getAssetLineageEntitiesByToAssetId(Long id);

  @Query(value = DOWNSTREAM_ASSET_ID_RECURSIVE_CTE, nativeQuery = true)
  List<Long> getRecursiveDownStreamAssetIds(@Param("asset_id") Long asset_id);

  List<AssetLineageEntity> findByToAssetId(Long toAssetId);
}
