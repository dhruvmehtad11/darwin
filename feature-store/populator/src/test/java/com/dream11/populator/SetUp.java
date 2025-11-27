package com.dream11.populator;

import static com.dream11.populator.constants.ExpectedDataPath.PopulatorWriterTableDataBasePath;
import static com.dream11.populator.expectedtestdata.ExpectedData.populatorFinalConfig;
import static io.restassured.RestAssured.given;

import com.dream11.core.dto.helper.Data;
import com.dream11.core.dto.populator.PopulatorGroupMetadata;
import com.dream11.core.util.S3ClientUtils;
import com.dream11.populator.MockServers.OfsMock;
import com.dream11.populator.util.HelixClusterUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Completable;
import io.restassured.common.mapper.TypeRef;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
public class SetUp extends com.dream11.core.ZkKafkaSetUp {
  private MainApplication app;

  @Override
  public Completable startApplication() {
    initS3();
    app = new MainApplication();
    return app.rxStartApplication();
  }

  public void preApplicationStartup(){
    HelixClusterUtils.createCluster();
  }

  @SneakyThrows
  @Override
  public void postApplicationStartup() {
    long startTime = System.currentTimeMillis();
    boolean stable = false;

    ObjectMapper objectMapper = new ObjectMapper();
    List<PopulatorGroupMetadata> populatorConfig =
        objectMapper.readValue(
            populatorFinalConfig.getJsonArray("data").toString(), new TypeReference<>() {});

    while (!stable) {
      if (System.currentTimeMillis() - startTime > 2 * 60 * 1000L)
        throw new RuntimeException("timed out waiting for the server to acquire stable state");
      Data<Map<String, Map<String, Integer>>> assignmentMap =
          given()
              .header("Content-Type", "application/json")
              .get("/assignments")
              .then()
              .assertThat()
              .statusCode(200)
              .extract()
              .as(new TypeRef<>() {});

      Map<String, Integer> groupedAssignments = new HashMap<>();
      List<PopulatorGroupMetadata> assignments = new ArrayList<>();
      for(Map.Entry<String, Map<String, Integer>> entry : assignmentMap.getData().entrySet()){
        for(Map.Entry<String, Integer> workerEntry : entry.getValue().entrySet()){
          if(!groupedAssignments.containsKey(workerEntry.getKey()))
            groupedAssignments.put(workerEntry.getKey(), 0);
          int count = groupedAssignments.get(workerEntry.getKey()) + 1;
          groupedAssignments.put(workerEntry.getKey(), count);
        }
      }
      for(Map.Entry<String, Integer> group : groupedAssignments.entrySet()){
        assignments.add(PopulatorGroupMetadata.builder().tenantName(group.getKey()).numWorkers(group.getValue()).build());
      }

      if (Objects.deepEquals(populatorConfig, assignments)) stable = true;
      else Thread.sleep(2_000);
    }
  }

  @SneakyThrows
  private void initS3() {
    S3AsyncClient client = S3ClientUtils.createS3Client();
    client.createBucket(CreateBucketRequest.builder().bucket("test-bucket").build()).get();
    client
        .createBucket(
            CreateBucketRequest.builder()
                .bucket(
                    String.format(
                        "darwin-ofs-v2-populator-metadata%s",
                        System.getProperty("TEAM_SUFFIX", "test")))
                .build())
        .get();

    Path root = Path.of(PopulatorWriterTableDataBasePath);
    Files.walk(root)
        .forEach(r -> {
          if(r.toFile().isFile()) {
            try {
              client
                  .putObject(
                      PutObjectRequest.builder()
                          .bucket("test-bucket")
                          .key(root.relativize(r).toString())
                          .build(), r)
                  .get();
            } catch (InterruptedException | ExecutionException e) {
              throw new RuntimeException(e);
            }
          }
        });
  }

  @Override
  public void close() throws Throwable {
    super.close();
    app.rxStopApplication(0).blockingGet();
  }

  @Override
  protected void initStubs() {
    OfsMock.mockServer(this.server);
  }

  @Override
  protected long getStartupDelay() {
    return 10_000L;
  }
}
