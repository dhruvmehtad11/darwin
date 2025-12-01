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
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import utils.CommonUtils;

@Slf4j
public class ZkKafkaSetUp
    implements BeforeAllCallback, AfterAllCallback, ExtensionContext.Store.CloseableResource {
  static Boolean started = false;
  GenericContainer kafkaContainer;
  GenericContainer zookeeperContainer;
  GenericContainer awsContainer;
  String teamSuffix = "-test";
  String vpcSuffix = "-test";

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

      this.kafkaContainer =
          new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1")).withReuse(false);

      this.awsContainer =
          new GenericContainer<>("localstack/localstack:4.1")
              .withEnv("SERVICES", "s3")
              .withExposedPorts(4566)
              .withReuse(false);

      this.zookeeperContainer =
          new GenericContainer<>("zookeeper:3.8.2")
              .withExposedPorts(2181)
              .withEnv("ZOOKEEPER_CLIENT_PORT", "2181");

      Startables.deepStart(zookeeperContainer, kafkaContainer, awsContainer).get();

      initWireMock();
      initStubs();
      server.start();

      int port = zookeeperContainer.getMappedPort(2181);
      System.setProperty("zookeeper.host", String.format("localhost:%s", port));

      String awsPort = awsContainer.getMappedPort(4566).toString();

      String kafkaHost = ((KafkaContainer) kafkaContainer).getBootstrapServers();
      System.setProperty("kafka.host", kafkaHost);

      String awsUrl = "http://127.0.0.1:" + awsPort;
      System.setProperty("AWS_ENDPOINT_URL", awsUrl);

      this.createTopics(kafkaHost);

      System.out.println("Migrations applied successfully.");
      this.preApplicationStartup();
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

  public void preApplicationStartup(){}
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
    kafkaContainer.close();
  }

  protected List<NewTopic> getTopicsToCreate() {
    return null;
  }

  protected long getStartupDelay() {
    return 0L;
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

}
