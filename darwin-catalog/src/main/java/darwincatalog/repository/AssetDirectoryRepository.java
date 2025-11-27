package darwincatalog.repository;

import darwincatalog.entity.AssetDirectoryEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetDirectoryRepository extends JpaRepository<AssetDirectoryEntity, Long> {

  boolean existsByAssetNameAndAssetPrefixAndDepth(String name, String prefix, int depth);

  List<AssetDirectoryEntity> findByAssetNameAndIsTerminal(String name, Boolean isTerminal);

  @Query(
      value =
          "select * from asset_directory where (:name_regex is null or :name_regex = '' or asset_name REGEXP :name_regex) and (:prefix_regex is null or :prefix_regex = '' or asset_prefix regexp :prefix_regex) and (-1 = :depth or depth = :depth) order by sort_path limit :page_size offset :offset",
      nativeQuery = true)
  List<AssetDirectoryEntity> findByNameAndPrefixMatchesRegex(
      @Param("name_regex") String nameRegex,
      @Param("prefix_regex") String prefixRegex,
      @Param("depth") int depth,
      @Param("page_size") int pageSize,
      @Param("offset") int offset);

  @Query(
      value =
          "select count(1) from asset_directory where (:name_regex is null or :name_regex = '' or asset_name REGEXP :name_regex) and (:prefix_regex is null or :prefix_regex = '' or asset_prefix regexp :prefix_regex) and (-1 = :depth or depth = :depth)",
      nativeQuery = true)
  long countByAssetNameAndAssetPrefixMatchesRegex(
      @Param("name_regex") String nameRegex,
      @Param("prefix_regex") String prefixRegex,
      @Param("depth") int depth);
}
