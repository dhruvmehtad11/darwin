package darwincatalog.repository;

import darwincatalog.entity.SchemaClassificationEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemaClassificationRepository
    extends JpaRepository<SchemaClassificationEntity, Long> {

  Optional<SchemaClassificationEntity> findBySchemaId(Long schemaId);

  @Query(
      value =
          "SELECT sc.* FROM schema_classification sc where sc.id in ("
              + "select distinct schema_classification_id "
              + "from schema_classification_status scs "
              + "WHERE (:classificationType IS NULL OR scs.classification_type = :classificationType) "
              + "AND (:classificationStatus IS NULL OR scs.classification_status = :classificationStatus) "
              + "AND (:classificationMethod IS NULL OR scs.classification_method = :classificationMethod) "
              + "AND (:schemaClassificationId IS NULL OR sc.id = :schemaClassificationId)) "
              + "ORDER BY sc.created_at",
      nativeQuery = true)
  Page<SchemaClassificationEntity> findSchemasWithClassificationFilters(
      @Param("classificationType") String classificationType,
      @Param("classificationStatus") String classificationStatus,
      @Param("classificationMethod") String classificationMethod,
      @Param("schemaClassificationId") Long schemaClassificationId,
      Pageable pageable);
}
