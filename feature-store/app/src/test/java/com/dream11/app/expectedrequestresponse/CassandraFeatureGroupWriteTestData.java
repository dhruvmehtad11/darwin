package com.dream11.app.expectedrequestresponse;

import static com.dream11.app.constants.ExpectedDataPath.CassandraFeatureGroupWriteTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraFeatureGroupWriteTestData {
  private static final JsonObject CassandraFeatureGroupWriteTests =
      new JsonObject(getFileBuffer(CassandraFeatureGroupWriteTestsDataPath));

  public static final JsonObject SuccessWriteFeaturesTest = CassandraFeatureGroupWriteTests.getJsonObject("successWriteFeaturesTest");
  public static final JsonObject PartialWriteFeaturesTest = CassandraFeatureGroupWriteTests.getJsonObject("partialWriteFeaturesTest");

  public static final JsonObject FeatureGroupNotFoundWriteFeaturesTest = CassandraFeatureGroupWriteTests.getJsonObject("featureGroupNotFoundWriteFeaturesTest");
  public static final JsonObject SuccessWriteFeaturesToVersionTest = CassandraFeatureGroupWriteTests.getJsonObject("successWriteFeaturesToVersionTest");
  public static final JsonObject FailForAllRowsWithOneFailedColumnTest = CassandraFeatureGroupWriteTests.getJsonObject("failForAllRowsWithOneFailedColumnTest");
  public static final JsonObject FeatureGroupWriteBadRequestTest = CassandraFeatureGroupWriteTests.getJsonObject("featureGroupWriteBadRequestTest");
  public static final JsonObject FeatureGroupWritePartialSchemaTest = CassandraFeatureGroupWriteTests.getJsonObject("featureGroupWritePartialSchemaTest");
  public static final JsonObject FeatureGroupWriteBadPrimaryKeySchemaTest = CassandraFeatureGroupWriteTests.getJsonObject("featureGroupWriteBadPrimaryKeySchemaTest");
  public static final JsonObject FeatureGroupWriteBadFeatureVectorLengthSchemaTest = CassandraFeatureGroupWriteTests.getJsonObject("featureGroupWriteBadFeatureVectorLengthSchemaTest");
  public static final JsonObject SuccessBulkWriteFeaturesTest = CassandraFeatureGroupWriteTests.getJsonObject("successBulkWriteFeaturesTest");
  public static final JsonObject TenantSuccessWriteFeaturesTest = CassandraFeatureGroupWriteTests.getJsonObject(
      "tenantSuccessWriteFeaturesTest");

}
