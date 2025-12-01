package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.CassandraMetastoreSearchTestsDataPath;
import static com.dream11.admin.constants.ExpectedDataPath.FctAppLayerTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;

public class FctAppLayerTestData {
  private static final JsonObject FctAppLayerTests =
      new JsonObject(getFileBuffer(FctAppLayerTestsDataPath));

  public static final JsonObject GetFeatureGroupsCountWithoutSearchStringAndFiltersTest =
      FctAppLayerTests.getJsonObject("getFeatureGroupsCountWithoutSearchStringAndFiltersTest");

  public static final JsonObject GetFeatureGroupsCountWithOwnerFiltersAndWithoutSearchStringTest =
      FctAppLayerTests.getJsonObject("getFeatureGroupsCountWithOwnerFiltersAndWithoutSearchStringTest");

  public static final JsonObject GetFeatureGroupsCountWithTagFiltersAndWithoutSearchStringTest =
      FctAppLayerTests.getJsonObject("getFeatureGroupsCountWithTagFiltersAndWithoutSearchStringTest");
  public static final JsonObject GetFeatureGroupsCountWithAllFiltersAndWithoutSearchStringTest =
      FctAppLayerTests.getJsonObject("getFeatureGroupsCountWithAllFiltersAndWithoutSearchStringTest");
  public static final JsonObject GetFeatureGroupsCountWithoutFiltersAndWithSearchStringTest =
      FctAppLayerTests.getJsonObject("getFeatureGroupsCountWithoutFiltersAndWithSearchStringTest");
  public static final JsonObject GetFeatureGroupsFilters =
      FctAppLayerTests.getJsonObject("getFeatureGroupsFilters");


}
