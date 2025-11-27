package com.dream11.admin.expectedrequestresponse;

import static com.dream11.admin.constants.ExpectedDataPath.ConsumerAdminTestDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import io.vertx.core.json.JsonObject;

public class ConsumerAdminTestData {
  private static final JsonObject ConsumerAdminTests =
      new JsonObject(getFileBuffer(ConsumerAdminTestDataPath));

  public static final JsonObject GetAllConsumerMetadataTest =  ConsumerAdminTests.getJsonObject("getAllConsumerMetadataTest");
  public static final JsonObject OnboardConsumersSuccessTest =  ConsumerAdminTests.getJsonObject("onboardConsumersSuccessTest");
  public static final JsonObject OnboardConsumersFeatureGroupNotFoundTest =  ConsumerAdminTests.getJsonObject(
      "onboardConsumersFeatureGroupNotFoundTest");

  public static final JsonObject ChangeConsumerCountTest =  ConsumerAdminTests.getJsonObject(
      "changeConsumerCountTest");

  public static final JsonObject ChangeConsumerCountFailedTest =  ConsumerAdminTests.getJsonObject(
      "changeConsumerCountFailedTest");

  public static final JsonObject UpdatePartitionsTest =  ConsumerAdminTests.getJsonObject(
      "updatePartitionsTest");

  public static final JsonObject UpdateTopicTest =  ConsumerAdminTests.getJsonObject(
      "updateTopicTest");

  public static final JsonObject GetFeatureGroupTopicTest =  ConsumerAdminTests.getJsonObject(
      "getFeatureGroupTopicTest");

  public static final JsonObject PutPopulatorMetadataTest =  ConsumerAdminTests.getJsonObject(
      "putPopulatorMetadataTest");
  public static final JsonObject PutPopulatorMetadataIncreaseTest =  ConsumerAdminTests.getJsonObject(
      "putPopulatorMetadataIncreaseTest");
  public static final JsonObject GetAllPopulatorMetadataTest =  ConsumerAdminTests.getJsonObject(
      "getAllPopulatorMetadataTest");

}
