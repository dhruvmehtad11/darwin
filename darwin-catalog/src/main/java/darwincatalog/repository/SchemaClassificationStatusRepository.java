package darwincatalog.repository;

import darwincatalog.entity.SchemaClassificationStatusEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemaClassificationStatusRepository
    extends JpaRepository<SchemaClassificationStatusEntity, Long> {
  List<SchemaClassificationStatusEntity> findBySchemaClassificationId(Long schemaClassificationId);
}
