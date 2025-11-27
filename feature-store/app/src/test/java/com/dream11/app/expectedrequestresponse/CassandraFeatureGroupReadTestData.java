package com.dream11.app.expectedrequestresponse;

import static com.dream11.app.constants.ExpectedDataPath.CassandraFeatureGroupReadTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CassandraFeatureGroupReadTestData {
  private static final JsonObject CassandraFeatureGroupRead =
      new JsonObject(getFileBuffer(CassandraFeatureGroupReadTestsDataPath));

  public static final JsonObject SuccessReadFeaturesTest = CassandraFeatureGroupRead.getJsonObject("successReadFeaturesTest");
  public static final JsonObject SuccessReadNullFeaturesTest = CassandraFeatureGroupRead.getJsonObject("successReadNullFeaturesTest");
  public static final JsonObject FailForAllNullFeaturesTest = CassandraFeatureGroupRead.getJsonObject("failForAllNullFeaturesTest");
  public static final JsonObject ReadFeaturesBadRequestColumnNotFoundTest =
      CassandraFeatureGroupRead.getJsonObject("readFeaturesBadRequestColumnNotFoundTest");
  public static final JsonObject ReadFeaturesBadRequestMissingPrimaryColumnsTest =
      CassandraFeatureGroupRead.getJsonObject("readFeaturesBadRequestMissingPrimaryColumnsTest");
  public static final JsonObject ReadFeaturesBadRequestDuplicateColumnsTest =
      CassandraFeatureGroupRead.getJsonObject("readFeaturesBadRequestDuplicateColumnsTest");
  public static final JsonObject ReadFeaturesBadRequestInvalidVectorSizeTest =
      CassandraFeatureGroupRead.getJsonObject("readFeaturesBadRequestInvalidVectorSizeTest");
  public static final JsonObject ReadFeaturesNotFoundTest =
      CassandraFeatureGroupRead.getJsonObject("readFeaturesNotFoundTest");

  public static final JsonObject FailForAllRowsNotBindingTest =
      CassandraFeatureGroupRead.getJsonObject("failForAllRowsNotBindingTest");

  public static final JsonObject SuccessMultiReadFeaturesTest =
      CassandraFeatureGroupRead.getJsonObject("successMultiReadFeaturesTest");

  public static final JsonObject SuccessMultiReadFeaturesWithErrorTest =
      CassandraFeatureGroupRead.getJsonObject("successMultiReadFeaturesWithErrorTest");

  public static final JsonObject TenantSuccessReadFeaturesTest = CassandraFeatureGroupRead.getJsonObject("tenantSuccessReadFeaturesTest");
  public static final JsonObject SuccessReadPartitionTest = CassandraFeatureGroupRead.getJsonObject("successReadPartitionTest");
  public static final JsonObject TenantReplicatedSuccessWriteFeaturesTest = CassandraFeatureGroupRead.getJsonObject(
      "tenantReplicatedSuccessWriteFeaturesTest");
  public static final JsonObject TenantReplicatedSuccessReadFeaturesTest = CassandraFeatureGroupRead.getJsonObject(
      "tenantReplicatedSuccessReadFeaturesTest");

}
