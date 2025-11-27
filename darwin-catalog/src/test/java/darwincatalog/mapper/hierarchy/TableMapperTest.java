package darwincatalog.mapper.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import darwincatalog.entity.AssetEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.TableDetail;

@ExtendWith(MockitoExtension.class)
class TableMapperTest {

  private final TableMapper tableMapper = new TableMapper();

  @Test
  void testGetDetails_withFullHierarchy() {
    AssetEntity entity = new AssetEntity();
    // org:type:subtype:catalog:db:table
    entity.setFqdn("org:type:subtype:catalog:db:table");
    TableDetail detail = (TableDetail) tableMapper.getDetails(entity);
    assertEquals("org", detail.getOrg());
    assertEquals("subtype", detail.getType());
    assertEquals("catalog", detail.getCatalogName());
    assertEquals("db", detail.getDatabaseName());
    assertEquals("table", detail.getTableName());
  }

  @Test
  void testGetDetails_withDatabaseOnly() {
    AssetEntity entity = new AssetEntity();
    // org:type:subtype:db:table
    entity.setFqdn("org:type:subtype:db:table");
    TableDetail detail = (TableDetail) tableMapper.getDetails(entity);
    assertEquals("org", detail.getOrg());
    assertEquals("subtype", detail.getType());
    assertNull(detail.getCatalogName());
    assertEquals("db", detail.getDatabaseName());
    assertEquals("table", detail.getTableName());
  }

  @Test
  void testGetDetails_withMinimalHierarchy() {
    AssetEntity entity = new AssetEntity();
    // org:type:subtype:table
    entity.setFqdn("org:type:subtype:table");
    TableDetail detail = (TableDetail) tableMapper.getDetails(entity);
    assertEquals("org", detail.getOrg());
    assertEquals("subtype", detail.getType());
    assertNull(detail.getCatalogName());
    assertNull(detail.getDatabaseName());
    assertEquals("table", detail.getTableName());
  }

  @Test
  void testGetDetails_withInsufficientComponents() {
    AssetEntity entity = new AssetEntity();
    // org:type:subtype
    entity.setFqdn("org:type:subtype");
    TableDetail detail = (TableDetail) tableMapper.getDetails(entity);
    assertNull(detail.getOrg());
    assertNull(detail.getType());
    assertNull(detail.getCatalogName());
    assertNull(detail.getDatabaseName());
    assertNull(detail.getTableName());
  }

  @Test
  void testGetDetails_withInsufficientComponents2() {
    AssetEntity entity = new AssetEntity();
    // org:type:subtype
    entity.setFqdn("org:type");
    TableDetail detail = (TableDetail) tableMapper.getDetails(entity);
    assertNull(detail.getOrg());
    assertNull(detail.getType());
    assertNull(detail.getCatalogName());
    assertNull(detail.getDatabaseName());
    assertNull(detail.getTableName());
  }

  @Test
  void testGetDetails_withInsufficientComponents3() {
    AssetEntity entity = new AssetEntity();
    // org:type:subtype
    entity.setFqdn("org");
    TableDetail detail = (TableDetail) tableMapper.getDetails(entity);
    assertNull(detail.getOrg());
    assertNull(detail.getType());
    assertNull(detail.getCatalogName());
    assertNull(detail.getDatabaseName());
    assertNull(detail.getTableName());
  }
}
