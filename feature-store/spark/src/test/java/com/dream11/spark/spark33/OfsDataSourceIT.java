package com.dream11.spark.spark33;

import static com.dream11.spark.testdata.WriterTestData.successWriteTest;

import com.dream11.spark.MockServers.OfsMock;
import com.dream11.spark.utils.RunDataUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class OfsDataSourceIT {
  private static SparkSession spark;
  private static WireMockServer server;
  private static AdminClient kafkaAdminClient;
  private static KafkaConsumer<String, String> consumer;
  private static GenericContainer kafkaContainer;

  @BeforeAll
  @SneakyThrows
  public static void setup() {
    List<String> jars =
        Files.walk(FileSystems.getDefault().getPath("target/"))
            .map(Path::toString)
            .filter(r -> r.matches("^.*/darwin-ofs-v2-spark-(?!.*-tests\\.jar$).*\\.jar"))
            .collect(Collectors.toList());
    assert jars.size() == 1;

    spark =
        org.apache.spark.sql.SparkSession.builder()
            .appName("ofs writer test")
            .master("local[*]")
            .config("spark.jars", jars.get(0))
            .getOrCreate();

    System.setProperty("app.environment", "test");
    System.setProperty("ENV", "test");
    System.setProperty("TEAM_SUFFIX", "");
    System.setProperty("VPC_SUFFIX", "");

    kafkaContainer =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1")).withReuse(false);
    kafkaContainer.start();

    String kafkaHost = ((KafkaContainer) kafkaContainer).getBootstrapServers();
    System.setProperty("ofs.kafka.host", kafkaHost);

    initWireMock();
    OfsMock.mockServer(server);
    server.start();
    Properties properties = new Properties();
    properties.put("bootstrap.servers", kafkaHost);
    kafkaAdminClient = KafkaAdminClient.create(properties);
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
    properties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
    consumer = new KafkaConsumer<>(properties);
  }

  @SneakyThrows
  public static void initWireMock() {
    int port;
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      port = serverSocket.getLocalPort();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    System.setProperty("wiremock.port", String.valueOf(port));
    System.setProperty("ofs.admin.host", "localhost:" + port);
    server =
        new WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(port)
                .preserveHostHeader(false)
                .extensions(new ResponseTemplateTransformer(false)));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f15", 1, (short) 1)));

    Dataset<Row> df = createTestDf();

    String runId = RunDataUtils.createRunId();
    df.write()
        .format("ofs")
        .option("feature-group-name", "f15")
        .option("run-id", runId)
        .mode("append")
        .save();

    Thread.sleep(2_000);
    assert getTopicOffsets("f15", 0) == 1L;
    TopicPartition topicPartition = new TopicPartition("f15", 0);
    consumer.assign(Collections.singleton(topicPartition));
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
    assert records.count() == 1;
    assert records.records(topicPartition).size() == 1;
    Map<String, String> headers = new HashMap<>();
    records
        .records(topicPartition)
        .get(0)
        .headers()
        .iterator()
        .forEachRemaining(h -> headers.put(h.key(), new String(h.value())));
    assert headers.containsKey("sdk-version");
    assert Objects.equals(headers.get("sdk-version"), "v2");
    assert headers.containsKey("feature-group-name");
    assert Objects.equals(headers.get("feature-group-name"), "f15");
    assert headers.containsKey("run-id");
    assert Objects.equals(headers.get("run-id"), runId);
    assert JsonParser.parseString(records.records(topicPartition).get(0).value())
        .getAsJsonObject()
        .equals(successWriteTest.getAsJsonObject("message"));
    consumer.unsubscribe();
    kafkaAdminClient.deleteTopics(Collections.singleton(topicPartition.topic()));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithVersionTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f16", 1, (short) 1)));

    Dataset<Row> df = createTestDf();

    df.write()
        .format("ofs")
        .option("feature-group-name", "f16")
        .option("feature-group-version", "v2")
        .mode("append")
        .save();

    Thread.sleep(2_000);
    assert getTopicOffsets("f16", 0) == 1L;
    TopicPartition topicPartition = new TopicPartition("f16", 0);
    consumer.assign(Collections.singleton(topicPartition));
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
    assert records.count() == 1;
    assert records.records(topicPartition).size() == 1;
    Map<String, String> headers = new HashMap<>();
    records
        .records(topicPartition)
        .get(0)
        .headers()
        .iterator()
        .forEachRemaining(h -> headers.put(h.key(), new String(h.value())));
    assert headers.containsKey("sdk-version");
    assert Objects.equals(headers.get("sdk-version"), "v2");
    assert headers.containsKey("feature-group-name");
    assert Objects.equals(headers.get("feature-group-name"), "f16");
    assert headers.containsKey("feature-group-version");
    assert Objects.equals(headers.get("feature-group-version"), "v2");
    assert JsonParser.parseString(records.records(topicPartition).get(0).value())
        .getAsJsonObject()
        .equals(successWriteTest.getAsJsonObject("message"));
    consumer.unsubscribe();
    kafkaAdminClient.deleteTopics(Collections.singleton(topicPartition.topic()));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithLegacySchemaTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f20", 1, (short) 1)));

    Dataset<Row> df = createTestDf();

    df.write().format("ofs").option("feature-group-name", "f20").mode("append").save();

    Thread.sleep(2_000);
    assert getTopicOffsets("f20", 0) == 1L;
    TopicPartition topicPartition = new TopicPartition("f20", 0);
    consumer.assign(Collections.singleton(topicPartition));
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
    assert records.count() == 1;
    assert records.records(topicPartition).size() == 1;
    Map<String, String> headers = new HashMap<>();
    records
        .records(topicPartition)
        .get(0)
        .headers()
        .iterator()
        .forEachRemaining(h -> headers.put(h.key(), new String(h.value())));
    assert headers.containsKey("sdk-version");
    assert Objects.equals(headers.get("sdk-version"), "v2");
    assert headers.containsKey("feature-group-name");
    assert Objects.equals(headers.get("feature-group-name"), "f20");
    assert JsonParser.parseString(records.records(topicPartition).get(0).value())
        .getAsJsonObject()
        .equals(successWriteTest.getAsJsonObject("message"));
    consumer.unsubscribe();
    kafkaAdminClient.deleteTopics(Collections.singleton(topicPartition.topic()));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithNullColumnTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f19", 1, (short) 1)));

    Dataset<Row> df = createTestDfWithNullableColumns();
    Exception exception = null;
    try {
      df.write()
          .format("ofs")
          .option("feature-group-name", "f19")
          .option("feature-group-version", "v2")
          .mode("append")
          .save();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      exception = e;
    }
    assert exception != null;
    assert exception
        .getMessage()
        .contains("Cannot write incompatible data to table 'f19':\n"
            + "- Cannot write nullable values to non-null column 'p_col1'");
    assert getTopicOffsets("f19", 0) == 0L;
    kafkaAdminClient.deleteTopics(Collections.singleton("f19"));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithExtraColumnsTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f22", 1, (short) 1)));

    Dataset<Row> df = createTestDfWithExtraColumns();
    Exception exception = null;
    try {
      df.write()
          .format("ofs")
          .option("feature-group-name", "f22")
          .option("feature-group-version", "v2")
          .mode("append")
          .save();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      exception = e;
    }

    assert exception != null;
    assert exception.getClass() == AnalysisException.class;
    assert exception
        .getMessage()
        .contains(
            "\nCannot write to 'f22', too many data columns:\n"
                + "Table columns: 'p_col1', 'p_col2', 'col1', 'col2', 'col3', 'col4', 'col5', 'col6', 'col7', 'col8', 'col9', 'col10'\n"
                + "Data columns: 'p_col1', 'p_col2', 'col1', 'col2', 'col3', 'col4', 'col5', 'col6', 'col7', 'col8', 'col9', 'col10', 'col11'\n");
    assert getTopicOffsets("f22", 0) == 0L;
    kafkaAdminClient.deleteTopics(Collections.singleton("f22"));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithMissingColumnsTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f23", 1, (short) 1)));

    Dataset<Row> df = createTestDfWithMissingColumns();
    Exception exception = null;
    try {
      df.write()
          .format("ofs")
          .option("feature-group-name", "f23")
          .option("feature-group-version", "v2")
          .mode("append")
          .save();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      exception = e;
    }
    assert exception != null;
    assert exception.getClass() == AnalysisException.class;
    assert Objects.equals(
        exception.getMessage(),
        "Cannot write incompatible data to table 'f23':\n"
            + "- Cannot find data for output column 'p_col2'");
    assert getTopicOffsets("f23", 0) == 0L;
    kafkaAdminClient.deleteTopics(Collections.singleton("f23"));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithDataTypeMismatchTest() {
    kafkaAdminClient.createTopics(Collections.singleton(new NewTopic("f24", 1, (short) 1)));

    Dataset<Row> df = createTestDfWithDataTypeMismatch();
    Exception exception = null;
    try {
      df.write()
          .format("ofs")
          .option("feature-group-name", "f24")
          .option("feature-group-version", "v2")
          .mode("append")
          .save();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      exception = e;
    }
    assert exception != null;
    assert exception.getClass() == AnalysisException.class;
    assert Objects.equals(
        exception.getMessage(),
        "Cannot write incompatible data to table 'f24':\n"
            + "- Cannot safely cast 'p_col1': string to int\n"
            + "- Cannot write nullable values to non-null column 'col1'");
    assert getTopicOffsets("f24", 0) == 0L;
    kafkaAdminClient.deleteTopics(Collections.singleton("f24"));
  }

  @Test
  @SneakyThrows
  public void ofsDataSourceWithFeatureGroupNotFoundTest() {
    Dataset<Row> df = createTestDfWithDataTypeMismatch();
    Exception exception = null;
    try {
      df.write().format("ofs").option("feature-group-name", "f21").mode("append").save();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      exception = e;
    }
    assert exception != null;
    assert Objects.equals(
        exception.getMessage(),
        "error fetching schema from admin code: FEATURE-GROUP-NOT-FOUND-EXCEPTION message: feature group not found");
  }

  private static Dataset<Row> createTestDfWithMissingColumns() {
    return spark.createDataFrame(
        Collections.singletonList(RowFactory.create(getTestDataWithMissingColumns().values().toArray())),
        getTestSchemaWithMissingPrimaryKeys());
  }

  private static Dataset<Row> createTestDfWithExtraColumns() {
    return spark.createDataFrame(
        Collections.singletonList(RowFactory.create(getTestDataWithExtraColumns().values().toArray())),
        getTestSchemaWithExtraColumns());
  }

  private static Dataset<Row> createTestDfWithNullColumns() {
    return spark.createDataFrame(
        Collections.singletonList(RowFactory.create(getTestDataWithNullPrimaryKeyValues().values().toArray())),
        getTestSchema());
  }

  private static Dataset<Row> createTestDfWithNullableColumns() {
    return spark.createDataFrame(
        Collections.singletonList(RowFactory.create(getTestDataWithNullPrimaryKeyValues().values().toArray())),
        getTestSchemaWithNullablePrimaryKeys());
  }

  private static Dataset<Row> createTestDf() {
    return spark.createDataFrame(
        Collections.singletonList(RowFactory.create(getTestData().values().toArray())), getTestSchema());
  }

  private static Dataset<Row> createTestDfWithDataTypeMismatch() {
    return spark.createDataFrame(
        Collections.singletonList(RowFactory.create(getTestDataWithDataTypeMismatch().values().toArray())),
        getTestSchemaWithDataTypeMismatch());
  }

  @SneakyThrows
  private static Map<String, Object> getTestData() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("p_col1", 1);
    map.put("p_col2", "a");
    map.put("col1", 1);
    map.put("col2", 2L);
    map.put("col3", true);
    map.put("col4", 2.5f);
    map.put("col5", 3.14);
    map.put("col6", new BigInteger("1234567894321234567890"));
    map.put("col7", new BigDecimal("78943212345678.567076526178312345654654"));
    map.put("col8", "string".getBytes(StandardCharsets.UTF_8));
    map.put("col9", Instant.ofEpochMilli(1528821000000L));
    map.put("col10", "string");
    return map;
  }

  @SneakyThrows
  private static Map<String, Object> getTestDataWithNullPrimaryKeyValues() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("p_col1", null);
    map.put("p_col2", "a");
    map.put("col1", 1);
    map.put("col2", 2L);
    map.put("col3", true);
    map.put("col4", 2.5f);
    map.put("col5", 3.14);
    map.put("col6", new BigInteger("1234567894321234567890"));
    map.put("col7", new BigDecimal("78943212345678.567076526178312345654654"));
    map.put("col8", "string".getBytes(StandardCharsets.UTF_8));
    map.put("col9", Instant.ofEpochMilli(1528821000000L));
    map.put("col10", "string");
    return map;
  }

  @SneakyThrows
  private static Map<String, Object> getTestDataWithExtraColumns() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("p_col1", 1);
    map.put("p_col2", "a");
    map.put("col1", 1);
    map.put("col2", 2L);
    map.put("col3", true);
    map.put("col4", 2.5f);
    map.put("col5", 3.14);
    map.put("col6", new BigInteger("1234567894321234567890"));
    map.put("col7", new BigDecimal("78943212345678.567076526178312345654654"));
    map.put("col8", "string".getBytes(StandardCharsets.UTF_8));
    map.put("col9", Instant.ofEpochMilli(1528821000000L));
    map.put("col10", "string");
    map.put("col11", "string");
    return map;
  }

  @SneakyThrows
  private static Map<String, Object> getTestDataWithMissingColumns() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("p_col1", 1);
    map.put("col1", 1);
    map.put("col2", 2L);
    map.put("col3", true);
    map.put("col4", 2.5f);
    map.put("col5", 3.14);
    map.put("col6", new BigInteger("1234567894321234567890"));
    map.put("col7", new BigDecimal("78943212345678.567076526178312345654654"));
    map.put("col8", "string".getBytes(StandardCharsets.UTF_8));
    map.put("col9", Instant.ofEpochMilli(1528821000000L));
    map.put("col10", "string");
    return map;
  }

  @SneakyThrows
  private static Map<String, Object> getTestDataWithDataTypeMismatch() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("p_col1", "a");
    map.put("p_col2", "a");
    map.put("col1", 1);
    map.put("col2", 2L);
    map.put("col3", true);
    map.put("col4", 2.5f);
    map.put("col5", 3.14);
    map.put("col6", new BigInteger("1234567894321234567890"));
    map.put("col7", new BigDecimal("78943212345678.567076526178312345654654"));
    map.put("col8", "string".getBytes(StandardCharsets.UTF_8));
    map.put("col9", Instant.ofEpochMilli(1528821000000L));
    map.put("col10", "string");
    return map;
  }

  private static StructType getTestSchema() {
    return new StructType(
        new StructField[] {
          new StructField("p_col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("p_col2", DataTypes.StringType, false, Metadata.empty()),
          new StructField("col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("col2", DataTypes.LongType, false, Metadata.empty()),
          new StructField("col3", DataTypes.BooleanType, false, Metadata.empty()),
          new StructField("col4", DataTypes.FloatType, false, Metadata.empty()),
          new StructField("col5", DataTypes.DoubleType, false, Metadata.empty()),
          new StructField("col6", DataTypes.createDecimalType(38, 0), false, Metadata.empty()),
          new StructField("col7", DataTypes.createDecimalType(38, 24), false, Metadata.empty()),
          new StructField("col8", DataTypes.BinaryType, false, Metadata.empty()),
          new StructField("col9", DataTypes.TimestampType, false, Metadata.empty()),
          new StructField("col10", DataTypes.StringType, false, Metadata.empty()),
        });
  }

  private static StructType getTestSchemaWithDataTypeMismatch() {
    return new StructType(
        new StructField[] {
          new StructField("p_col1", DataTypes.StringType, false, Metadata.empty()),
          new StructField("p_col2", DataTypes.StringType, false, Metadata.empty()),
          new StructField("col1", DataTypes.IntegerType, true, Metadata.empty()),
          new StructField("col2", DataTypes.LongType, false, Metadata.empty()),
          new StructField("col3", DataTypes.BooleanType, false, Metadata.empty()),
          new StructField("col4", DataTypes.FloatType, false, Metadata.empty()),
          new StructField("col5", DataTypes.DoubleType, false, Metadata.empty()),
          new StructField("col6", DataTypes.createDecimalType(38, 0), false, Metadata.empty()),
          new StructField("col7", DataTypes.createDecimalType(38, 24), false, Metadata.empty()),
          new StructField("col8", DataTypes.BinaryType, false, Metadata.empty()),
          new StructField("col9", DataTypes.TimestampType, false, Metadata.empty()),
          new StructField("col10", DataTypes.StringType, false, Metadata.empty()),
        });
  }

  private static StructType getTestSchemaWithNullablePrimaryKeys() {
    return new StructType(
        new StructField[] {
          new StructField("p_col1", DataTypes.IntegerType, true, Metadata.empty()),
          new StructField("p_col2", DataTypes.StringType, false, Metadata.empty()),
          new StructField("col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("col2", DataTypes.LongType, false, Metadata.empty()),
          new StructField("col3", DataTypes.BooleanType, false, Metadata.empty()),
          new StructField("col4", DataTypes.FloatType, false, Metadata.empty()),
          new StructField("col5", DataTypes.DoubleType, false, Metadata.empty()),
          new StructField("col6", DataTypes.createDecimalType(38, 0), false, Metadata.empty()),
          new StructField("col7", DataTypes.createDecimalType(38, 24), false, Metadata.empty()),
          new StructField("col8", DataTypes.BinaryType, false, Metadata.empty()),
          new StructField("col9", DataTypes.TimestampType, false, Metadata.empty()),
          new StructField("col10", DataTypes.StringType, false, Metadata.empty()),
        });
  }

  private static StructType getTestSchemaWithExtraColumns() {
    return new StructType(
        new StructField[] {
          new StructField("p_col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("p_col2", DataTypes.StringType, false, Metadata.empty()),
          new StructField("col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("col2", DataTypes.LongType, false, Metadata.empty()),
          new StructField("col3", DataTypes.BooleanType, false, Metadata.empty()),
          new StructField("col4", DataTypes.FloatType, false, Metadata.empty()),
          new StructField("col5", DataTypes.DoubleType, false, Metadata.empty()),
          new StructField("col6", DataTypes.createDecimalType(38, 0), false, Metadata.empty()),
          new StructField("col7", DataTypes.createDecimalType(38, 24), false, Metadata.empty()),
          new StructField("col8", DataTypes.BinaryType, false, Metadata.empty()),
          new StructField("col9", DataTypes.TimestampType, false, Metadata.empty()),
          new StructField("col10", DataTypes.StringType, false, Metadata.empty()),
          new StructField("col11", DataTypes.StringType, false, Metadata.empty())
        });
  }

  private static StructType getTestSchemaWithMissingPrimaryKeys() {
    return new StructType(
        new StructField[] {
          new StructField("p_col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("col1", DataTypes.IntegerType, false, Metadata.empty()),
          new StructField("col2", DataTypes.LongType, false, Metadata.empty()),
          new StructField("col3", DataTypes.BooleanType, false, Metadata.empty()),
          new StructField("col4", DataTypes.FloatType, false, Metadata.empty()),
          new StructField("col5", DataTypes.DoubleType, false, Metadata.empty()),
          new StructField("col6", DataTypes.createDecimalType(38, 0), false, Metadata.empty()),
          new StructField("col7", DataTypes.createDecimalType(38, 24), false, Metadata.empty()),
          new StructField("col8", DataTypes.BinaryType, false, Metadata.empty()),
          new StructField("col9", DataTypes.TimestampType, false, Metadata.empty()),
          new StructField("col10", DataTypes.StringType, false, Metadata.empty()),
        });
  }

  @AfterAll
  public static void stop() {
    spark.close();
  }

  @SneakyThrows
  private long getTopicOffsets(String topicName, Integer partition) {
    DescribeTopicsResult describeTopicsResult =
        kafkaAdminClient.describeTopics(Collections.singletonList(topicName));
    Map<String, TopicDescription> topicDescriptions = describeTopicsResult.all().get();
    TopicDescription topicDescription = topicDescriptions.get(topicName);

    // Prepare partitions to fetch offsets
    List<TopicPartition> topicPartitions = new ArrayList<>();
    for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
      topicPartitions.add(new TopicPartition(topicName, partitionInfo.partition()));
    }

    // Fetch beginning and end offsets
    Map<TopicPartition, OffsetSpec> request = new HashMap<>();
    for (TopicPartition tp : topicPartitions) {
      request.put(tp, OffsetSpec.latest()); // Use OffsetSpec.earliest() for beginning offsets
    }

    ListOffsetsResult offsetsResult = kafkaAdminClient.listOffsets(request);
    Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsets =
        offsetsResult.all().get();

    return offsets.get(new TopicPartition(topicName, partition)).offset();
  }
}
