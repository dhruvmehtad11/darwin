package darwincatalog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import darwincatalog.testcontainers.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Asset;
import org.openapitools.model.GetAssetsRequest;
import org.openapitools.model.PaginatedAssets;
import org.openapitools.model.PaginatedList;
import org.openapitools.model.SearchAssetsRequest;
import org.openapitools.model.SearchEntry;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    scripts = "/sql/list_search_pagination.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = "/sql/list_search_pagination_cleanup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ReadAssetsControllerIT extends AbstractIntegrationTest {

  @Test
  @DisplayName("test getAssets with selected fields (only name, type, detail) and regex")
  void testGetAssets1() throws Exception {
    int total = 6;
    int limit = 2;
    int offset = 0;
    String regex = "^.+$";
    GetAssetsRequest getAssetsRequest = new GetAssetsRequest().regex(regex);
    String fields = "fqdn,type,detail";
    int totalCalls = (int) Math.ceil((double) total / limit);
    for (int i = 0; i < totalCalls; i++) {
      String path =
          String.format(
              "/v1/assets?regex=%s&offset=%s&page_size=%s&fields=%s",
              regex, offset + (i * limit), limit, fields);

      PaginatedAssets paginatedResponse =
          testUtil.postRequest(path, getAssetsRequest, PaginatedAssets.class, 200);

      assertEquals(limit, paginatedResponse.getPageSize());
      assertTrue(paginatedResponse.getAssets().size() <= limit);
      assertEquals(offset + (i * limit), paginatedResponse.getOffset());
      assertEquals(total, paginatedResponse.getTotal());
      assertEquals(limit, paginatedResponse.getAssets().size());
      for (Asset asset : paginatedResponse.getAssets()) {
        assertNull(asset.getDescription());
        assertNull(asset.getSourcePlatform());
        assertNull(asset.getBusinessRoster());

        assertEquals(0, asset.getRules().size());
        assertEquals(0, asset.getImmediateParents().size());
        assertEquals(0, asset.getTags().size());
        assertNull(asset.getAssetSchema());

        assertNotNull(asset.getFqdn());
        assertNotNull(asset.getType());
        assertNotNull(asset.getDetail());
      }
    }
  }

  @Test
  @DisplayName(
      "test getAssets with selected fields (only name, type, detail) and more specific regex")
  void testGetAssets2() throws Exception {
    int total = 3;
    int limit = 2;
    int offset = 0;
    String regex = "^example:table.*";
    GetAssetsRequest getAssetsRequest = new GetAssetsRequest().regex(regex);
    String fields = "fqdn,type,detail";
    int totalCalls = (int) Math.ceil((double) total / limit);
    for (int i = 0; i < totalCalls; i++) {
      String path =
          String.format(
              "/v1/assets?regex=%s&offset=%s&page_size=%s&fields=%s",
              regex, offset + (i * limit), limit, fields);

      PaginatedAssets paginatedResponse =
          testUtil.postRequest(path, getAssetsRequest, PaginatedAssets.class, 200);

      assertEquals(limit, paginatedResponse.getPageSize());
      assertTrue(paginatedResponse.getAssets().size() <= limit);
      assertEquals(offset + (i * limit), paginatedResponse.getOffset());
      assertEquals(total, paginatedResponse.getTotal());
      assertEquals(
          i == totalCalls - 1 ? total % limit : limit, paginatedResponse.getAssets().size());
    }
  }

  @Test
  @DisplayName("test getAssetByName with all selected fields")
  void testGetAssetByName() throws Exception {
    String assetName = "example:kafka:datahighway_logger:test_alleventattributes";
    String fields =
        "metadata, fqdn, quality_score, business_roster, description, rules, type, immediate_parents, tags, asset_updated_at, asset_created_at, source_platform, asset_schema, detail";
    String path = String.format("/v1/assets/%s?fields=%s", assetName, fields);
    Asset asset = testUtil.getRequest(path, Asset.class, 200);

    // cover cases where all fields need to be returned
    fields = "all";
    path = String.format("/v1/assets/%s?fields=%s", assetName, fields);
    testUtil.getRequest(path, Asset.class, 200);

    fields = "";
    path = String.format("/v1/assets/%s?fields=%s", assetName, fields);
    testUtil.getRequest(path, Asset.class, 200);

    path = String.format("/v1/assets/%s", assetName);
    testUtil.getRequest(path, Asset.class, 200);

    assertNotNull(asset);
    assertEquals(assetName, asset.getFqdn());
    assertNotNull(asset.getType());
    assertNotNull(asset.getDetail());
    assertNotNull(asset.getAssetSchema());
    assertEquals(3, asset.getAssetSchema().getSchemaJson().getFields().size());
    assertEquals("tourname", asset.getAssetSchema().getSchemaJson().getFields().get(1).getName());
  }

  @Test
  @DisplayName("test getAssets returns 400 if invalid regex provided")
  void testGetAssets3() throws Exception {
    String regex = "*";
    GetAssetsRequest getAssetsRequest = new GetAssetsRequest().regex(regex);
    String fields = "fqdn,type,detail";
    String path = String.format("/v1/assets?fields=%s", fields);

    PaginatedAssets paginatedResponse =
        testUtil.postRequest(path, getAssetsRequest, PaginatedAssets.class, 400);
    assertTrue(paginatedResponse.getAssets().isEmpty());
  }

  @Test
  @DisplayName("Test getAssets with pagination. Depth is not used in this test)")
  void testSearch1() throws Exception {
    int total = 12;
    int limit = 2;
    int offset = 0;
    String regex = "^.+$";
    SearchAssetsRequest searchAssetsRequest = new SearchAssetsRequest().assetNameRegex(regex);
    int totalCalls = (int) Math.ceil((double) total / limit);
    for (int i = 0; i < totalCalls; i++) {
      String path = String.format("/v1/search?offset=%s&page_size=%s", offset + (i * limit), limit);
      PaginatedList paginatedResponse =
          testUtil.postRequest(path, searchAssetsRequest, PaginatedList.class, 200);

      assertEquals(limit, paginatedResponse.getPageSize());
      assertTrue(paginatedResponse.getData().size() <= limit);
      assertEquals(offset + (i * limit), paginatedResponse.getOffset());
      assertEquals(total, paginatedResponse.getTotal());
      assertTrue(paginatedResponse.getData().size() <= limit);
    }
  }

  @Test
  @DisplayName("Test getAssets with pagination, with nagative depth")
  void testSearch2() throws Exception {
    int total = 12;
    int limit = 2;
    int offset = 0;
    int depth = -1;
    String regex = "^.+$";
    SearchAssetsRequest searchAssetsRequest = new SearchAssetsRequest().assetNameRegex(regex);
    int totalCalls = (int) Math.ceil((double) total / limit);
    for (int i = 0; i < totalCalls; i++) {
      String path =
          String.format(
              "/v1/search?&offset=%s&page_size=%s&depth=%s", offset + (i * limit), limit, depth);
      PaginatedList paginatedResponse =
          testUtil.postRequest(path, searchAssetsRequest, PaginatedList.class, 200);
      assertEquals(limit, paginatedResponse.getPageSize());
      assertTrue(paginatedResponse.getData().size() <= limit);
      assertEquals(offset + (i * limit), paginatedResponse.getOffset());
      assertEquals(total, paginatedResponse.getTotal());
      assertTrue(paginatedResponse.getData().size() <= limit);
    }
  }

  @Test
  @DisplayName("Test getAssets with pagination, with non negative depth")
  void testSearch3() throws Exception {
    int total = 4;
    int limit = 3;
    int offset = 0;
    int depth = 3;
    String regex = "^.+$";
    SearchAssetsRequest searchAssetsRequest = new SearchAssetsRequest().assetNameRegex(regex);
    int totalCalls = (int) Math.ceil((double) total / limit);
    for (int i = 0; i < totalCalls; i++) {
      String path =
          String.format(
              "/v1/search?offset=%s&page_size=%s&depth=%s", offset + (i * limit), limit, depth);
      PaginatedList paginatedResponse =
          testUtil.postRequest(path, searchAssetsRequest, PaginatedList.class, 200);
      assertEquals(limit, paginatedResponse.getPageSize());
      assertTrue(paginatedResponse.getData().size() <= limit);
      assertEquals(offset + (i * limit), paginatedResponse.getOffset());
      assertEquals(total, paginatedResponse.getTotal());

      for (SearchEntry entry : paginatedResponse.getData()) {
        assertTrue(entry.getDepth() <= depth);
      }
    }
  }

  @Test
  @DisplayName(
      "Test getAssets with pagination, with - only table regex. Pass both prefix and asset name regex")
  void testSearch4() throws Exception {
    int total = 5;
    int limit = 3;
    int offset = 0;
    int depth = -1;
    String nameRegex = "^.+$";
    String prefixRegex = "^example:table.*";
    SearchAssetsRequest searchAssetsRequest =
        new SearchAssetsRequest().assetNameRegex(nameRegex).assetPrefixRegex(prefixRegex);
    int totalCalls = (int) Math.ceil((double) total / limit);
    for (int i = 0; i < totalCalls; i++) {
      String path =
          String.format(
              "/v1/search?offset=%s&page_size=%s&depth=%s", offset + (i * limit), limit, depth);
      PaginatedList paginatedResponse =
          testUtil.postRequest(path, searchAssetsRequest, PaginatedList.class, 200);
      assertEquals(limit, paginatedResponse.getPageSize());
      assertTrue(paginatedResponse.getData().size() <= limit);
      assertEquals(offset + (i * limit), paginatedResponse.getOffset());
      assertEquals(total, paginatedResponse.getTotal());
    }
  }

  @Test
  @DisplayName("Return 400 if no search string provided")
  void testSearch5() throws Exception {
    String paginatedQuery = "/v1/search?offset=5&page_size=5";
    SearchAssetsRequest searchAssetsRequest = new SearchAssetsRequest();
    PaginatedList paginatedResponse =
        testUtil.postRequest(paginatedQuery, searchAssetsRequest, PaginatedList.class, 400);
    assertTrue(paginatedResponse.getData().isEmpty());
  }

  @Test
  @DisplayName("Return 400 if invalid regex provided")
  void testSearch6() throws Exception {
    String paginatedQuery = "/v1/search?offset=5&page_size=5";
    SearchAssetsRequest searchAssetsRequest = new SearchAssetsRequest().assetNameRegex("*");
    PaginatedList paginatedResponse =
        testUtil.postRequest(paginatedQuery, searchAssetsRequest, PaginatedList.class, 400);
    assertTrue(paginatedResponse.getData().isEmpty());
  }
}
