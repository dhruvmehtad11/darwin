package com.dream11.admin.crudintegrationtests;

import static com.dream11.admin.expectedrequestresponse.CassandraEntityTestData.*;
import static com.dream11.admin.expectedrequestresponse.CassandraFeatureGroupTestData.*;
import static com.dream11.admin.expectedrequestresponse.KafkaTopicMetadataTestData.kafkaTopicMetadataNotFoundTest;
import static com.dream11.core.constant.query.CassandraQuery.GET_CASSANDRA_TABLE_COLUMNS_QUERY;
import static com.dream11.core.util.Utils.getAndAssertResponse;
import static com.dream11.core.util.Utils.getResponse;
import static org.hamcrest.MatcherAssert.assertThat;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.dream11.core.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.common.mapper.TypeRef;
import io.vertx.core.json.JsonObject;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SetUp.class)
public class RestIT {

  // entity tests
  @Test
  public void successEntityCreationTest() {
    JsonObject expectedResponse = SuccessEntityCreationTest.getJsonObject("response");
    getAndAssertResponse(SuccessEntityCreationTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void invalidRequestEntityCreationTest() {
    JsonObject expectedResponse = InvalidRequestEntityCreationTest.getJsonObject("response");
    getAndAssertResponse(
        InvalidRequestEntityCreationTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void invalidRequestEntityCreationTest1() {
    JsonObject expectedResponse = InvalidRequestEntityCreationTest1.getJsonObject("response");
    getAndAssertResponse(
        InvalidRequestEntityCreationTest1.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void entityAlreadyExistsTest() {
    JsonObject expectedResponse = EntityAlreadyExistsTest.getJsonObject("response");
    getAndAssertResponse(EntityAlreadyExistsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successGetEntityTest() {
    JsonObject expectedResponse = SuccessGetEntityTest.getJsonObject("response");
    getAndAssertResponse(SuccessGetEntityTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successGetEntityMetadataTest() {
    JsonObject expectedResponse = SuccessGetEntityMetadataTest.getJsonObject("response");
    getAndAssertResponse(SuccessGetEntityMetadataTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void failedGetEntityTest() {
    JsonObject expectedResponse = FailedGetEntityTest.getJsonObject("response");
    getAndAssertResponse(FailedGetEntityTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void invalidRequestGetEntityTest() {
    JsonObject expectedResponse = InvalidRequestGetEntityTest.getJsonObject("response");
    getAndAssertResponse(InvalidRequestGetEntityTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void createEntityInvalidPrimaryKeysTest1() {
    JsonObject expectedResponse = CreateEntityInvalidSchemaTest1.getJsonObject("response");
    getAndAssertResponse(CreateEntityInvalidSchemaTest1.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void createEntityInvalidPrimaryKeysTest2() {
    JsonObject expectedResponse = CreateEntityInvalidSchemaTest2.getJsonObject("response");
    getAndAssertResponse(CreateEntityInvalidSchemaTest2.getJsonObject("request"), expectedResponse);
  }

  // feature group tests

  @Test
  public void successFeatureGroupCreationTest() {
    JsonObject expectedResponse = SuccessFeatureGroupCreationTest.getJsonObject("response");
    getAndAssertResponse(
        SuccessFeatureGroupCreationTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successFeatureGroupUpgradeTest() {
    JsonObject expectedResponse = SuccessFeatureGroupUpgradeTest.getJsonObject("response");
    getAndAssertResponse(SuccessFeatureGroupUpgradeTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void invalidRequestFeatureGroupCreationTest() {
    JsonObject expectedResponse = InvalidRequestFeatureGroupCreationTest.getJsonObject("response");
    getAndAssertResponse(
        InvalidRequestFeatureGroupCreationTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupAlreadyExistsTest() {
    JsonObject expectedResponse = FeatureGroupAlreadyExistsTest.getJsonObject("response");
    getAndAssertResponse(FeatureGroupAlreadyExistsTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupVersioningDisabledTest() {
    JsonObject expectedResponse = FeatureGroupVersioningDisabledTest.getJsonObject("response");
    getAndAssertResponse(
        FeatureGroupVersioningDisabledTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successGetFeatureGroupTest() {
    JsonObject expectedResponse = SuccessGetFeatureGroupTest.getJsonObject("response");
    getAndAssertResponse(SuccessGetFeatureGroupTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successGetFeatureGroupMetadataTest() {
    JsonObject expectedResponse = SuccessGetFeatureGroupMetadataTest.getJsonObject("response");
    getAndAssertResponse(
        SuccessGetFeatureGroupMetadataTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successGetFeatureGroupSchemaTest() {
    JsonObject expectedResponse = SuccessGetFeatureGroupSchemaTest.getJsonObject("response");
    getAndAssertResponse(
        SuccessGetFeatureGroupSchemaTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void failedGetFeatureGroupTest() {
    JsonObject expectedResponse = FailedGetFeatureGroupTest.getJsonObject("response");
    getAndAssertResponse(FailedGetFeatureGroupTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void invalidRequestGetFeatureGroupTest() {
    JsonObject expectedResponse = InvalidRequestGetFeatureGroupTest.getJsonObject("response");
    getAndAssertResponse(
        InvalidRequestGetFeatureGroupTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void successGetFeatureGroupLatestVersionTest() {
    JsonObject expectedResponse = SuccessGetFeatureGroupLatestVersionTest.getJsonObject("response");
    getAndAssertResponse(
        SuccessGetFeatureGroupLatestVersionTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void invalidRequestGetFeatureGroupLatestVersionTest() {
    JsonObject expectedResponse =
        InvalidRequestGetFeatureGroupLatestVersionTest.getJsonObject("response");
    getAndAssertResponse(
        InvalidRequestGetFeatureGroupLatestVersionTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void entityNotFoundFeatureGroupCreationTest() {
    JsonObject expectedResponse = EntityNotFoundFeatureGroupCreationTest.getJsonObject("response");
    getAndAssertResponse(
        EntityNotFoundFeatureGroupCreationTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupCreationInvalidSchemaTest() {
    JsonObject expectedResponse = FeatureGroupCreationInvalidSchemaTest.getJsonObject("response");
    getAndAssertResponse(
        FeatureGroupCreationInvalidSchemaTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void featureGroupCreationInvalidSchemaTest1() {
    JsonObject expectedResponse = FeatureGroupCreationInvalidSchemaTest1.getJsonObject("response");
    getAndAssertResponse(
        FeatureGroupCreationInvalidSchemaTest1.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void updateTenantTest() {
    JsonObject expectedResponse = UpdateFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(UpdateFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);
    Exception e = null;
    try {
      Thread.sleep(2_000);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    String testContainerHost = System.getProperty("cassandra.tenant." + "t1" + ".host");
    String testContainerPort = System.getProperty("cassandra.tenant." + "t1" + ".port");

    Set<String> verifyColumns =
        new HashSet<>(
            (List<String>) UpdateFeatureGroupTenantTest.getJsonArray("tableColumns").getList());
    Set<String> addedColumns = new HashSet<>();

    try (CqlSession session =
        CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .addContactPoint(
                new InetSocketAddress(testContainerHost, Integer.parseInt(testContainerPort)))
            .build()) {
      ResultSet resultSet = session.execute(GET_CASSANDRA_TABLE_COLUMNS_QUERY, "ofs", "t15");
      resultSet.forEach(r -> addedColumns.add(r.getString(0)));
    } catch (Exception exception) {
      e = exception;
    }
    // assert no exception
    assert e == null;

    assert !addedColumns.isEmpty();
    // take intersection of both sets
    addedColumns.removeAll(verifyColumns);
    assert addedColumns.isEmpty();

    // move it back to a cluster with existing table and column
    expectedResponse = MoveBackFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(MoveBackFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void putFeatureGroupRunDataTest() {
    JsonObject expectedResponse = PutFeatureGroupRunDataTest.getJsonObject("response");
    getAndAssertResponse(PutFeatureGroupRunDataTest.getJsonObject("request"), expectedResponse);
  }
  @Test
  public void putFeatureGroupRunDataLongCountTest() {
    JsonObject expectedResponse = PutFeatureGroupRunDataLongCountTest.getJsonObject("response");
    getAndAssertResponse(PutFeatureGroupRunDataLongCountTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void getFeatureGroupRunDataTest() {
    JsonObject expectedResponse = GetFeatureGroupRunDataTest.getJsonObject("response");
    getAndAssertResponse(GetFeatureGroupRunDataTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void getKafkaTopicMetadataNotFoundTest() {
    JsonObject expectedResponse = kafkaTopicMetadataNotFoundTest.getJsonObject("response");
    getAndAssertResponse(kafkaTopicMetadataNotFoundTest.getJsonObject("request"), expectedResponse);
  }

  @Test
  public void updateAllTenantTest() throws JsonProcessingException {
    JsonObject expectedResponse = UpdateFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(UpdateFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);
    Exception e = null;
    try {
      Thread.sleep(2_000);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    String testContainerHost = System.getProperty("cassandra.tenant." + "t1" + ".host");
    String testContainerPort = System.getProperty("cassandra.tenant." + "t1" + ".port");

    Set<String> verifyColumns =
        new HashSet<>(
            (List<String>) UpdateFeatureGroupTenantTest.getJsonArray("tableColumns").getList());
    Set<String> addedColumns = new HashSet<>();

    try (CqlSession session =
        CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .addContactPoint(
                new InetSocketAddress(testContainerHost, Integer.parseInt(testContainerPort)))
            .build()) {
      ResultSet resultSet = session.execute(GET_CASSANDRA_TABLE_COLUMNS_QUERY, "ofs", "t15");
      resultSet.forEach(r -> addedColumns.add(r.getString(0)));
    } catch (Exception exception) {
      e = exception;
    }
    // assert no exception
    assert e == null;

    assert !addedColumns.isEmpty();
    // take intersection of both sets
    addedColumns.removeAll(verifyColumns);
    assert addedColumns.isEmpty();

    // move it back to a cluster with existing table and column
    expectedResponse = MoveBackFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(MoveBackFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);

    // update tenant again
    expectedResponse = UpdateFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(UpdateFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);
    try {
      Thread.sleep(2_000);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }

    // update using update for all api
    expectedResponse = UpdateAllReaderFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(
        UpdateAllReaderFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);

    expectedResponse = UpdateAllWriterFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(
        UpdateAllWriterFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);

    expectedResponse = UpdateAllConsumerFeatureGroupTenantTest.getJsonObject("response");
    getAndAssertResponse(
        UpdateAllConsumerFeatureGroupTenantTest.getJsonObject("request"), expectedResponse);

    expectedResponse = VerifyAllUpdateFeatureGroupTenantTest.getJsonObject("response");
    Map<String, Object> response =
        getResponse(
            VerifyAllUpdateFeatureGroupTenantTest.getJsonObject("request"),
            expectedResponse,
            new TypeRef<>() {});

    assertThat(
        "tenant config is same",
        new JsonObject(Utils.objectMapper.writeValueAsString(response)).getJsonObject("tenantConfig"),
        Matchers.equalTo(expectedResponse.getJsonObject("body").getJsonObject("tenantConfig")));
  }
}
