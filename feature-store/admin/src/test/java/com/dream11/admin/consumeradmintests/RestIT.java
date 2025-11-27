package com.dream11.admin.consumeradmintests;

import static com.dream11.admin.expectedrequestresponse.CassandraEntityTestData.*;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.ChangeConsumerCountFailedTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.ChangeConsumerCountTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.GetAllConsumerMetadataTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.GetAllPopulatorMetadataTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.GetFeatureGroupTopicTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.OnboardConsumersFeatureGroupNotFoundTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.OnboardConsumersSuccessTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.PutPopulatorMetadataIncreaseTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.PutPopulatorMetadataTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.UpdatePartitionsTest;
import static com.dream11.admin.expectedrequestresponse.ConsumerAdminTestData.UpdateTopicTest;
import static com.dream11.core.util.Utils.getAndAssertResponse;
import static com.dream11.core.util.Utils.getResponse;

import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import com.dream11.core.dto.helper.Data;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.common.mapper.TypeRef;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@ExtendWith(SetUp.class)
public class RestIT {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @SneakyThrows
  public void getAllConsumerMetadataTest() {
    JsonObject expectedResponse = GetAllConsumerMetadataTest.getJsonObject("response");
    Map<String, Object> response = getResponse(GetAllConsumerMetadataTest.getJsonObject("request"), expectedResponse, new TypeRef<>() {
    });

    Data<List<ConsumerGroupMetadata>> current = objectMapper.readValue(new JsonObject(response).toString(), new TypeReference<>() {});
    Data<List<ConsumerGroupMetadata>> expected = objectMapper.readValue(expectedResponse.getJsonObject("body").toString(), new TypeReference<>() {});

    Set<ConsumerGroupMetadata> cur = new HashSet<>(current.getData());
    Set<ConsumerGroupMetadata> exp = new HashSet<>(expected.getData());
    assert cur.containsAll(exp);
  }

  @Test
  public void onboardConsumersSuccessTest() {
    JsonObject expectedResponse = OnboardConsumersSuccessTest.getJsonObject("response");
    getAndAssertResponse(OnboardConsumersSuccessTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void onboardConsumersFeatureGroupNotFoundTest() {
    JsonObject expectedResponse = OnboardConsumersFeatureGroupNotFoundTest.getJsonObject("response");
    getAndAssertResponse(OnboardConsumersFeatureGroupNotFoundTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void changeConsumerCountTest() {
    JsonObject expectedResponse = ChangeConsumerCountTest.getJsonObject("response");
    getAndAssertResponse(ChangeConsumerCountTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void changeConsumerCountFailedTest() {
    JsonObject expectedResponse = ChangeConsumerCountFailedTest.getJsonObject("response");
    getAndAssertResponse(ChangeConsumerCountFailedTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void updatePartitionsTest() {
    JsonObject expectedResponse = UpdatePartitionsTest.getJsonObject("response");
    getAndAssertResponse(UpdatePartitionsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void updateTopicTest() {
    JsonObject expectedResponse = UpdateTopicTest.getJsonObject("response");
    getAndAssertResponse(UpdateTopicTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void getFeatureGroupTopicTest() {
    JsonObject expectedResponse = GetFeatureGroupTopicTest.getJsonObject("response");
    getAndAssertResponse(GetFeatureGroupTopicTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void addAndGetAllPopulatorMetadata() {
    JsonObject expectedResponse = PutPopulatorMetadataTest.getJsonObject("response");
    getAndAssertResponse(PutPopulatorMetadataTest.getJsonObject("request"), expectedResponse);

    expectedResponse = PutPopulatorMetadataIncreaseTest.getJsonObject("response");
    getAndAssertResponse(PutPopulatorMetadataIncreaseTest.getJsonObject("request"), expectedResponse);

    expectedResponse = GetAllPopulatorMetadataTest.getJsonObject("response");
    getAndAssertResponse(GetAllPopulatorMetadataTest.getJsonObject("request"), expectedResponse);
  }
}
