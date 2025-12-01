package com.dream11.app.restfalllbackintegrationtests;

import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.FailForAllNullFeaturesTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.FailForAllRowsNotBindingTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.ReadFeaturesBadRequestColumnNotFoundTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.ReadFeaturesBadRequestDuplicateColumnsTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.ReadFeaturesBadRequestInvalidVectorSizeTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.ReadFeaturesBadRequestMissingPrimaryColumnsTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.ReadFeaturesNotFoundTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.SuccessMultiReadFeaturesTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.SuccessMultiReadFeaturesWithErrorTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.SuccessReadFeaturesTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.SuccessReadNullFeaturesTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupReadTestData.TenantSuccessReadFeaturesTest;
import static com.dream11.app.expectedrequestresponse.CassandraFeatureGroupWriteTestData.*;
import static com.dream11.app.expectedrequestresponse.LegacyStackTestData.LegacyAsyncBatchWriteFeatures;
import static com.dream11.app.expectedrequestresponse.LegacyStackTestData.LegacyBulkReadFeatures;
import static com.dream11.app.expectedrequestresponse.LegacyStackTestData.LegacyMultiBulkReadFeatures;
import static com.dream11.app.expectedrequestresponse.LegacyStackTestData.LegacyMultiBulkReadFeaturesConstraintViolation;
import static com.dream11.app.expectedrequestresponse.LegacyStackTestData.LegacyReadFeatures;
import static com.dream11.app.expectedrequestresponse.LegacyStackTestData.LegacyReadFeaturesBadRequest;
import static com.dream11.core.util.Utils.getAndAssertResponse;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SetUp.class)
public class RestIT {
  // write tests
  @Test
  public void successWriteFeaturesTest() {
    JsonObject expectedResponse = SuccessWriteFeaturesTest.getJsonObject("response");
    getAndAssertResponse(SuccessWriteFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void partialWriteFeaturesTest() {
    JsonObject expectedResponse = PartialWriteFeaturesTest.getJsonObject("response");
    getAndAssertResponse(PartialWriteFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupNotFoundWriteFeaturesTest() {
    JsonObject expectedResponse = FeatureGroupNotFoundWriteFeaturesTest.getJsonObject("response");
    getAndAssertResponse(FeatureGroupNotFoundWriteFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successWriteFeaturesToVersionTest() {
    JsonObject expectedResponse = SuccessWriteFeaturesToVersionTest.getJsonObject("response");
    getAndAssertResponse(SuccessWriteFeaturesToVersionTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void failForAllRowsWithOneFailedColumnTest() {
    JsonObject expectedResponse = FailForAllRowsWithOneFailedColumnTest.getJsonObject("response");
    getAndAssertResponse(FailForAllRowsWithOneFailedColumnTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupWriteBadRequestTest() {
    JsonObject expectedResponse = FeatureGroupWriteBadRequestTest.getJsonObject("response");
    getAndAssertResponse(FeatureGroupWriteBadRequestTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupWritePartialSchemaTest() {
    JsonObject expectedResponse = FeatureGroupWritePartialSchemaTest.getJsonObject("response");
    getAndAssertResponse(FeatureGroupWritePartialSchemaTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupWriteBadPrimaryKeySchemaTest() {
    JsonObject expectedResponse = FeatureGroupWriteBadPrimaryKeySchemaTest.getJsonObject("response");
    getAndAssertResponse(FeatureGroupWriteBadPrimaryKeySchemaTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupWriteBadFeatureVectorLengthSchemaTest() {
    JsonObject expectedResponse = FeatureGroupWriteBadFeatureVectorLengthSchemaTest.getJsonObject("response");
    getAndAssertResponse(FeatureGroupWriteBadFeatureVectorLengthSchemaTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successBulkWriteFeaturesTest() {
    JsonObject expectedResponse = SuccessBulkWriteFeaturesTest.getJsonObject("response");
    getAndAssertResponse(SuccessBulkWriteFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void tenantSuccessWriteFeaturesTest() {
    // only in tenant cassandra so no need to verify via query again
    JsonObject expectedResponse = TenantSuccessWriteFeaturesTest.getJsonObject("response");
    getAndAssertResponse(TenantSuccessWriteFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  // read tests
  @Test
  public void successReadFeaturesTest() {
    JsonObject expectedResponse = SuccessReadFeaturesTest.getJsonObject("response");
    getAndAssertResponse(SuccessReadFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successReadNullFeaturesTest() {
    JsonObject expectedResponse = SuccessReadNullFeaturesTest.getJsonObject("response");
    getAndAssertResponse(SuccessReadNullFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void FailForAllNullFeaturesTest() {
    JsonObject expectedResponse = FailForAllNullFeaturesTest.getJsonObject("response");
    getAndAssertResponse(FailForAllNullFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void readFeaturesBadRequestColumnNotFoundTest() {
    JsonObject expectedResponse = ReadFeaturesBadRequestColumnNotFoundTest.getJsonObject("response");
    getAndAssertResponse(ReadFeaturesBadRequestColumnNotFoundTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void readFeaturesBadRequestMissingPrimaryColumnsTest() {
    JsonObject expectedResponse = ReadFeaturesBadRequestMissingPrimaryColumnsTest.getJsonObject("response");
    getAndAssertResponse(ReadFeaturesBadRequestMissingPrimaryColumnsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void readFeaturesBadRequestDuplicateColumnsTest() {
    JsonObject expectedResponse = ReadFeaturesBadRequestDuplicateColumnsTest.getJsonObject("response");
    getAndAssertResponse(ReadFeaturesBadRequestDuplicateColumnsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void readFeaturesBadRequestInvalidVectorSizeTest() {
    JsonObject expectedResponse = ReadFeaturesBadRequestInvalidVectorSizeTest.getJsonObject("response");
    getAndAssertResponse(ReadFeaturesBadRequestInvalidVectorSizeTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void readFeaturesNotFoundTest() {
    JsonObject expectedResponse = ReadFeaturesNotFoundTest.getJsonObject("response");
    getAndAssertResponse(ReadFeaturesNotFoundTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void failForRowsBindingTest() {
    JsonObject expectedResponse = FailForAllRowsNotBindingTest.getJsonObject("response");
    getAndAssertResponse(FailForAllRowsNotBindingTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successMultiReadFeaturesTest() {
    JsonObject expectedResponse = SuccessMultiReadFeaturesTest.getJsonObject("response");
    getAndAssertResponse(SuccessMultiReadFeaturesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successMultiReadFeaturesWithErrorTest() {
    JsonObject expectedResponse = SuccessMultiReadFeaturesWithErrorTest.getJsonObject("response");
    getAndAssertResponse(SuccessMultiReadFeaturesWithErrorTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void legacyReadFeatures() {
    JsonObject expectedResponse = LegacyReadFeatures.getJsonObject("response");
    getAndAssertResponse(LegacyReadFeatures.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void legacyReadFeaturesBadRequest() {
    JsonObject expectedResponse = LegacyReadFeaturesBadRequest.getJsonObject("response");
    getAndAssertResponse(LegacyReadFeaturesBadRequest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void legacyBulkReadFeatures() {
    JsonObject expectedResponse = LegacyBulkReadFeatures.getJsonObject("response");
    getAndAssertResponse(LegacyBulkReadFeatures.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void legacyAsyncBatchWriteFeatures() {
    JsonObject expectedResponse = LegacyAsyncBatchWriteFeatures.getJsonObject("response");
    getAndAssertResponse(LegacyAsyncBatchWriteFeatures.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void legacyMultiBulkReadFeatures() {
    JsonObject expectedResponse = LegacyMultiBulkReadFeatures.getJsonObject("response");
    getAndAssertResponse(LegacyMultiBulkReadFeatures.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void legacyMultiBulkReadFeaturesConstraintViolation() {
    JsonObject expectedResponse = LegacyMultiBulkReadFeaturesConstraintViolation.getJsonObject("response");
    getAndAssertResponse(LegacyMultiBulkReadFeaturesConstraintViolation.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void tenantSuccessReadFeaturesTest() {
    // only in tenant cassandra so no need to verify via query again
    JsonObject expectedResponse = TenantSuccessReadFeaturesTest.getJsonObject("response");
    getAndAssertResponse(TenantSuccessReadFeaturesTest.getJsonObject("request"), expectedResponse);
  }
}
