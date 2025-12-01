package darwincatalog.mapper.hierarchy;

import darwincatalog.entity.AssetEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.AssetType;
import org.openapitools.model.StreamDetail;

@ExtendWith(MockitoExtension.class)
class StreamMapperTest {
  private final StreamMapper streamMapper = new StreamMapper();

  @Test
  void testGetAssetType() {
    Assertions.assertEquals(AssetType.STREAM, streamMapper.getAssetType());
  }

  @Test
  void testIsType() {
    Assertions.assertTrue(streamMapper.isType(AssetType.STREAM));
  }

  @Test
  void testGetDetails_withFullHierarchy() {
    AssetEntity entity = new AssetEntity();
    // org:stream:tenant:stream_name
    entity.setFqdn("example:stream:Streams-internal:default.cli_stream_1");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertEquals("example", detail.getOrg());
    Assertions.assertEquals("Streams-internal", detail.getTenant());
    Assertions.assertEquals("default.cli_stream_1", detail.getStreamName());
  }

  @Test
  void testGetDetails_withMinimalHierarchy() {
    AssetEntity entity = new AssetEntity();
    // org:stream:stream_name
    entity.setFqdn("example:stream:my_stream");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertEquals("example", detail.getOrg());
    Assertions.assertNull(detail.getTenant());
    Assertions.assertEquals("my_stream", detail.getStreamName());
  }

  @Test
  void testGetDetails_withComplexStreamName() {
    AssetEntity entity = new AssetEntity();
    // org:stream:tenant:complex_stream_name
    entity.setFqdn("org:stream:tenant:user.events.stream_v2");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertEquals("org", detail.getOrg());
    Assertions.assertEquals("tenant", detail.getTenant());
    Assertions.assertEquals("user.events.stream_v2", detail.getStreamName());
  }

  @Test
  void testGetDetails_withLongTenantName() {
    AssetEntity entity = new AssetEntity();
    // org:stream:long_tenant_name:stream_name
    entity.setFqdn("myorg:stream:production-environment-tenant:analytics_stream");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertEquals("myorg", detail.getOrg());
    Assertions.assertEquals("production-environment-tenant", detail.getTenant());
    Assertions.assertEquals("analytics_stream", detail.getStreamName());
  }

  @Test
  void testGetDetails_withInsufficientComponents() {
    AssetEntity entity = new AssetEntity();
    // org:stream
    entity.setFqdn("org:stream");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertNull(detail.getOrg());
    Assertions.assertNull(detail.getTenant());
    Assertions.assertNull(detail.getStreamName());
  }

  @Test
  void testGetDetails_withInsufficientComponents2() {
    AssetEntity entity = new AssetEntity();
    // org
    entity.setFqdn("org");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertNull(detail.getOrg());
    Assertions.assertNull(detail.getTenant());
    Assertions.assertNull(detail.getStreamName());
  }

  @Test
  void testGetDetails_withEmptyString() {
    AssetEntity entity = new AssetEntity();
    entity.setFqdn("");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertNull(detail.getOrg());
    Assertions.assertNull(detail.getTenant());
    Assertions.assertNull(detail.getStreamName());
  }

  @Test
  void testGetDetails_withSingleComponent() {
    AssetEntity entity = new AssetEntity();
    entity.setFqdn("single_component");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertNull(detail.getOrg());
    Assertions.assertNull(detail.getTenant());
    Assertions.assertNull(detail.getStreamName());
  }

  @Test
  void testGetDetails_withSpecialCharactersInStreamName() {
    AssetEntity entity = new AssetEntity();
    // org:stream:tenant:stream_name_with_special_chars
    entity.setFqdn("org:stream:tenant:user-events_stream.v1");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertEquals("org", detail.getOrg());
    Assertions.assertEquals("tenant", detail.getTenant());
    Assertions.assertEquals("user-events_stream.v1", detail.getStreamName());
  }

  @Test
  void testGetDetails_withNumericComponents() {
    AssetEntity entity = new AssetEntity();
    // org:stream:tenant:stream_name with numbers
    entity.setFqdn("org123:stream:tenant456:stream789");
    StreamDetail detail = streamMapper.getDetails(entity);
    Assertions.assertEquals("org123", detail.getOrg());
    Assertions.assertEquals("tenant456", detail.getTenant());
    Assertions.assertEquals("stream789", detail.getStreamName());
  }
}
