package com.dream11.admin.s3metastoreintegrationtests;

import static com.dream11.admin.expectedrequestresponse.KafkaTopicMetadataTestData.kafkaTopicMetadataList;
import static com.dream11.admin.expectedrequestresponse.KafkaTopicMetadataTestData.kafkaTopicMetadataNotFoundTest;
import static com.dream11.admin.expectedrequestresponse.KafkaTopicMetadataTestData.kafkaTopicMetadataSuccessTest;
import static com.dream11.admin.expectedrequestresponse.S3MetastoreTestData.S3backupSuccessTest;
import static com.dream11.core.util.Utils.getAndAssertResponse;
import static com.dream11.core.util.Utils.getResponse;
import static org.hamcrest.MatcherAssert.assertThat;

import com.dream11.admin.util.Utils;
import com.dream11.common.app.AppContext;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.VersionMetadata;
import com.dream11.core.dto.response.GetCassandraFeatureGroupSchemaResponse;
import com.dream11.core.dto.response.GetCassandraFeatureGroupSchemaResponseMetadataPair;
import com.dream11.core.dto.response.GetTopicResponse;
import com.dream11.core.dto.response.interfaces.GetFeatureGroupSchemaResponse;
import com.dream11.core.service.S3MetastoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.common.mapper.TypeRef;
import io.vertx.core.json.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.dream11.core.dto.consumer.ConsumerGroupMetadata;
import com.dream11.core.dto.kafka.KafkaTopicMetadata;

@Slf4j
@ExtendWith(SetUp.class)
public class RestIT {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void s3MetastoreBackupTest() throws JsonProcessingException, InterruptedException {
    JsonObject expectedResponse = S3backupSuccessTest.getJsonObject("response");
    getAndAssertResponse(S3backupSuccessTest.getJsonObject("request"), expectedResponse);

    JsonObject s3Data = S3backupSuccessTest.getJsonObject("s3Data");
    List<CassandraEntityMetadata> entityBackup =
        objectMapper.readValue(
            s3Data.getJsonArray("entities").toString(), new TypeReference<>() {
            });

    List<CassandraFeatureGroupMetadata> featureGroupBackup =
        objectMapper.readValue(
            s3Data.getJsonArray("featureGroups").toString(), new TypeReference<>() {
            });
    List<VersionMetadata> featureGroupVersionBackup =
        objectMapper.readValue(
            s3Data.getJsonArray("versions").toString(), new TypeReference<>() {
            });

    List<GetCassandraFeatureGroupSchemaResponseMetadataPair> featureGroupSchemaBackup =
        objectMapper.readValue(
            s3Data.getJsonArray("schemas").toString(), new TypeReference<>() {
            });

    List<GetTopicResponse> featureGroupTopicsBackup =
        objectMapper.readValue(
            s3Data.getJsonArray("topics").toString(), new TypeReference<>() {
            });

    List<ConsumerGroupMetadata> consumerGroupConfigBackup =
        objectMapper.readValue(
            s3Data.getJsonArray("consumerGroupConfig").toString(), new TypeReference<>() {
            });

    List<KafkaTopicMetadata> kafkaTopicMetadataBackup =
        objectMapper.readValue(
            kafkaTopicMetadataList.toString(), new TypeReference<>() {
            });

    S3MetastoreService s3MetastoreService = AppContext.getInstance(S3MetastoreService.class);
    Thread.sleep(5_000);
    assert validateAllEntities(s3MetastoreService, entityBackup);
    assert validateAllFeatureGroups(s3MetastoreService, featureGroupBackup);
    assert validateAllFeatureGroupVersions(s3MetastoreService, featureGroupVersionBackup);
    assert validateAllFeatureGroupSchemas(s3MetastoreService, featureGroupSchemaBackup);
    assert validateAllFeatureGroupTopics(s3MetastoreService, featureGroupTopicsBackup);
    assert validateAllConsumerGroupConfig(s3MetastoreService, consumerGroupConfigBackup);
    assert validateAllKafkaTopicMetadata(s3MetastoreService, kafkaTopicMetadataBackup);
    assert validateGetKafkaTopicMetadataSuccessTest();
  }

  private boolean validateAllEntities(
      S3MetastoreService s3MetastoreService, List<CassandraEntityMetadata> entityBackup) {
    List<CassandraEntityMetadata> allEntityMetadata =
        s3MetastoreService.getAllEntityMetadata().blockingGet().getEntities();
    assert Objects.deepEquals(new HashSet<>(allEntityMetadata), new HashSet<>(entityBackup));

    for (CassandraEntityMetadata entity : entityBackup) {
      CassandraEntityMetadata metadataBackup =
          s3MetastoreService.getEntityMetadata(entity.getName()).blockingGet();
      assert Objects.deepEquals(metadataBackup, entity);
    }
    return true;
  }

