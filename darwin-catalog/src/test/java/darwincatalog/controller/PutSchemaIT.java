package darwincatalog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import darwincatalog.testcontainers.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openapitools.model.AssetSchema;
import org.openapitools.model.Field;
import org.openapitools.model.PostSchemaClassificationStatus;
import org.openapitools.model.PutAssetSchema;
import org.openapitools.model.SchemaClassificationCategory;
import org.openapitools.model.SchemaStructure;
import org.springframework.test.context.jdbc.Sql;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/sql/put_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/put_schema_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PutSchemaIT extends AbstractIntegrationTest {

  private static final String ENDPOINT = "/v1/assets/schema";
  private static final String EXISTING_ASSET_FQDN =
      "example:table:redshift:segment:example:test_existing_asset";

  @Test
  @DisplayName("Successfully create first schema for asset via PUT")
  @Order(100)
  void testCreateFirstSchemaViaPut() throws Exception {
    Field field1 = new Field().name("user_id").type("bigint").doc("Unique user identifier");

    Field field2 = new Field().name("email").type("varchar").doc("User email address");

    SchemaStructure schemaStructure =
        new SchemaStructure()
            .name("user_schema")
            .type(SchemaStructure.TypeEnum.RECORD)
            .fields(List.of(field1, field2));

    PutAssetSchema request =
        new PutAssetSchema().assetFqdn(EXISTING_ASSET_FQDN).schemaJson(schemaStructure);

    AssetSchema response = testUtil.putRequest(ENDPOINT, request, AssetSchema.class, 200);

    assertNotNull(response);
    assertEquals(1, response.getVersion());
    assertNotNull(response.getSchemaJson());
    assertEquals("user_schema", response.getSchemaJson().getName());
    assertEquals(2, response.getSchemaJson().getFields().size());

    List<Field> fields = response.getSchemaJson().getFields();
    assertTrue(fields.stream().anyMatch(field -> "user_id".equals(field.getName())));
    assertTrue(fields.stream().anyMatch(field -> "email".equals(field.getName())));
  }

  @Test
  @DisplayName("Successfully update schema with explicit version increment")
  @Order(200)
  void testUpdateSchemaWithVersionIncrement() throws Exception {
    Field field1 = new Field().name("user_id").type("bigint").doc("Unique user identifier");

    Field field2 = new Field().name("email").type("varchar").doc("User email address");

    Field field3 = new Field().name("age").type("varchar").doc("User's age");

    SchemaStructure schemaStructure =
        new SchemaStructure()
            .name("user_schema_2")
            .type(SchemaStructure.TypeEnum.RECORD)
            .fields(List.of(field1, field2, field3));

    PutAssetSchema initialRequest =
        new PutAssetSchema().assetFqdn(EXISTING_ASSET_FQDN).version(2).schemaJson(schemaStructure);

    AssetSchema response = testUtil.putRequest(ENDPOINT, initialRequest, AssetSchema.class, 200);

    assertNotNull(response);
    assertEquals(2, response.getVersion());
    assertEquals(3, response.getSchemaJson().getFields().size());
    assertTrue(
        response.getSchemaJson().getFields().stream()
            .anyMatch(field -> "age".equals(field.getName())));
  }

  @Test
  @DisplayName("Fail to update schema with older version than existing")
  @Order(300)
  void testUpdateWithOlderVersion() throws Exception {
    Field olderField =
        new Field().name("old_field").type("varchar").doc("Old field").isPii("false");

    SchemaStructure olderSchema =
        new SchemaStructure()
            .name("inventory_schema")
            .type(SchemaStructure.TypeEnum.RECORD)
            .fields(List.of(olderField));

    PutAssetSchema olderRequest =
        new PutAssetSchema().assetFqdn(EXISTING_ASSET_FQDN).version(-1).schemaJson(olderSchema);

    testUtil.putRequest(ENDPOINT, olderRequest, AssetSchema.class, 400);
  }

  @Test
  @DisplayName(
      "Successfully create schema with classification status. Require explicit newer schema version for schema evolution")
  @Order(400)
  void testCreateSchemaWithClassificationStatus() throws Exception {
    Field field1 =
        new Field().name("ssn").type("varchar").doc("Social Security Number").isPii("true");

    Field field2 =
        new Field().name("credit_card").type("varchar").doc("Credit card number").isPii("true");

    SchemaStructure schemaStructure =
        new SchemaStructure()
            .name("sensitive_data_schema")
            .type(SchemaStructure.TypeEnum.RECORD)
            .fields(List.of(field1, field2));

    PostSchemaClassificationStatus piiClassification =
        new PostSchemaClassificationStatus()
            .category(SchemaClassificationCategory.PII)
            .required(true);

    PutAssetSchema request =
        new PutAssetSchema()
            .assetFqdn(EXISTING_ASSET_FQDN)
            .schemaJson(schemaStructure)
            .version(3)
            .schemaClassificationStatus(List.of(piiClassification));

    AssetSchema response = testUtil.putRequest(ENDPOINT, request, AssetSchema.class, 200);

    assertNotNull(response);
    assertEquals(3, response.getVersion());
    assertEquals("sensitive_data_schema", response.getSchemaJson().getName());
  }

  @Test
  @DisplayName("Fail to update schema without version when schema differs from existing")
  @Order(300)
  void testUpdateWithoutVersionWithDifferentSchema() throws Exception {
    Field differentField =
        new Field().name("category_name").type("varchar").doc("Category name").isPii("false");

    SchemaStructure differentSchema =
        new SchemaStructure()
            .name("category_schema_new")
            .type(SchemaStructure.TypeEnum.RECORD)
            .fields(List.of(differentField));

    PutAssetSchema differentRequest =
        new PutAssetSchema().assetFqdn(EXISTING_ASSET_FQDN).schemaJson(differentSchema);

    testUtil.putRequest(ENDPOINT, differentRequest, AssetSchema.class, 400);
  }
}
