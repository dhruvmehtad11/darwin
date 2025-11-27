package darwincatalog.repository;

import darwincatalog.entity.AssetTagRelationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AssetTagRelationRepository extends JpaRepository<AssetTagRelationEntity, Integer> {

  @Modifying
  @Transactional
  @Query(value = "delete from asset_tag_relation where id=:id", nativeQuery = true)
  void deleteByTagId(@Param("id") Long id);

  List<AssetTagRelationEntity> findAllByAssetId(Long assetId);
}