  private boolean validateAllFeatureGroups(
      S3MetastoreService s3MetastoreService, List<CassandraFeatureGroupMetadata> featureGroupBackup) {
    List<CassandraFeatureGroupMetadata> allFeatureGroup =
        s3MetastoreService.getAllFeatureGroupMetadata().blockingGet().getFeatureGroups();
    assert Objects.deepEquals(new HashSet<>(allFeatureGroup), new HashSet<>(featureGroupBackup));

    for (CassandraFeatureGroupMetadata featureGroup : featureGroupBackup) {
      CassandraFeatureGroupMetadata metadataBackup =
          s3MetastoreService.getFeatureGroupMetadata(featureGroup.getName(), featureGroup.getVersion()).blockingGet();
      assert Objects.deepEquals(metadataBackup, featureGroup);
    }
    return true;
  }

  private boolean validateAllFeatureGroupVersions(
      S3MetastoreService s3MetastoreService, List<VersionMetadata> featureGroupVersionBackup) {
    List<VersionMetadata> allFeatureGroupVersionMetadata =
        s3MetastoreService.getAllFeatureGroupVersionMetadata().blockingGet().getVersions();
    assert Objects.deepEquals(new HashSet<>(allFeatureGroupVersionMetadata), new HashSet<>(featureGroupVersionBackup));

    for (VersionMetadata version : featureGroupVersionBackup) {
      VersionMetadata metadataBackup =
          s3MetastoreService.getFeatureGroupVersionMetadata(version.getName()).blockingGet();
      assert Objects.deepEquals(metadataBackup, version);
    }
    return true;
  }

  private boolean validateAllFeatureGroupSchemas(
      S3MetastoreService s3MetastoreService, List<GetCassandraFeatureGroupSchemaResponseMetadataPair> schemaResponseMetadataPairs) {

    for (GetCassandraFeatureGroupSchemaResponseMetadataPair pair : schemaResponseMetadataPairs) {
      GetCassandraFeatureGroupSchemaResponse metadataBackup =
          s3MetastoreService.getFeatureGroupSchema(pair.getName(), pair.getVersion()).blockingGet();
      assert Objects.deepEquals(metadataBackup, pair.getSchema());
    }
    return true;
  }

  private boolean validateAllFeatureGroupTopics(
      S3MetastoreService s3MetastoreService, List<GetTopicResponse> topicResponses) {

    for (GetTopicResponse topicResponse : topicResponses) {
      GetTopicResponse metadataBackup =
          s3MetastoreService.getFeatureGroupTopic(topicResponse.getFeatureGroupName()).blockingGet();
      assert Objects.deepEquals(metadataBackup, topicResponse);
    }
    return true;
  }

  private boolean validateAllConsumerGroupConfig(
      S3MetastoreService s3MetastoreService, List<ConsumerGroupMetadata> consumerGroupConfigBackup) {
    List<ConsumerGroupMetadata> allConsumerGroupConfig =
        s3MetastoreService.getConsumerGroupConfig().blockingGet().getConsumerGroupMetadata();
    assert Objects.deepEquals(new HashSet<>(allConsumerGroupConfig), new HashSet<>(consumerGroupConfigBackup));
    return true;
  }

  private boolean validateAllKafkaTopicMetadata(
      S3MetastoreService s3MetastoreService, List<KafkaTopicMetadata> kafkaTopicMetadataBackup) {
    List<KafkaTopicMetadata> allKafkaTopicMetadata =
        s3MetastoreService.getKafkaTopicConfig().blockingGet().getKafkaTopicMetadata();
    assert Objects.deepEquals(new HashSet<>(allKafkaTopicMetadata), new HashSet<>(kafkaTopicMetadataBackup));
    return true;
  }

  @SneakyThrows
  public boolean validateGetKafkaTopicMetadataSuccessTest() {
    JsonObject expectedResponse = kafkaTopicMetadataSuccessTest.getJsonObject("response");
    JsonObject request = kafkaTopicMetadataSuccessTest.getJsonObject("request");

    Map<String, Object> response = getResponse(request, expectedResponse, new TypeRef<>() {
    });

    Objects.deepEquals(new JsonObject(Utils.getObjectMapper().writeValueAsString(response)),
        expectedResponse.getJsonObject("body"));

    return true;
  }
}
