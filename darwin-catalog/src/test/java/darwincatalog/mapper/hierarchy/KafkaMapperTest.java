package darwincatalog.mapper.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import darwincatalog.entity.AssetEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.KafkaDetail;

@ExtendWith(MockitoExtension.class)
class KafkaMapperTest {
  private final KafkaMapper kafkaMapper = new KafkaMapper();

  @Test
  void testGetDetails_withFullHierarchy() {
    AssetEntity entity = new AssetEntity();
    // org:type:cluster:topic
    entity.setFqdn("org:type:cluster:topic");
    KafkaDetail detail = kafkaMapper.getDetails(entity);
    assertEquals("org", detail.getOrg());
    assertEquals("cluster", detail.getClusterName());
    assertEquals("topic", detail.getTopicName());
  }

  @Test
  void testGetDetails_withMinimalHierarchy() {
    AssetEntity entity = new AssetEntity();
    // org:type:topic
    entity.setFqdn("org:type:topic");
    KafkaDetail detail = kafkaMapper.getDetails(entity);
    assertEquals("org", detail.getOrg());
    assertNull(detail.getClusterName());
    assertEquals("topic", detail.getTopicName());
  }

  @Test
  void testGetDetails_withInsufficientComponents() {
    AssetEntity entity = new AssetEntity();
    // org:type
    entity.setFqdn("org:type");
    KafkaDetail detail = kafkaMapper.getDetails(entity);
    assertNull(detail.getOrg());
    assertNull(detail.getClusterName());
    assertNull(detail.getTopicName());
  }

  @Test
  void testGetDetails_withInsufficientComponents2() {
    AssetEntity entity = new AssetEntity();
    // org:type
    entity.setFqdn("org");
    KafkaDetail detail = kafkaMapper.getDetails(entity);
    assertNull(detail.getOrg());
    assertNull(detail.getClusterName());
    assertNull(detail.getTopicName());
  }
}
