package darwincatalog.repository;

import darwincatalog.entity.AssetEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {

  Optional<AssetEntity> findByFqdn(String assetName);

  @Query(
      value =
          "select * from asset where fqdn REGEXP :regex_expression and type != 'DASHBOARD' and type != 'SERVICE' order by fqdn limit :page_size offset :offset",
      nativeQuery = true)
  List<AssetEntity> findByNameMatchesRegex(
      @Param("regex_expression") String regex,
      @Param("page_size") int pageSize,
      @Param("offset") int offset);

  @Query(
      value =
          "select count(1) from asset where fqdn REGEXP :regex_expression and type != 'DASHBOARD' and type != 'SERVICE'",
      nativeQuery = true)
  long countByNameMatchesRegex(@Param("regex_expression") String regex);

  @Modifying
  @Transactional
  @Query(value = "delete from asset where fqdn=:name", nativeQuery = true)
  void deleteByName(@Param("name") String name);

  Page<AssetEntity> findAllByFqdnIn(Collection<String> fqdns, Pageable pageable);

  @Query(
      value =
          "select * from asset where fqdn like %:search_string% and fqdn like %:parsed_table_name",
      nativeQuery = true)
  List<AssetEntity> findByFqdnContainingAndFqdnEndsWith(
      @Param("search_string") String searchString,
      @Param("parsed_table_name") String parsedTableName);
}
