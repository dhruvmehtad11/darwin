package com.dream11.app.expectedrequestresponse;

import static com.dream11.app.constants.ExpectedDataPath.CassandraFeatureGroupWriteTestsDataPath;
import static com.dream11.app.constants.ExpectedDataPath.LegacyStackTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacyStackTestData {
  private static final JsonObject LegacyStackTests =
      new JsonObject(getFileBuffer(LegacyStackTestsDataPath));

  public static final JsonObject CreateTableEntityExists = LegacyStackTests.getJsonObject("createTableEntityExists");
  public static final JsonObject CreateTableSuccess = LegacyStackTests.getJsonObject("createTableSuccess");
  public static final JsonObject CreateTableInvalidSchema = LegacyStackTests.getJsonObject("createTableInvalidSchema");
  public static final JsonObject AlterTableFeatureGroupExists = LegacyStackTests.getJsonObject("alterTableFeatureGroupExists");
  public static final JsonObject AlterTableSuccess = LegacyStackTests.getJsonObject("alterTableSuccess");
  public static final JsonObject AlterTableInvalidDataType = LegacyStackTests.getJsonObject("alterTableInvalidDataType");
  public static final JsonObject LegacyReadFeatures = LegacyStackTests.getJsonObject("legacyReadFeatures");
  public static final JsonObject LegacyReadFeaturesBadRequest = LegacyStackTests.getJsonObject(
      "legacyReadFeaturesBadRequest");
  public static final JsonObject LegacyBulkReadFeatures = LegacyStackTests.getJsonObject("legacyBulkReadFeatures");
  public static final JsonObject LegacyAsyncBatchWriteFeatures = LegacyStackTests.getJsonObject("legacyAsyncBatchWriteFeatures");
  public static final JsonObject LegacyMultiBulkReadFeatures = LegacyStackTests.getJsonObject("legacyMultiBulkReadFeatures");
  public static final JsonObject LegacyMultiBulkReadFeaturesConstraintViolation = LegacyStackTests.getJsonObject(
      "legacyMultiBulkReadFeaturesConstraintViolation");


}
