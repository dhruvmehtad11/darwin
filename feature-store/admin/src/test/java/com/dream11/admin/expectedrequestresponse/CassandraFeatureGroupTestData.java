package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.CassandraFeatureGroupTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;

public class CassandraFeatureGroupTestData {
  private static final JsonObject CassandraFeatureGroupTests =
      new JsonObject(getFileBuffer(CassandraFeatureGroupTestsDataPath));

  public static final JsonObject SuccessFeatureGroupCreationTest = CassandraFeatureGroupTests.getJsonObject("successFeatureGroupCreationTest");
  public static final JsonObject SuccessFeatureGroupUpgradeTest = CassandraFeatureGroupTests.getJsonObject("successFeatureGroupUpgradeTest");
  public static final JsonObject InvalidRequestFeatureGroupCreationTest =
      CassandraFeatureGroupTests.getJsonObject("invalidRequestFeatureGroupCreationTest");
  public static final JsonObject FeatureGroupAlreadyExistsTest = CassandraFeatureGroupTests.getJsonObject("featureGroupAlreadyExistsTest");
  public static final JsonObject FeatureGroupVersioningDisabledTest =
      CassandraFeatureGroupTests.getJsonObject("featureGroupVersioningDisabledTest");
  public static final JsonObject SuccessGetFeatureGroupTest = CassandraFeatureGroupTests.getJsonObject("successGetFeatureGroupTest");
  public static final JsonObject SuccessGetFeatureGroupMetadataTest =
      CassandraFeatureGroupTests.getJsonObject("successGetFeatureGroupMetadataTest");
  public static final JsonObject SuccessGetFeatureGroupSchemaTest = CassandraFeatureGroupTests.getJsonObject("successGetFeatureGroupSchemaTest");
  public static final JsonObject FailedGetFeatureGroupTest = CassandraFeatureGroupTests.getJsonObject("failedGetFeatureGroupTest");
  public static final JsonObject InvalidRequestGetFeatureGroupTest =
      CassandraFeatureGroupTests.getJsonObject("invalidRequestGetFeatureGroupTest");

  public static final JsonObject SuccessGetFeatureGroupLatestVersionTest =
      CassandraFeatureGroupTests.getJsonObject("successGetFeatureGroupLatestVersionTest");
  public static final JsonObject InvalidRequestGetFeatureGroupLatestVersionTest =
      CassandraFeatureGroupTests.getJsonObject("invalidRequestGetFeatureGroupLatestVersionTest");
  public static final JsonObject EntityNotFoundFeatureGroupCreationTest =
      CassandraFeatureGroupTests.getJsonObject("entityNotFoundFeatureGroupCreationTest");
  public static final JsonObject FeatureGroupCreationInvalidSchemaTest =
      CassandraFeatureGroupTests.getJsonObject("featureGroupCreationInvalidSchemaTest");

  public static final JsonObject FeatureGroupCreationInvalidSchemaTest1 =
      CassandraFeatureGroupTests.getJsonObject("featureGroupCreationInvalidSchemaTest1");

  public static final JsonObject UpdateFeatureGroupTenantTest =
      CassandraFeatureGroupTests.getJsonObject("updateFeatureGroupTenantTest");

  public static final JsonObject MoveBackFeatureGroupTenantTest =
      CassandraFeatureGroupTests.getJsonObject("moveBackFeatureGroupTenantTest");

  public static final JsonObject PutFeatureGroupRunDataTest =
      CassandraFeatureGroupTests.getJsonObject("putFeatureGroupRunDataTest");
  public static final JsonObject PutFeatureGroupRunDataLongCountTest =
      CassandraFeatureGroupTests.getJsonObject("putFeatureGroupRunDataLongCountTest");
  public static final JsonObject GetFeatureGroupRunDataTest =
      CassandraFeatureGroupTests.getJsonObject("getFeatureGroupRunDataTest");

  public static final JsonObject UpdateAllReaderFeatureGroupTenantTest =
      CassandraFeatureGroupTests.getJsonObject("updateAllReaderFeatureGroupTenantTest");
  public static final JsonObject UpdateAllWriterFeatureGroupTenantTest =
      CassandraFeatureGroupTests.getJsonObject("updateAllWriterFeatureGroupTenantTest");
  public static final JsonObject UpdateAllConsumerFeatureGroupTenantTest =
      CassandraFeatureGroupTests.getJsonObject("updateAllConsumerFeatureGroupTenantTest");

  public static final JsonObject VerifyAllUpdateFeatureGroupTenantTest =
      CassandraFeatureGroupTests.getJsonObject("verifyAllUpdateFeatureGroupTenantTest");
}
