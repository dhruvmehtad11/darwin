package com.dream11.core.util;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsOptions;
import org.apache.kafka.clients.admin.NewTopic;
import org.hamcrest.Matchers;

@Slf4j
public class Utils {
  public static final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  public static String getFileBuffer(String path) {
    return IOUtils.toString(new FileReader(path));
  }

  public static <T> T getResponse(JsonObject req, JsonObject res, TypeRef<T> typeRef) {
    RequestSpecification request = given()
        .header("Content-Type", "application/json");
    if (req.containsKey("body")) {
      request.body(req.getJsonObject("body").toString());
    }
    if (req.containsKey("headers")) {
      request.headers(req.getJsonObject("headers").stream().sequential()
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString())));
    }
    if (req.containsKey("params")) {
      request.queryParams(req.getJsonObject("params").stream().sequential()
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString())));
    }

    Response result;
    if (Objects.equals(req.getString("method"), "POST")) {
      result = request.post(req.getString("endpoint"));
    } else if (Objects.equals(req.getString("method"), "GET")) {
      result = request.get(req.getString("endpoint"));
    } else if (Objects.equals(req.getString("method"), "PUT")) {
      result = request.put(req.getString("endpoint"));
    } else {
      throw new RuntimeException("Invalid HTTP method provided in request.");
    }

    if(res.getValue("status")!=null && res.getValue("status") instanceof Integer){
      return result.then().assertThat().statusCode(res.getInteger("status")).extract().as(typeRef);
    }

    return result.then().assertThat().statusCode(res.getInteger("statusCode")).extract().as(typeRef);
  }

  @SneakyThrows
  public static void getAndAssertResponse(JsonObject req, JsonObject expectedResponse) {
    Map<String, Object> response = getResponse(req, expectedResponse, new TypeRef<>() {
    });

    assertThat(
        "Assert that response is correct",
        new JsonObject(objectMapper.writeValueAsString(response)),
        Matchers.equalTo(expectedResponse.getJsonObject("body")));
  }

  @SneakyThrows
  public static void getAndAssertResponseWithMatchingData(JsonObject req, JsonObject expectedResponse) {
    Map<String, Object> response = getResponse(req, expectedResponse, new TypeRef<>() {
    });

    assertThat(
            "Assert that response is correct",
            new JsonObject(objectMapper.writeValueAsString(response)).getJsonArray("data"),
            Matchers.equalTo(expectedResponse.getJsonArray("data")));
  }

  // just for metastore validations so the ordering is not required
  @SneakyThrows
  public static void getAndAssertCompressedResponse(JsonObject req, JsonObject expectedResponse) {
    Map<String, String> response = getResponse(req, expectedResponse, new TypeRef<>() {});

    JsonObject decompressedResponse =
        new JsonObject(CompressionUtils.decompressNonBlocking(Vertx.vertx(), response.get("data")).blockingGet());

    assertThat(
        "Assert that decompressed response is correct",
        decompressedResponse,
        jsonEqualTo(expectedResponse.getJsonObject("decompressedBody")));
  }

  private static org.hamcrest.Matcher<JsonObject> jsonEqualTo(JsonObject expected) {
    return new org.hamcrest.TypeSafeMatcher<JsonObject>() {

      @Override
      protected boolean matchesSafely(JsonObject actual) {
        Map<String, Object> actualMap = actual.getMap();
        Map<String, Object> expectedMap = expected.getMap();
        return match(actualMap, expectedMap);
      }

      @Override
      public void describeTo(org.hamcrest.Description description) {
        description.appendText("JSON object equal to ").appendValue(expected.toString());
      }

      public boolean match(Map<String, Object> actualMap, Map<String, Object> expectedMap) {
        if (actualMap.size() != expectedMap.size()) {
          return false;
        }
        Set<String> actualKeySet = actualMap.keySet();
        Set<String> expectedKeySet = expectedMap.keySet();

        if (!actualKeySet.equals(expectedKeySet)) {
          return false;
        }

        for (String key : actualKeySet) {
          Object actualValue = actualMap.get(key);
          Object expectedValue = expectedMap.get(key);
          if (!match(actualValue, expectedValue)) {
            return false;
          }
        }
        return true;
      }

      public boolean match(List<Object> actualList, List<Object> expectedList) {
        if (actualList.size() != expectedList.size()) {
          return false;
        }
        List<Object> actualCopy = new ArrayList<>(actualList);
        List<Object> expectedCopy = new ArrayList<>(expectedList);

        for (Object expected : expectedCopy) {
          boolean matched = false;
          Iterator<Object> iterator = actualCopy.iterator();
          while (iterator.hasNext()) {
            Object actual = iterator.next();
            if (match(actual, expected)) {
              iterator.remove();
              matched = true;
              break;
            }
          }
          if (!matched) {
            return false;
          }
        }
        return true;
      }

      private boolean match(Object actual, Object expected) {
        if (actual == null || expected == null) {
          return Objects.equals(actual, expected);
        }
        if (actual instanceof Map && expected instanceof Map) {
          return match((Map<String, Object>) actual, (Map<String, Object>) expected);
        }
        if (actual instanceof List && expected instanceof List) {
          return match((List<Object>) actual, (List<Object>) expected);
        }
        return actual.equals(expected);
      }
    };
  }

  public static void createKafkaTopic(String bootstrapServer, NewTopic newTopic)
      throws ExecutionException, InterruptedException {
    String topic = newTopic.name();
    // Configure the AdminClient
    Properties adminProperties = new Properties();
    adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, List.of(bootstrapServer));

    try (AdminClient adminClient = AdminClient.create(adminProperties)) {
      // Create the topic
      adminClient.createTopics(Collections.singletonList(newTopic)).all().get();

      System.out.println("Topic '" + topic + "' created successfully.");
    } catch (Exception e) {
      System.err.println("Error creating topic: " + e.getMessage());
      throw e;
    }
  }

  public static void deleteTopic(String bootstrapServer, String topic)
      throws ExecutionException, InterruptedException {
    Properties adminProperties = new Properties();
    adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    //    adminProperties.put("kafka.bootstrap.servers", bootstrapServer);
    try (AdminClient adminClient = AdminClient.create(adminProperties)) {
      // Delete the topic
      adminClient
          .deleteTopics(Collections.singletonList(topic), new DeleteTopicsOptions().timeoutMs(5000))
          .all()
          .get();
      System.out.println("Topic '" + topic + "' deleted successfully.");
    } catch (ExecutionException | InterruptedException e) {
      // Ignore the exception if the topic doesn't exist
      if (!(e.getCause()
          instanceof org.apache.kafka.common.errors.UnknownTopicOrPartitionException)) {
        throw e;
      }
    }
  }
}
