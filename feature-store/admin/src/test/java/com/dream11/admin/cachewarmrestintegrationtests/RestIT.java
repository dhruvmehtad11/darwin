package com.dream11.admin.cachewarmrestintegrationtests;

import static com.dream11.admin.expectedrequestresponse.CassandraSearchTestData.GetAllEntitiesTest;
import static com.dream11.admin.expectedrequestresponse.CassandraSearchTestData.GetAllFeatureGroupsLatestVersionTest;
import static com.dream11.admin.expectedrequestresponse.CassandraSearchTestData.GetAllFeatureGroupsTest;
import static com.dream11.admin.expectedrequestresponse.CassandraSearchTestData.GetAllUpdatedEntitiesTest;
import static com.dream11.admin.expectedrequestresponse.CassandraSearchTestData.GetAllUpdatedFeatureGroupsLatestVersionTest;
import static com.dream11.admin.expectedrequestresponse.CassandraSearchTestData.GetAllUpdatedFeatureGroupsTest;
import static com.dream11.core.util.Utils.getAndAssertCompressedResponse;

import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SetUp.class)
public class RestIT {
  // metastore tests
  @Test
  public void GetAllEntitiesTest() {
    JsonObject expectedResponse = GetAllEntitiesTest.getJsonObject("response");
    getAndAssertCompressedResponse(GetAllEntitiesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void GetAllUpdatedEntitiesTest() {
    JsonObject expectedResponse = GetAllUpdatedEntitiesTest.getJsonObject("response");
    getAndAssertCompressedResponse(GetAllUpdatedEntitiesTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void GetAllFeatureGroupsTest() {
    JsonObject expectedResponse = GetAllFeatureGroupsTest.getJsonObject("response");
    getAndAssertCompressedResponse(GetAllFeatureGroupsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void GetAllUpdatedFeatureGroupsTest() {
    JsonObject expectedResponse = GetAllUpdatedFeatureGroupsTest.getJsonObject("response");
    getAndAssertCompressedResponse(GetAllUpdatedFeatureGroupsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void GetAllFeatureGroupsLatestVersionTest() {
    JsonObject expectedResponse = GetAllFeatureGroupsLatestVersionTest.getJsonObject("response");
    getAndAssertCompressedResponse(GetAllFeatureGroupsLatestVersionTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void GetAllUpdatedFeatureGroupsLatestVersionTest() {
    JsonObject expectedResponse = GetAllUpdatedFeatureGroupsLatestVersionTest.getJsonObject("response");
    getAndAssertCompressedResponse(GetAllUpdatedFeatureGroupsLatestVersionTest.getJsonObject("request"), expectedResponse);
  }

}
