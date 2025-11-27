package com.dream11.consumer.unittests;

import com.dream11.common.app.AppContext;
import com.dream11.common.guice.DefaultModule;
import com.dream11.common.util.ContextUtils;
import com.dream11.config.constant.Constants;
import com.dream11.consumer.MockServers.OfsMock;
import com.dream11.consumer.service.ConsumerProcessor;
import com.dream11.consumer.util.KafkaMessageUtils;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.core.dto.featuredata.CassandraFeatureData;
import com.dream11.core.dto.request.WriteCassandraFeaturesRequest;
import com.dream11.core.dto.request.legacystack.LegacyWriteFeaturesKafkaMessage;
import com.dream11.webclient.reactivex.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import io.reactivex.Completable;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumer;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecords;
import io.vertx.reactivex.kafka.client.producer.KafkaProducer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utils.CommonUtils;

import static com.dream11.core.constant.Constants.SDK_V2_HEADER_VALUE;
import static com.dream11.core.constant.Constants.SDK_VERSION_HEADER_NAME;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
class ConsumerProcessorTest {
  public static MockConsumer<String, String> mockConsumer;
  public static KafkaProducer<String, String> mockProducer;
  public static ConsumerProcessor consumerProcessor;

  public static ObjectMapper objectMapper = new ObjectMapper();
  public WireMockServer server;
  public static Vertx vertx;
  public AppContext appContext;

