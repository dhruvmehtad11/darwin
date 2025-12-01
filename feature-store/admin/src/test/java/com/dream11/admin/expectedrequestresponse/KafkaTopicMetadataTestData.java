package com.dream11.admin.expectedrequestresponse;

import com.dream11.admin.util.Utils;
import com.dream11.core.dto.kafka.KafkaTopicMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dream11.admin.constants.ExpectedDataPath.KafkaTopicMetadataTestsDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

public class KafkaTopicMetadataTestData {
  private static final JsonObject KafkaTopicMetadataTests = new JsonObject(getFileBuffer(KafkaTopicMetadataTestsDataPath));
  public static final JsonObject kafkaTopicMetadataSuccessTest = KafkaTopicMetadataTests.getJsonObject("getKafkaTopicMetadataSuccessTest");
  public static final JsonObject kafkaTopicMetadataNotFoundTest = KafkaTopicMetadataTests.getJsonObject(
      "getKafkaTopicMetadataNotFoundTest");

  public static final JsonArray kafkaTopicMetadataList = KafkaTopicMetadataTests.getJsonArray("kafkaTopicMetadata");

  public static List<NewTopic> getNewTopicListForKafkaTopicMetadataTest() {
    return kafkaTopicMetadataList.stream().map(obj -> {
      try {
        return Utils.getObjectMapper().readValue(obj.toString(), KafkaTopicMetadata.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }).map(metadata -> {
      NewTopic topic = new NewTopic(metadata.getTopicName(), metadata.getPartitions(),
          Short.parseShort(String.valueOf(metadata.getReplicationFactor())));
      topic.configs(Map.of("retention.ms", String.valueOf(metadata.getRetentionMs())));
      return topic;
    }).collect(Collectors.toList());
  }
}
