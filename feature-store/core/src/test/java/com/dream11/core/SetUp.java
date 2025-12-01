package com.dream11.core;

import static com.dream11.core.constant.Constants.DEFAULT_TENANT_NAME;
import static com.dream11.core.util.Utils.createKafkaTopic;
import static com.dream11.core.util.Utils.deleteTopic;

import com.datastax.oss.driver.api.core.CqlSession;
import com.dream11.core.util.S3ClientUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.reactivex.Completable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import utils.CommonUtils;

@Slf4j
public class SetUp
    implements BeforeAllCallback, AfterAllCallback, ExtensionContext.Store.CloseableResource {
  static Boolean started = false;
  GenericContainer sqlContainer;
  GenericContainer cassandraContainer;
  GenericContainer kafkaContainer;
  GenericContainer tenantCassandraContainer;
  GenericContainer awsContainer;
  String teamSuffix = "-dummyTeamSuffix";
  String vpcSuffix = "-dummyVpcSuffix";

  protected WireMockServer server;

  @Override
  @BeforeSuite(alwaysRun = true)
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    log.info(">>>>>>>Triggering Before Suite>>>>>>>>>");
    if (!started) {
      System.setProperty("TEAM_SUFFIX", teamSuffix);
      System.setProperty("VPC_SUFFIX", vpcSuffix);
      System.setProperty("app.environment", "test");
      System.setProperty("ENV", "test");
      System.setProperty("IGNITE_JETTY_PORT", "80");

      String sqlHost = "127.0.0.1";
      String sqlUsername = "root";
      String sqlPassword = "password";
      String sqlDb = "metastore";

      String cassandraHost = "127.0.0.1";

      this.sqlContainer =
          new GenericContainer<>("mysql:8.0.35")
              .withEnv("MYSQL_ROOT_PASSWORD", "password")
              .withEnv("TZ", "GMT")
              .withExposedPorts(3306)
              .withReuse(false);

      this.cassandraContainer =
          new GenericContainer<>("cassandra:5.0.2")
              .withExposedPorts(9042)
              .withReuse(false)
              .withEnv("MAX_HEAP_SIZE", "128m")
              .withStartupTimeout(Duration.ofMinutes(2));

      this.kafkaContainer =
          new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1")).withReuse(false);

      this.awsContainer =
          new GenericContainer<>("localstack/localstack:4.1")
              .withEnv("SERVICES", "s3")
              .withExposedPorts(4566)
              .withReuse(false);

      Startables.deepStart(sqlContainer, cassandraContainer, kafkaContainer, awsContainer).get();

      initWireMock();
      initStubs();
      server.start();

      String sqlPort = sqlContainer.getMappedPort(3306).toString();

      Integer cassandraPort = cassandraContainer.getMappedPort(9042);

      String migrationUrl = "jdbc:mysql://localhost:" + sqlPort + "/mysql";

      String mysqlUrl = "jdbc:mysql://localhost:" + sqlPort + "/metastore";

      String awsPort = awsContainer.getMappedPort(4566).toString();

      System.setProperty("mysql.masterHost", sqlHost);
      System.setProperty("mysql.slaveHost", sqlHost);
      System.setProperty("mysql.port", sqlPort);
      System.setProperty("mysql.database", sqlDb);
      System.setProperty("mysql.username", sqlUsername);
      System.setProperty("mysql.password", sqlPassword);

      System.setProperty("cassandra.host", cassandraHost);
      System.setProperty("cassandra.port", cassandraPort.toString());

      String kafkaHost = ((KafkaContainer) kafkaContainer).getBootstrapServers();
      System.setProperty("kafka.host", kafkaHost);

      String awsUrl = "http://127.0.0.1:" + awsPort;
      System.setProperty("AWS_ENDPOINT_URL", awsUrl);

      this.initializeSql(migrationUrl, mysqlUrl, sqlUsername, sqlPassword);
      this.initializeCassandra(cassandraHost, cassandraPort);

      this.addSqlSeed(migrationUrl, mysqlUrl, sqlUsername, sqlPassword);
      this.addCqlSeed(cassandraHost, cassandraPort);
      this.createTopics(kafkaHost);

      this.initMultiTenantConfig();
      this.initS3MetastoreConfig();

      System.out.println("Migrations applied successfully.");
      Throwable error =
          this.startApplication()
              .doOnError(
                  (i) -> {
                    log.error("Error in starting containers: " + i.getLocalizedMessage());
                  })
              .delay(getStartupDelay(), TimeUnit.MILLISECONDS) // doing this to prefill caches
              .blockingGet();
      if (error != null) {
        log.error("error:- ", error);
        throw new Exception(error);
      }

      this.postApplicationStartup();
    }
    started = true;
    extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("rest-core", this);
  }

  public void postApplicationStartup(){}

  public Completable startApplication() {
    return Completable.complete();
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

  protected void initStubs() {}

  @Override
  public void afterAll(ExtensionContext extensionContext) throws Exception {
    log.info(">>>>>>>Triggering After Suite>>>>>>>>>");
  }

  @Override
  public void close() throws Throwable {
    log.info("Closing containers and application");
    if(sqlContainer!=null)
    sqlContainer.close();
    if(cassandraContainer!=null)
    cassandraContainer.close();
    kafkaContainer.close();
  }

  @SneakyThrows
  public void initializeSql(
      String migrationUrl, String url, String sqlUsername, String sqlPassword) {

    try (Connection connection =
        DriverManager.getConnection(migrationUrl, sqlUsername, sqlPassword)) {
      Statement statement = connection.createStatement();
      statement.execute("DROP DATABASE IF EXISTS metastore;");
      statement.close();
    } catch (Exception e) {
      throw new RuntimeException("sql database initialization error");
    }

    try (Connection connection =
        DriverManager.getConnection(migrationUrl, sqlUsername, sqlPassword)) {
      Statement statement = connection.createStatement();
      statement.execute("CREATE DATABASE metastore;");
      statement.close();
    } catch (Exception e) {
      throw new RuntimeException("sql database initialization error");
    }

    try {
      Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
      try (BufferedReader reader =
          new BufferedReader(
              new FileReader(
                  "../core/src/main/resources/db/mysql/migrations/20231103142004_CreateTables.sql"))) {
        StringBuilder sql = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          sql.append(line).append("\n");
        }
        String[] sqlStatements = sql.toString().split(";\n");

        Statement statement = connection.createStatement();

        for (String sqlStatement : sqlStatements) {
          if (!sqlStatement.trim().isEmpty()) {
            statement.addBatch(sqlStatement);
          }
        }

        statement.executeBatch();
        statement.close();
        connection.close();
      }
      connection.close();
    } catch (Exception e) {
      throw new RuntimeException("sql migration initialization error: ", e);
    }
  }

  @SneakyThrows
  public void initializeCassandra(String host, Integer port) {
    try {
      CqlSession session =
          CqlSession.builder()
              .withLocalDatacenter("datacenter1")
              .addContactPoint(new InetSocketAddress(host, port))
              .build();
      String query = "DROP KEYSPACE IF EXISTS ofs;";
      session.execute(query);
      session.close();

    } catch (Exception e) {
      throw new RuntimeException("cassandra migration initialization error ", e);
    }

    try {
      CqlSession session =
          CqlSession.builder()
              .withLocalDatacenter("datacenter1")
              .addContactPoint(new InetSocketAddress(host, port))
              .build();
      String query =
          "CREATE KEYSPACE IF NOT EXISTS ofs WITH replication = { 'class' : 'org.apache.cassandra.locator.SimpleStrategy'};";
      session.execute(query);
      session.close();

    } catch (Exception e) {
      throw new RuntimeException("cassandra migration initialization error ", e);
    }
  }

  protected String getSqlSeedPath() {
    return null;
  }

  protected String getCqlSeedPath() {
    return null;
  }

  protected List<NewTopic> getTopicsToCreate() {
    return null;
  }

  protected long getStartupDelay() {
    return 0L;
  }

  @SneakyThrows
  private void addSqlSeed(String migrationUrl, String url, String sqlUsername, String sqlPassword) {
    String path = getSqlSeedPath();
    if (path == null) {
      return;
    }
    try {
      Connection connection = DriverManager.getConnection(url, sqlUsername, sqlPassword);
      try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        StringBuilder sql = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          sql.append(line).append("\n");
        }
        String[] sqlStatements = sql.toString().split(";\n");

        Statement statement = connection.createStatement();

        for (String sqlStatement : sqlStatements) {
          if (!sqlStatement.trim().isEmpty()) {
            statement.addBatch(sqlStatement);
          }
        }

        statement.executeBatch();
        statement.close();
        connection.close();
      }
      connection.close();
    } catch (Exception e) {
      throw new RuntimeException("sql migration initialization error: ", e);
    }
  }

  @SneakyThrows
  private void createTopics(String kafkaHost) {
    List<NewTopic> topics = getTopicsToCreate();
    if (topics == null) {
      return;
    }
    for (NewTopic t : topics) {
      deleteTopic(kafkaHost, t.name());
      createKafkaTopic(kafkaHost, t);
    }
  }

  public void addCqlSeed(String host, Integer port) {
    String path = getCqlSeedPath();
    if (path == null) {
      return;
    }
    try (CqlSession session =
        CqlSession.builder()
            .withLocalDatacenter("datacenter1")
            .addContactPoint(new InetSocketAddress(host, port))
            .build()) {

      try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        String sqlStatements = reader.lines().collect(Collectors.joining("\n"));

        String[] sqlStatementsArray = sqlStatements.split(";");

        for (String sqlStatement : sqlStatementsArray) {
          if (!sqlStatement.trim().isEmpty()) {
            session.execute(sqlStatement);
          }
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Cassandra migration initialization error", e);
    }
  }

  public void addCqlSeed(String host, Integer port, String path) {
    if (path == null) {
      return;
    }
    try (CqlSession session =
             CqlSession.builder()
                 .withLocalDatacenter("datacenter1")
                 .addContactPoint(new InetSocketAddress(host, port))
                 .build()) {

      try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        String sqlStatements = reader.lines().collect(Collectors.joining("\n"));

        String[] sqlStatementsArray = sqlStatements.split(";");

        for (String sqlStatement : sqlStatementsArray) {
          if (!sqlStatement.trim().isEmpty()) {
            session.execute(sqlStatement);
          }
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Cassandra migration initialization error", e);
    }
  }

  protected List<String> getTenants() {
    return new ArrayList<>();
  }

  protected void initMultiTenantConfig() {
    List<String> tenants = getTenants();

    this.tenantCassandraContainer =
        new GenericContainer<>("cassandra:5.0.2")
            .withExposedPorts(9042)
            .withReuse(false)
            .withEnv("MAX_HEAP_SIZE", "128m")
            .withStartupTimeout(Duration.ofMinutes(2));
    tenantCassandraContainer.start();
    // default
    System.setProperty("cassandra.tenant." + DEFAULT_TENANT_NAME + ".host", "127.0.0.1");
    System.setProperty(
        "cassandra.tenant." + DEFAULT_TENANT_NAME + ".port",
        cassandraContainer.getMappedPort(9042).toString());

    for (String tenant : tenants) {
      System.setProperty("cassandra.tenant." + tenant + ".host", "127.0.0.1");
      System.setProperty(
          "cassandra.tenant." + tenant + ".port",
          tenantCassandraContainer.getMappedPort(9042).toString());
    }
  }

  protected void initS3MetastoreConfig(){
    S3AsyncClient client = S3ClientUtils.createS3Client();
    try {
      client.createBucket(CreateBucketRequest.builder().bucket("darwin-ofs-v2-metastore-test").build()).get();
    } catch (Exception e) {
      throw new RuntimeException("error creating metastore bucket", e);
    }
    client.close();
  }

}