  @BeforeEach
  public void beforeAll() throws Exception {

    System.setProperty("app.environment", "test");
    System.setProperty("ENV", "test");
    System.setProperty("TEAM_SUFFIX", "");
    System.setProperty("VPC_SUFFIX", "");
    initWireMock();
    OfsMock.mockServer(server);
    server.start();

    vertx = Vertx.vertx(new VertxOptions()
            .setMetricsOptions(new MicrometerMetricsOptions()
                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                .setEnabled(true))
        .setEventLoopPoolSize(1).setPreferNativeTransport(true));
    mockConsumer = new MockConsumer<>(OffsetResetStrategy.LATEST);
    mockProducer = Mockito.mock(KafkaProducer.class);
    Mockito.when(mockProducer.rxWrite(any())).thenReturn(Completable.complete());

    DefaultModule defaultModule = new DefaultModule(vertx);

    AbstractModule testModule =
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(ApplicationConfig.class).toProvider(ConfigManager.class);
            bind(WebClient.class).toInstance(WebClient.create(vertx));
            bind(new TypeLiteral<KafkaConsumer<String, String>>() {})
                .toInstance(
                    KafkaConsumer.newInstance(
                        io.vertx.kafka.client.consumer.KafkaConsumer.create(
                            vertx.getDelegate(), mockConsumer)));
            bind(new TypeLiteral<KafkaProducer<String, String>>() {})
                .toInstance(mockProducer);

            bind(AdminClient.class)
                .toProvider(() -> ContextUtils.getInstance(KafkaAdminClient.class));
            bind(StatsDClient.class).toProvider(() -> new NonBlockingStatsDClientBuilder()
                .prefix(Constants.SERVICE_NAME)
                .hostname(System.getenv().getOrDefault("DD_AGENT_HOST", "localhost"))
                .port(8125).build());
          }
        };

    AppContext.initialize(List.of(testModule, defaultModule));
    consumerProcessor = AppContext.getInstance(ConsumerProcessor.class);
    consumerProcessor.setConsumerId(UUID.randomUUID().toString());
  }

  // dummy message
  // {"Features":"{\"userid\":502,\"id\":15,\"name\":\"A7Hxm8JX\",\"col1\":5161,\"col2\":\"Tp2FL\",\"col3\":36,\"col6\":false}",
  // "EntityName":"fg_test_entity","Entities":"userid,id,name","FeatureGroup":"fg_test"}
  //                                ^^^^ not list as expected by legacy write api

  @SneakyThrows
  public static String getTestMessage() {
    LegacyWriteFeaturesKafkaMessage request =
        LegacyWriteFeaturesKafkaMessage.builder()
            .entityName("t5")
            .entityColumns("p_col1,p_col2")
            .featureGroupName("f15")
            .writeFeatures(objectMapper.writeValueAsString(Map.of("p_col1", "1", "p_col2", "2", "col1", "1", "col2", "name")))
            .build();
    return AppContext.getInstance(ObjectMapper.class).writeValueAsString(request);
  }

  @SneakyThrows
  public static WriteCassandraFeaturesRequest getTestMessageV2() {
    WriteCassandraFeaturesRequest request =
        WriteCassandraFeaturesRequest.builder()
            .featureGroupName("f15")
            .features(CassandraFeatureData.builder()
                .names(List.of("p_col1", "p_col2", "col1", "col2"))
                .values(List.of(List.of("1", "2", "1", "name")))
                .build())
            .build();
    return request;
  }


  @SneakyThrows
  public static String getFailTestMessage() {
    LegacyWriteFeaturesKafkaMessage request =
        LegacyWriteFeaturesKafkaMessage.builder()
            .entityName("t5")
            .entityColumns("p_col1,p_col2")
            .featureGroupName("f15")
            .writeFeatures(objectMapper.writeValueAsString(Map.of("p_col1", "1", "p_col2", "2", "col1", "1a", "col2", "name")))
            .build();
    return AppContext.getInstance(ObjectMapper.class).writeValueAsString(request);
  }

  @SneakyThrows
  public static WriteCassandraFeaturesRequest getFailTestMessageV2() {
    WriteCassandraFeaturesRequest request =
        WriteCassandraFeaturesRequest.builder()
            .featureGroupName("f15")
            .features(CassandraFeatureData.builder()
                .names(List.of("p_col1", "p_col2", "col1", "col2"))
                .values(List.of(List.of("1", "2", "1a", "name")))
                .build())
            .build();
    return request;
  }

  @SneakyThrows
  public static WriteCassandraFeaturesRequest getNotFoundTestMessageV2() {
    WriteCassandraFeaturesRequest request =
        WriteCassandraFeaturesRequest.builder()
            .featureGroupName("f16")
            .features(CassandraFeatureData.builder()
                .names(List.of("p_col1", "p_col2", "col1", "col2"))
                .values(List.of(List.of("1", "2", "1a", "name")))
                .build())
            .build();
    return request;
  }

  public void initWireMock() {
    int port = CommonUtils.getFreePort();
    System.setProperty("wiremock.port", String.valueOf(port));
    this.server =
        new WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(port)
                .preserveHostHeader(false)
                .extensions(new ResponseTemplateTransformer(false)));
  }

  public static List<String> getTestMessages(int numMessages) {
    List<String> li = new ArrayList<>();
    for (int i = 0; i < numMessages; i++) {
      li.add(getTestMessage());
    }
    return li;
  }

  private List<ConsumerRecord<String, String>> getTestBatchMessages() {
    ConsumerRecord<String, String> successRecord =
        new ConsumerRecord<String, String>("test", 0, 0, null, getTestMessage());
    ConsumerRecord<String, String> invalidRecord =
            new ConsumerRecord<String, String>("test", 0, 1, null, getTestMessage().substring(4));
    ConsumerRecord<String, String> failedRecord =
        new ConsumerRecord<String, String>("test", 0, 2, null, getFailTestMessage());
    return List.of(successRecord, invalidRecord, failedRecord);
  }

  @SneakyThrows
  private List<ConsumerRecord<String, String>> getTestBatchMessagesV2() {
    WriteCassandraFeaturesRequest success = getTestMessageV2();
    ConsumerRecord<String, String> successRecord =
        new ConsumerRecord<>(
            "test",
            0,
            0,
            -1L,
            TimestampType.NO_TIMESTAMP_TYPE,
            -1L,
            -1,
            -1,
            null,
            createFeatureDataMessages(objectMapper, success.getFeatures()).get(0),
            new RecordHeaders()
                .add(SDK_VERSION_HEADER_NAME, SDK_V2_HEADER_VALUE.getBytes(StandardCharsets.UTF_8))
                .add(
                    "feature-group-name",
                    success.getFeatureGroupName().getBytes(StandardCharsets.UTF_8)));

    ConsumerRecord<String, String> invalidRecord =
        new ConsumerRecord<>(
            "test",
            0,
            0,
            -1L,
            TimestampType.NO_TIMESTAMP_TYPE,
            -1L,
            -1,
            -1,
            null,
            createFeatureDataMessages(objectMapper, success.getFeatures()).get(0).substring(4),
            new RecordHeaders()
                .add(SDK_VERSION_HEADER_NAME, SDK_V2_HEADER_VALUE.getBytes(StandardCharsets.UTF_8))
                .add(
                    "feature-group-name",
                    success.getFeatureGroupName().getBytes(StandardCharsets.UTF_8)));

    WriteCassandraFeaturesRequest failed = getFailTestMessageV2();
    ConsumerRecord<String, String> failedRecord;
    failedRecord =
        new ConsumerRecord<>(
            "test",
            0,
            0,
            -1L,
            TimestampType.NO_TIMESTAMP_TYPE,
            -1L,
            -1,
            -1,
            null,
            createFeatureDataMessages(objectMapper, failed.getFeatures()).get(0),
            new RecordHeaders()
                .add(SDK_VERSION_HEADER_NAME, SDK_V2_HEADER_VALUE.getBytes(StandardCharsets.UTF_8))
                .add(
                    "feature-group-name",
                    failed.getFeatureGroupName().getBytes(StandardCharsets.UTF_8)));

    WriteCassandraFeaturesRequest failedButSerializable = getNotFoundTestMessageV2();
    ConsumerRecord<String, String> failedButSerializableRecord;
    failedButSerializableRecord =
        new ConsumerRecord<>(
            "test",
            0,
            0,
            -1L,
            TimestampType.NO_TIMESTAMP_TYPE,
            -1L,
            -1,
            -1,
            null,
            createFeatureDataMessages(objectMapper, failedButSerializable.getFeatures()).get(0),
            new RecordHeaders()
                .add(SDK_VERSION_HEADER_NAME, SDK_V2_HEADER_VALUE.getBytes(StandardCharsets.UTF_8))
                .add(
                    "feature-group-name",
                    failedButSerializable.getFeatureGroupName().getBytes(StandardCharsets.UTF_8)));
    return List.of(successRecord, invalidRecord, failedRecord, failedButSerializableRecord);
  }

  @SneakyThrows
  @Test
  void testProcessor() {
    List<ConsumerRecord<String, String>> li = new ArrayList<>(getTestBatchMessages());
    li.addAll(getTestBatchMessagesV2());

    TopicPartition topicPartition = new TopicPartition("test", 0);
    Map<TopicPartition, List<ConsumerRecord<String, String>>> records = Map.of(topicPartition, li);
    ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(records);

    KafkaConsumerRecords<String, String> vertxConsumerRecords =
        new KafkaConsumerRecords<>(
            new io.vertx.kafka.client.consumer.impl.KafkaConsumerRecordsImpl<>(consumerRecords));

    consumerProcessor
        .batchHandler(vertxConsumerRecords)
        .delay(5_000L, TimeUnit.MILLISECONDS)
        .blockingAwait();

    // 2 success record to ofs (1 v2, 1 legacy)
    // 2 invalid (1 v2, 1 legacy)
    // 2 failed (1 v2, 1 legacy)
    // 1 not found from v2
    verify(mockProducer, times(5)).rxWrite(any());
  }

  public static List<String> createFeatureDataMessages(ObjectMapper objectMapper, CassandraFeatureData featureData)
      throws Throwable {
    List<String> messageList = new ArrayList<>();
    for (int i = 0; i < featureData.getValues().size(); i++) {
      Map<String, Object> map = new HashMap<>();
      for (int j = 0; j < featureData.getNames().size(); j++) {
        String featureName = featureData.getNames().get(j);
        map.put(featureName, featureData.getValues().get(i).get(j));
      }
      messageList.add(objectMapper.writeValueAsString(map));
    }
    return messageList;
  }
}
