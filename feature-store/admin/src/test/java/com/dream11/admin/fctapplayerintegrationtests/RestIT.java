//package com.dream11.admin.fctapplayerintegrationtests;
//
//import static com.dream11.admin.expectedrequestresponse.CassandraEntityTestData.*;
//import static com.dream11.admin.expectedrequestresponse.CassandraFeatureGroupTestData.*;
//import static com.dream11.admin.expectedrequestresponse.FctAppLayerTestData.GetFeatureGroupsCountWithAllFiltersAndWithoutSearchStringTest;
//import static com.dream11.admin.expectedrequestresponse.FctAppLayerTestData.GetFeatureGroupsCountWithOwnerFiltersAndWithoutSearchStringTest;
//import static com.dream11.admin.expectedrequestresponse.FctAppLayerTestData.GetFeatureGroupsCountWithTagFiltersAndWithoutSearchStringTest;
//import static com.dream11.admin.expectedrequestresponse.FctAppLayerTestData.GetFeatureGroupsCountWithoutFiltersAndWithSearchStringTest;
//import static com.dream11.admin.expectedrequestresponse.FctAppLayerTestData.GetFeatureGroupsCountWithoutSearchStringAndFiltersTest;
//import static com.dream11.admin.expectedrequestresponse.FctAppLayerTestData.GetFeatureGroupsFilters;
//import static com.dream11.core.util.Utils.getAndAssertResponse;
//
//import io.vertx.core.json.JsonObject;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//@Slf4j
//@ExtendWith(SetUp.class)
//public class RestIT {
//  @Test
//  public void getFeatureGroupsCountWithoutSearchStringAndFiltersTest() {
//    JsonObject expectedResponse = GetFeatureGroupsCountWithoutSearchStringAndFiltersTest.getJsonObject("response");
//    getAndAssertResponse(GetFeatureGroupsCountWithoutSearchStringAndFiltersTest.getJsonObject("request"), expectedResponse);
//  }
//
//  @Test
//  public void getFeatureGroupsCountWithOwnerFiltersAndWithoutSearchStringTest() {
//    JsonObject expectedResponse = GetFeatureGroupsCountWithOwnerFiltersAndWithoutSearchStringTest.getJsonObject("response");
//    getAndAssertResponse(GetFeatureGroupsCountWithOwnerFiltersAndWithoutSearchStringTest.getJsonObject("request"), expectedResponse);
//  }
//
//  @Test
//  public void getFeatureGroupsCountWithTagFiltersAndWithoutSearchStringTest() {
//    JsonObject expectedResponse = GetFeatureGroupsCountWithTagFiltersAndWithoutSearchStringTest.getJsonObject("response");
//    getAndAssertResponse(GetFeatureGroupsCountWithTagFiltersAndWithoutSearchStringTest.getJsonObject("request"), expectedResponse);
//  }
//
//  @Test
//  public void getFeatureGroupsCountWithAllFiltersAndWithoutSearchStringTest() {
//    JsonObject expectedResponse = GetFeatureGroupsCountWithAllFiltersAndWithoutSearchStringTest.getJsonObject("response");
//    getAndAssertResponse(GetFeatureGroupsCountWithAllFiltersAndWithoutSearchStringTest.getJsonObject("request"), expectedResponse);
//  }
//
//  @Test
//  public void getFeatureGroupsCountWithoutFiltersAndWithSearchStringTest() {
//    JsonObject expectedResponse = GetFeatureGroupsCountWithoutFiltersAndWithSearchStringTest.getJsonObject("response");
//    getAndAssertResponse(GetFeatureGroupsCountWithoutFiltersAndWithSearchStringTest.getJsonObject("request"), expectedResponse);
//  }
//
//  @Test
//  public void getFeatureGroupsFilters() {
//    JsonObject expectedResponse = GetFeatureGroupsFilters.getJsonObject("response");
//    getAndAssertResponse(GetFeatureGroupsFilters.getJsonObject("request"), expectedResponse);
//  }
//
//}
