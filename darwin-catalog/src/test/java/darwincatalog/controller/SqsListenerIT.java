package darwincatalog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.config.ConfigProvider;
import darwincatalog.entity.ConfigEntity;
import darwincatalog.exception.AssetNotFoundException;
import darwincatalog.listener.SqsMessageListener;
import darwincatalog.repository.ConfigRepository;
import darwincatalog.service.AssetService;
import darwincatalog.testcontainers.AbstractIntegrationTest;
import darwincatalog.testcontainers.SharedSqsQueue;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.openapitools.model.Asset;
import org.openapitools.model.Field;
import org.openapitools.model.KafkaDetail;
import org.openapitools.model.TableDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.Column;
import software.amazon.awssdk.services.glue.model.GetTableRequest;
import software.amazon.awssdk.services.glue.model.GetTableResponse;
import software.amazon.awssdk.services.glue.model.StorageDescriptor;
import software.amazon.awssdk.services.glue.model.Table;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(
    scripts = "/sql/glue_listener_cleanup.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class SqsListenerIT extends AbstractIntegrationTest {

  @TestConfiguration
  static class SqsTestConfig {
    @Bean
    public SqsClient sqsClientIT() {
      SharedSqsQueue sharedSqsQueue = SharedSqsQueue.getInstance();
      return SqsClient.builder()
          .endpointOverride(sharedSqsQueue.getEndpointOverride(SQS))
          .region(Region.of(sharedSqsQueue.getRegion()))
          .credentialsProvider(
              StaticCredentialsProvider.create(
                  AwsBasicCredentials.create(
                      sharedSqsQueue.getAccessKey(), sharedSqsQueue.getSecretKey())))
          .build();
    }

    @Bean
    public String getQueueUrlIT(@Qualifier("sqsClientIT") SqsClient sqsClient) {
      return sqsClient
          .createQueue(CreateQueueRequest.builder().queueName("test-queue").build())
          .queueUrl();
    }

    @Primary
    @Bean(name = "testConfigProvider")
    public ConfigProvider getConfigProvider() {
      ConfigRepository configRepository = Mockito.mock(ConfigRepository.class);
      ConfigEntity configEntity =
          ConfigEntity.builder()
              .key("glue_database_whitelist")
              .value(
                  "[\"mock-db-1\", \"mock-db-2\", \"redshift_segment_company_transactions\", \"stream_data_highway\", \"example_org_transactions\"]")
              .valueType("LIST")
              .build();
      when(configRepository.findAll()).thenReturn(List.of(configEntity));
      return new ConfigProvider(configRepository);
    }
  }

  @Autowired
  @Qualifier("sqsClientIT")
  private SqsClient sqsClient;

  @Autowired
  @Qualifier("getQueueUrlIT")
  private String queueUrl;

  @MockBean private GlueClient glueClient;

  @Autowired private SqsMessageListener sqsMessageListener;
  @Autowired private AssetService assetService;
  @Autowired private ObjectMapper objectMapper;

  private static final Instant createdAt = Instant.parse("2025-06-12T10:30:00Z");
  private static final Instant updatedAt = Instant.parse("2025-06-13T10:30:00Z");

  private static final String createTableEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"CreateTable\", \"databaseName\": \"mock-db-1\", \"changedTables\": [\"mock-table-1\"], \"changedPartitions\": [] } }";
  private static final String updateTableEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"UpdateTable\", \"databaseName\": \"mock-db-1\", \"tableName\": \"mock-table-1\", \"changedPartitions\": [] } }";
  private static final String createDatabaseEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"CreateDatabase\", \"databaseName\": \"redshift_segment_company_transactions\", \"changedTables\": [\"mock-table-1\", \"mock-table-2\", \"mock-table-3\"], \"changedPartitions\": [] } }";
  private static final String deleteTableEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"DeleteTable\", \"databaseName\": \"redshift_segment_company_transactions\", \"changedTables\": [\"mock-table-2\"], \"changedPartitions\": [] } }";
  private static final String deleteDatabaseEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"DeleteDatabase\", \"databaseName\": \"redshift_segment_company_transactions\", \"changedTables\": [\"mock-table-1\", \"mock-table-3\"], \"changedPartitions\": [] } }";

  private static final String streamCreateEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"CreateTable\", \"databaseName\": \"stream_data_highway\", \"changedTables\": [\"mock-stream-1\"], \"changedPartitions\": [] } }";
  private static final String streamDeleteEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"DeleteTable\", \"databaseName\": \"stream_data_highway\", \"changedTables\": [\"mock-stream-1\"], \"changedPartitions\": [] } }";

  private static final String parquetCreateEvent =
      "{ \"version\": \"0\", \"id\": \"abc12345-6789-4321-abcd-efgh98765432\", \"detail-type\": \"Glue Data Catalog Table State Change\", \"source\": \"aws.glue\", \"account\": \"123456789012\", \"time\": \"2025-06-12T10:30:00Z\", \"region\": \"us-east-1\", \"resources\": [\"arn:aws:glue:us-east-1:123456789012:table/my-db/my-table\"], \"detail\": { \"typeOfChange\": \"CreateTable\", \"databaseName\": \"example_org_transactions\", \"changedTables\": [\"mock-table-1\"], \"changedPartitions\": [] } }";

  @Test
  @DisplayName("Test Glue Listener for CreateTable Event")
  @Order(100)
  void testGlueListener1() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(createTableEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    List<Column> columns =
        List.of(
            Column.builder().name("id").type("int").build(),
            Column.builder().name("name").type("string").build());

    GetTableRequest getTableRequest =
        GetTableRequest.builder().databaseName("mock-db-1").name("mock-table-1").build();
    when(glueClient.getTable(getTableRequest))
        .thenReturn(mockTable("mock-db-1", "mock-table-1", "iceberg", createdAt, columns));
    sqsMessageListener.pollQueue(queueUrl);

    Asset asset =
        assetService.getAssetByName(
            "example:table:lakehouse:mock-db-1:mock-table-1", Collections.emptyList());

    assertNotNull(asset);
    assertEquals("example:table:lakehouse:mock-db-1:mock-table-1", asset.getFqdn());
    TableDetail tableDetail = (TableDetail) asset.getDetail();
    assertEquals("example", tableDetail.getOrg());
    assertEquals("lakehouse", tableDetail.getType());
    assertNull(tableDetail.getCatalogName());
    assertEquals("mock-db-1", tableDetail.getDatabaseName());
    assertEquals("mock-table-1", tableDetail.getTableName());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetCreatedAt());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetUpdatedAt());
    assertEquals(
        "{\"path\":\"s3://mock-bucket/mock-db-1/mock-table-1\",\"type\":\"iceberg\"}",
        convertMetadataToJson(asset.getMetadata()));

    List<Field> fields = asset.getAssetSchema().getSchemaJson().getFields();
    assertNotNull(fields);
    assertEquals(2, fields.size());
    assertEquals("id", fields.get(0).getName());
    assertEquals("int", fields.get(0).getType());
    assertEquals("name", fields.get(1).getName());
    assertEquals("string", fields.get(1).getType());
  }

  @Test
  @DisplayName("Test Glue Listener for UpdateTable Event")
  @Order(200)
  void testGlueListener2() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(updateTableEvent).build();
    sqsClient.sendMessage(sendMessageRequest);
    List<Column> columns =
        List.of(
            Column.builder().name("id").type("int").build(),
            Column.builder().name("name").type("string").build(),
            Column.builder().name("new_column").type("string").build());

    GetTableRequest getTableRequest =
        GetTableRequest.builder().databaseName("mock-db-1").name("mock-table-1").build();
    when(glueClient.getTable(getTableRequest))
        .thenReturn(mockTable("mock-db-1", "mock-table-1", "iceberg", updatedAt, columns));
    sqsMessageListener.pollQueue(queueUrl);

    Asset asset =
        assetService.getAssetByName(
            "example:table:lakehouse:mock-db-1:mock-table-1", Collections.emptyList());

    assertNotNull(asset);
    assertEquals("example:table:lakehouse:mock-db-1:mock-table-1", asset.getFqdn());
    TableDetail tableDetail = (TableDetail) asset.getDetail();
    assertEquals("example", tableDetail.getOrg());
    assertEquals("lakehouse", tableDetail.getType());
    assertNull(tableDetail.getCatalogName());
    assertEquals("mock-db-1", tableDetail.getDatabaseName());
    assertEquals("mock-table-1", tableDetail.getTableName());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetCreatedAt());
    assertEquals(updatedAt.toEpochMilli(), asset.getAssetUpdatedAt());
    assertEquals(
        "{\"path\":\"s3://mock-bucket/mock-db-1/mock-table-1\",\"type\":\"iceberg\"}",
        convertMetadataToJson(asset.getMetadata()));

    List<Field> fields = asset.getAssetSchema().getSchemaJson().getFields();
    assertNotNull(fields);
    assertEquals(3, fields.size());
    assertEquals("id", fields.get(0).getName());
    assertEquals("int", fields.get(0).getType());
    assertEquals("name", fields.get(1).getName());
    assertEquals("string", fields.get(1).getType());
    assertEquals("new_column", fields.get(2).getName());
    assertEquals("string", fields.get(2).getType());
  }

  @Test
  @DisplayName("Test Glue Listener for CreateDatabase Event")
  @Order(100)
  void testGlueListener3() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(createDatabaseEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    List<Column> columns =
        List.of(
            Column.builder().name("id").type("int").build(),
            Column.builder().name("name").type("string").build());
    when(glueClient.getTable((GetTableRequest) any()))
        .thenReturn(
            mockTable(
                "redshift_segment_company_transactions",
                "mock-table-1",
                "redshift",
                createdAt,
                columns))
        .thenReturn(
            mockTable(
                "redshift_segment_company_transactions",
                "mock-table-2",
                "redshift",
                createdAt,
                columns))
        .thenReturn(
            mockTable(
                "redshift_segment_company_transactions",
                "mock-table-3",
                "redshift",
                createdAt,
                columns));
    sqsMessageListener.pollQueue(queueUrl);

    Asset asset =
        assetService.getAssetByName(
            "example:table:redshift:segment:company_transactions:mock-table-2",
            Collections.emptyList());

    assertNotNull(asset);
    assertEquals(
        "example:table:redshift:segment:company_transactions:mock-table-2", asset.getFqdn());
    TableDetail tableDetail = (TableDetail) asset.getDetail();
    assertEquals("example", tableDetail.getOrg());
    assertEquals("redshift", tableDetail.getType());
    assertEquals("segment", tableDetail.getCatalogName());
    assertEquals("company_transactions", tableDetail.getDatabaseName());
    assertEquals("mock-table-2", tableDetail.getTableName());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetCreatedAt());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetUpdatedAt());
    assertEquals(
        "{\"path\":\"s3://mock-bucket/redshift_segment_company_transactions/mock-table-2\",\"type\":\"redshift\"}",
        convertMetadataToJson(asset.getMetadata()));

    List<Field> fields = asset.getAssetSchema().getSchemaJson().getFields();
    assertNotNull(fields);
    assertEquals(2, fields.size());
    assertEquals("id", fields.get(0).getName());
    assertEquals("int", fields.get(0).getType());
    assertEquals("name", fields.get(1).getName());
    assertEquals("string", fields.get(1).getType());
  }

  @Test
  @DisplayName("Test Glue Listener for DeleteTable Event")
  @Order(200)
  void testGlueListener5() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(deleteTableEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    sqsMessageListener.pollQueue(queueUrl);

    Executable getAssetCallable =
        () ->
            assetService.getAssetByName(
                "example:table:lakehouse:mock-db-2:mock-table-2", Collections.emptyList());
    assertThrows(AssetNotFoundException.class, getAssetCallable);
  }

  @Test
  @DisplayName("Test Glue Listener for DeleteDatabase Event")
  @Order(300)
  void testGlueListener6() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(deleteDatabaseEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    sqsMessageListener.pollQueue(queueUrl);

    Executable getAssetCallable =
        () ->
            assetService.getAssetByName(
                "example:table:lakehouse:mock-db-2:mock-table-2", Collections.emptyList());
    assertThrows(AssetNotFoundException.class, getAssetCallable);
  }

  @Test
  @DisplayName("Test Glue Listener for CreateTable Event for a stream")
  @Order(100)
  void testGlueListener7() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(streamCreateEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    List<Column> columns =
        List.of(
            Column.builder().name("id").type("int").build(),
            Column.builder().name("name").type("string").build());

    GetTableRequest getTableRequest =
        GetTableRequest.builder().databaseName("stream_data_highway").name("mock-stream-1").build();
    when(glueClient.getTable(getTableRequest))
        .thenReturn(
            mockTable("stream_data_highway", "mock-stream-1", "stream", createdAt, columns));
    sqsMessageListener.pollQueue(queueUrl);

    Asset asset =
        assetService.getAssetByName(
            "example:kafka:data_highway:mock-stream-1", Collections.emptyList());

    assertNotNull(asset);
    assertEquals("example:kafka:data_highway:mock-stream-1", asset.getFqdn());
    KafkaDetail kafkaDetail = (KafkaDetail) asset.getDetail();
    assertEquals("example", kafkaDetail.getOrg());
    assertEquals("data_highway", kafkaDetail.getClusterName());
    assertEquals("mock-stream-1", kafkaDetail.getTopicName());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetCreatedAt());
    assertEquals(createdAt.toEpochMilli(), asset.getAssetUpdatedAt());
    assertEquals(
        "{\"path\":\"s3://mock-bucket/stream_data_highway/mock-stream-1\"}",
        convertMetadataToJson(asset.getMetadata()));

    List<Field> fields = asset.getAssetSchema().getSchemaJson().getFields();
    assertNotNull(fields);
    assertEquals(2, fields.size());
    assertEquals("id", fields.get(0).getName());
    assertEquals("int", fields.get(0).getType());
    assertEquals("name", fields.get(1).getName());
    assertEquals("string", fields.get(1).getType());
  }

  @Test
  @DisplayName("Test Glue Listener for DeleteTable Event for a stream")
  @Order(200)
  void testGlueListener8() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(streamDeleteEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    sqsMessageListener.pollQueue(queueUrl);

    Executable getAssetCallable =
        () ->
            assetService.getAssetByName(
                "example:kafka:data_highway:mock-stream-1", Collections.emptyList());
    assertThrows(AssetNotFoundException.class, getAssetCallable);
  }

  @Test
  @DisplayName("Test Glue Listener for CreateTable Event for a parquet table")
  @Order(100)
  void testGlueListener9() {
    ReflectionTestUtils.setField(sqsMessageListener, "sqsClient", sqsClient);
    SendMessageRequest sendMessageRequest =
        SendMessageRequest.builder().queueUrl(queueUrl).messageBody(parquetCreateEvent).build();
    sqsClient.sendMessage(sendMessageRequest);

    List<Column> columns =
        List.of(
            Column.builder().name("id").type("int").build(),
            Column.builder().name("name").type("string").build());

    GetTableRequest getTableRequest =
        GetTableRequest.builder()
            .databaseName("example_org_transactions")
            .name("mock-table-1")
            .build();
    when(glueClient.getTable(getTableRequest))
        .thenReturn(
            mockTable("example_org_transactions", "mock-table-1", "parquet", createdAt, columns));
    sqsMessageListener.pollQueue(queueUrl);

    Asset asset =
        assetService.getAssetByName(
            "example:table:lakehouse:example_org_transactions:mock-table-1",
            Collections.emptyList());

    assertNotNull(asset);
    assertEquals("example:table:lakehouse:example_org_transactions:mock-table-1", asset.getFqdn());
    TableDetail tableDetail = (TableDetail) asset.getDetail();
    assertEquals("example", tableDetail.getOrg());
    assertEquals("example_org_transactions", tableDetail.getDatabaseName());
    assertEquals("mock-table-1", tableDetail.getTableName());
    assertEquals("lakehouse", tableDetail.getType());
    assertEquals(
        "{\"path\":\"s3://mock-bucket/example_org_transactions/mock-table-1\",\"type\":\"parquet\"}",
        convertMetadataToJson(asset.getMetadata()));
    assertNull(tableDetail.getCatalogName());

    List<Field> fields = asset.getAssetSchema().getSchemaJson().getFields();
    assertNotNull(fields);
    assertEquals(2, fields.size());
    assertEquals("id", fields.get(0).getName());
    assertEquals("int", fields.get(0).getType());
    assertEquals("name", fields.get(1).getName());
    assertEquals("string", fields.get(1).getType());
  }

  public static GetTableResponse mockTable(
      String databaseName,
      String tableName,
      String tableType,
      Instant updatedAt,
      List<Column> columns) {
    StorageDescriptor.Builder storageDescriptorBuilder =
        StorageDescriptor.builder()
            .columns(columns)
            .location("s3://mock-bucket/" + databaseName + "/" + tableName);
    Map<String, String> parameters = new HashMap<>();
    if (tableType.equals("iceberg") || tableType.equals("redshift")) {
      parameters = Map.of("table_type", tableType, "classification", tableType);
    } else if (tableType.equals("parquet")) {
      storageDescriptorBuilder
          .inputFormat("org.apache.hadoop.mapred.ParquetFormat")
          .outputFormat("org.apache.hadoop.hive.ql.io.ParquetFormat")
          .build();
    }
    Table table =
        Table.builder()
            .name(tableName)
            .databaseName(databaseName)
            .owner("test-owner")
            .storageDescriptor(storageDescriptorBuilder.build())
            .parameters(parameters)
            .createTime(createdAt)
            .updateTime(updatedAt)
            .build();
    return GetTableResponse.builder().table(table).build();
  }

  private String convertMetadataToJson(Map<String, Object> metadata) {
    if (metadata == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(metadata);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert metadata to JSON", e);
    }
  }
}
