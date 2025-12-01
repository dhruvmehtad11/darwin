package darwincatalog.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.testcontainers.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.openapitools.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/sql/lineage.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/lineage_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class LineageIT extends AbstractIntegrationTest {

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Patch asset creates lineage relationship between assets in the seed data")
  @Order(1)
  void testPatchAssetLineage() throws Exception {

    // Asset: 4_L11
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("4_L11")
            .addParentDependenciesItem(
                new ParentDependency().name("1_L21").fieldsMap(Map.of("c1", List.of("c1"))))
            .addParentDependenciesItem(
                new ParentDependency()
                    .name("2_L22")
                    .fieldsMap(
                        Map.of(
                            "c1", List.of("c1"),
                            "c2", List.of("c1")))),
        Asset.class,
        200);

    // Asset: 5_L12
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("5_L12")
            .addParentDependenciesItem(
                new ParentDependency().name("3_L23").fieldsMap(Map.of("c1", List.of("c1"))))
            .addParentDependenciesItem(
                new ParentDependency()
                    .name("2_L22")
                    .fieldsMap(
                        Map.of(
                            "c3", List.of("c1"),
                            "c2", List.of("c1")))),
        Asset.class,
        200);

    // Asset: 6_M
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("6_M")
            .addParentDependenciesItem(
                new ParentDependency().name("4_L11").fieldsMap(Map.of("c1", List.of("c1"))))
            .addParentDependenciesItem(
                new ParentDependency().name("5_L12").fieldsMap(Map.of("c1", List.of("c1")))),
        Asset.class,
        200);

    // Asset: 7_R11
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("7_R11")
            .addParentDependenciesItem(
                new ParentDependency().name("6_M").fieldsMap(Map.of("c1", List.of("c1")))),
        Asset.class,
        200);

    // Asset: 8_R12
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("8_R12")
            .addParentDependenciesItem(
                new ParentDependency().name("6_M").fieldsMap(Map.of("c1", List.of("c1")))),
        Asset.class,
        200);

    // Asset: 9_R21
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("9_R21")
            .addParentDependenciesItem(new ParentDependency().name("6_M"))
            .addParentDependenciesItem(new ParentDependency().name("7_R11")),
        Asset.class,
        200);

    // Asset: 10_R22
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("10_R22")
            .addParentDependenciesItem(
                new ParentDependency().name("7_R11").fieldsMap(Map.of("c1", List.of("c1", "c2"))))
            .addParentDependenciesItem(
                new ParentDependency().name("8_R12").fieldsMap(Map.of("c1", List.of("c3", "c2")))),
        Asset.class,
        200);

    // Asset: 11_R23
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("11_R23")
            .addParentDependenciesItem(new ParentDependency().name("6_M"))
            .addParentDependenciesItem(new ParentDependency().name("8_R12")),
        Asset.class,
        200);
  }

  @Test
  @DisplayName("Attempts to set lineage relationship with self")
  @Order(2)
  void testSelfDependency() throws Exception {
    // Expect a 400 Bad Request due to self lineage
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("6_M")
            .addParentDependenciesItem(new ParentDependency().name("6_M")),
        Asset.class,
        400);
  }

  @Test
  @DisplayName("Attempts to set cyclic lineage relationship between assets")
  @Order(3)
  void testCyclicLineage() throws Exception {
    testUtil.patchRequest(
        "/v1/assets",
        new PatchAssetRequest()
            .assetFqdn("6_M")
            .addParentDependenciesItem(new ParentDependency().name("7_R11")),
        Asset.class,
        400);
  }

  @Test
  @DisplayName("End-to-end lineage test for asset 6_M")
  @Order(4)
  void testEndToEndLineage() throws Exception {

    Lineage expectedLineage =
        new Lineage()
            .addGraphItem(
                new AssetRelation().from("4_L11").to("6_M").fieldsMap(Map.of("c1", List.of("c1"))))
            .addGraphItem(
                new AssetRelation().from("6_M").to("7_R11").fieldsMap(Map.of("c1", List.of("c1"))))
            .addGraphItem(
                new AssetRelation().from("6_M").to("8_R12").fieldsMap(Map.of("c1", List.of("c1"))))
            .addGraphItem(new AssetRelation().from("6_M").to("9_R21"))
            .addGraphItem(new AssetRelation().from("6_M").to("11_R23"))
            .addGraphItem(new AssetRelation().from("7_R11").to("9_R21"))
            .addGraphItem(
                new AssetRelation()
                    .from("7_R11")
                    .to("10_R22")
                    .fieldsMap(Map.of("c1", List.of("c1", "c2"))))
            .addGraphItem(
                new AssetRelation()
                    .from("8_R12")
                    .to("10_R22")
                    .fieldsMap(Map.of("c1", List.of("c3", "c2"))))
            .addGraphItem(new AssetRelation().from("8_R12").to("11_R23"))
            .addGraphItem(
                new AssetRelation()
                    .from("1_L21")
                    .to("4_L11")
                    .fieldsMap(Map.of("c1", List.of("c1"))))
            .addGraphItem(
                new AssetRelation()
                    .from("2_L22")
                    .to("4_L11")
                    .fieldsMap(Map.of("c1", List.of("c1"), "c2", List.of("c1"))))
            .putAssetsInfoItem(
                "1_L21",
                new Asset()
                    .fqdn("1_L21")
                    .type(AssetType.TABLE)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(
                                        List.of(
                                            new Field().name("c1").type("string"),
                                            new Field().name("c2").type("string")))))
                    .detail(new TableDetail()))
            .putAssetsInfoItem(
                "2_L22",
                new Asset()
                    .fqdn("2_L22")
                    .type(AssetType.TABLE)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "f27fb6672172ce40c9269c9acc319538e695b8b4f3c24217366a7bbbcf86ab0f")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(
                                        List.of(
                                            new Field().name("c1").type("string"),
                                            new Field().name("c2").type("string"),
                                            new Field().name("c3").type("string"),
                                            new Field().name("c4").type("string")))))
                    .detail(new TableDetail()))
            .putAssetsInfoItem(
                "6_M",
                new Asset()
                    .fqdn("6_M")
                    .type(AssetType.TABLE)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "2006c74f182f528b592c2c09f92850833c5337d294e683859d0e1f1464256afc")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(List.of(new Field().name("c1").type("string")))))
                    .detail(new TableDetail()))
            .putAssetsInfoItem(
                "9_R21",
                new Asset().fqdn("9_R21").type(AssetType.DASHBOARD).detail(new DashboardDetail()))
            .putAssetsInfoItem(
                "4_L11",
                new Asset()
                    .fqdn("4_L11")
                    .type(AssetType.TABLE)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "766ac5705748d54fb861a58581582e63d2040a3e8cbaee91ff45e4c5a2e9e1e0")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(
                                        List.of(
                                            new Field().name("c1").type("string"),
                                            new Field().name("c2").type("string")))))
                    .detail(new TableDetail()))
            .putAssetsInfoItem(
                "11_R23",
                new Asset().fqdn("11_R23").type(AssetType.SERVICE).detail(new ServiceDetail()))
            .putAssetsInfoItem(
                "7_R11",
                new Asset()
                    .fqdn("7_R11")
                    .type(AssetType.TABLE)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(
                                        List.of(
                                            new Field().name("c1").type("string"),
                                            new Field().name("c2").type("string")))))
                    .detail(new TableDetail()))
            .putAssetsInfoItem(
                "10_R22",
                new Asset()
                    .fqdn("10_R22")
                    .type(AssetType.TABLE)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "f27fb6672172ce40c9269c9acc319538e695b8b4f3c24217366a7bbbcf86ab0f")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(
                                        List.of(
                                            new Field().name("c1").type("string"),
                                            new Field().name("c2").type("string"),
                                            new Field().name("c3").type("string"),
                                            new Field().name("c4").type("string")))))
                    .detail(new TableDetail()))
            .putAssetsInfoItem(
                "8_R12",
                new Asset()
                    .fqdn("8_R12")
                    .type(AssetType.KAFKA)
                    .assetSchema(
                        new AssetSchema()
                            .version(1)
                            .schemaHash(
                                "06a97911e822e6bc5d16eb69936b21e9ea5a1b9f5ca1c69c12c2d031c017d23d")
                            .schemaJson(
                                new SchemaStructure()
                                    .name("schema")
                                    .type(SchemaStructure.TypeEnum.RECORD)
                                    .fields(
                                        List.of(
                                            new Field().name("c1").type("string"),
                                            new Field().name("c2").type("string")))))
                    .detail(new KafkaDetail()));

    // Fetch the lineage for asset 6_M
    Lineage actualLineage =
        testUtil.getRequest(String.format("/v1/assets/%s/lineage", "4_L11"), Lineage.class, 200);

    // Validate the lineage structure
    String actualLineageJson = objectMapper.writeValueAsString(actualLineage);
    String expectedLineageJson = objectMapper.writeValueAsString(expectedLineage);
    assertEquals(expectedLineageJson, actualLineageJson);
  }
}
