package darwincatalog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import darwincatalog.testcontainers.AbstractIntegrationTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Asset;
import org.openapitools.model.AssetType;
import org.openapitools.model.DashboardDetail;
import org.openapitools.model.Field;
import org.openapitools.model.KafkaDetail;
import org.openapitools.model.ParentDependency;
import org.openapitools.model.PostAssetSchema;
import org.openapitools.model.RegisterAssetRequest;
import org.openapitools.model.SchemaStructure;
import org.openapitools.model.ServiceDetail;
import org.openapitools.model.StreamDetail;
import org.openapitools.model.TableDetail;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/sql/register_asset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = "/sql/register_asset_cleanup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RegisterAssetIT extends AbstractIntegrationTest {

  private static final String ENDPOINT = "/v1/assets/register";

  @Test
  @DisplayName("Successfully register a table asset most of the fields")
  void testRegisterTableAsset() throws Exception {
    TableDetail detail =
        new TableDetail()
            .org("example")
            .type("redshift")
            .catalogName("analytics")
            .databaseName("example")
            .tableName("test_users");

    Field field1 =
        new Field()
            .name("transaction_id")
            .type("bigint")
            .doc("Unique transaction identifier")
            .isPii("false");

    Field field2 = new Field().name("user_id").type("bigint").doc("User identifier").isPii("true");

    SchemaStructure schemaStructure =
        new SchemaStructure()
            .name("test_schema")
            .type(SchemaStructure.TypeEnum.RECORD)
            .fields(List.of(field1, field2));

    PostAssetSchema schema = new PostAssetSchema().schemaJson(schemaStructure);
    Map<String, List<String>> fieldsMap = new HashMap<>();
    fieldsMap.put("transaction_id", List.of("transaction_id"));
    fieldsMap.put("user_id", List.of("user_id"));
    fieldsMap.put("amount", List.of("transaction_id"));
    List<ParentDependency> parentDependencies =
        List.of(
            new ParentDependency()
                .name("example:table:redshift:segment:example:existing_transactions"),
            new ParentDependency()
                .name("example:table:redshift:segment:example:existing_transactions_2")
                .fieldsMap(fieldsMap));

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("retention_days", 90);
    metadata.put("data_classification", "internal");
    metadata.put("owner_team", "data_platform");

    RegisterAssetRequest request =
        new RegisterAssetRequest()
            .type(AssetType.TABLE)
            .detail(convertToMap(detail))
            .description("Test users table for integration tests")
            .businessRoster("data_platform")
            .assetSchema(schema)
            .parentDependencies(parentDependencies)
            .metadata(metadata);

    Asset asset = testUtil.postRequest(ENDPOINT, request, Asset.class, 200);

    assertNotNull(asset);
    assertEquals("example:table:redshift:analytics:example:test_users", asset.getFqdn());
    assertEquals(AssetType.TABLE, asset.getType());
    assertEquals("Test users table for integration tests", asset.getDescription());
    assertEquals("data_platform", asset.getBusinessRoster());
    assertEquals("contractcentral", asset.getSourcePlatform());
    assertNotNull(asset.getAssetCreatedAt());
    assertNotNull(asset.getAssetUpdatedAt());

    assertNotNull(asset.getAssetSchema());
    List<Field> fields = asset.getAssetSchema().getSchemaJson().getFields();
    assertEquals(2, fields.size());
    assertTrue(
        fields.stream()
            .anyMatch(
                field ->
                    "transaction_id".equals(field.getName()) && "false".equals(field.getIsPii())));
    assertTrue(
        fields.stream()
            .anyMatch(
                field -> "user_id".equals(field.getName()) && "true".equals(field.getIsPii())));

    assertEquals(2, asset.getImmediateParents().size());
    List<String> parentNames =
        asset.getImmediateParents().stream()
            .map(ParentDependency::getName)
            .collect(java.util.stream.Collectors.toList());
    assertEquals(2, parentNames.size());
    assertTrue(
        parentNames.contains("example:table:redshift:segment:example:existing_transactions"));
    assertTrue(
        parentNames.contains("example:table:redshift:segment:example:existing_transactions_2"));
    fieldsMap = asset.getImmediateParents().get(1).getFieldsMap();
    assertTrue(fieldsMap.containsKey("transaction_id"));
    assertTrue(fieldsMap.containsKey("user_id"));
    assertTrue(fieldsMap.containsKey("amount"));
    assertEquals(List.of("transaction_id"), fieldsMap.get("transaction_id"));
    assertEquals(List.of("user_id"), fieldsMap.get("user_id"));
    assertEquals(List.of("transaction_id"), fieldsMap.get("amount"));
    assertFalse(fieldsMap.containsKey("non_existent_key"));

    assertNotNull(asset.getMetadata());
    assertEquals(90, asset.getMetadata().get("retention_days"));
    assertEquals("internal", asset.getMetadata().get("data_classification"));
    assertEquals("data_platform", asset.getMetadata().get("owner_team"));
  }

  @Test
  @DisplayName("Successfully register a kafka asset")
  void testRegisterKafkaAsset() throws Exception {
    KafkaDetail detail =
        new KafkaDetail()
            .org("example")
            .clusterName("datahighway_logger")
            .topicName("test_user_events");

    RegisterAssetRequest request =
        new RegisterAssetRequest()
            .type(AssetType.KAFKA)
            .detail(convertToMap(detail))
            .description("Test kafka topic for user events")
            .businessRoster("analytics_team");

    Asset asset = testUtil.postRequest(ENDPOINT, request, Asset.class, 200);

    assertNotNull(asset);
    assertEquals("example:kafka:datahighway_logger:test_user_events", asset.getFqdn());
    assertEquals(AssetType.KAFKA, asset.getType());
    assertEquals("Test kafka topic for user events", asset.getDescription());
    assertEquals("analytics_team", asset.getBusinessRoster());
  }

  @Test
  @DisplayName("Successfully register a stream asset")
  void testRegisterStreamAsset() throws Exception {
    StreamDetail detail =
        new StreamDetail().org("example").tenant("analytics").streamName("test_stream_payments");

    RegisterAssetRequest request =
        new RegisterAssetRequest()
            .type(AssetType.STREAM)
            .detail(convertToMap(detail))
            .businessRoster("analytics_team")
            .description("Test stream for payment processing");

    Asset asset = testUtil.postRequest(ENDPOINT, request, Asset.class, 200);

    assertNotNull(asset);
    assertEquals("example:stream:analytics:test_stream_payments", asset.getFqdn());
    assertEquals(AssetType.STREAM, asset.getType());
    assertEquals("Test stream for payment processing", asset.getDescription());
  }

  /**
   * Helper method to convert detail objects to Map<String, String> This simulates how the frontend
   * would send the data
   */
  private Map<String, String> convertToMap(Object detail) {
    Map<String, String> map = new HashMap<>();

    if (detail instanceof TableDetail) {
      TableDetail tableDetail = (TableDetail) detail;
      map.put("org", tableDetail.getOrg());
      map.put("type", tableDetail.getType());
      map.put("catalog_name", tableDetail.getCatalogName());
      map.put("database_name", tableDetail.getDatabaseName());
      map.put("table_name", tableDetail.getTableName());
    } else if (detail instanceof KafkaDetail) {
      KafkaDetail kafkaDetail = (KafkaDetail) detail;
      map.put("org", kafkaDetail.getOrg());
      map.put("cluster_name", kafkaDetail.getClusterName());
      map.put("topic_name", kafkaDetail.getTopicName());
    } else if (detail instanceof StreamDetail) {
      StreamDetail streamDetail = (StreamDetail) detail;
      map.put("org", streamDetail.getOrg());
      map.put("tenant", streamDetail.getTenant());
      map.put("stream_name", streamDetail.getStreamName());
    } else if (detail instanceof ServiceDetail) {
      ServiceDetail serviceDetail = (ServiceDetail) detail;
      map.put("org", serviceDetail.getOrg());
    } else if (detail instanceof DashboardDetail) {
      DashboardDetail dashboardDetail = (DashboardDetail) detail;
      map.put("org", dashboardDetail.getOrg());
    }
    return map;
  }
}
