package com.dream11.admin.s3metastoreintegrationtests;

import com.dream11.admin.MainApplication;
import com.dream11.admin.expectedrequestresponse.KafkaTopicMetadataTestData;
import io.reactivex.Completable;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import java.util.Map;
import java.util.stream.Collectors;
import com.dream11.admin.expectedrequestresponse.S3MetastoreTestData;
import io.vertx.core.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.dream11.core.dto.kafka.KafkaTopicMetadata;
import com.dream11.admin.util.Utils;

@Slf4j
public class SetUp extends com.dream11.core.SetUp {
  private MainApplication app;

  @Override
  public Completable startApplication() {
    app = new MainApplication();
    return app.rxStartApplication();
  }

  @Override
  public void close() throws Throwable {
    super.close();
    app.rxStopApplication(0).blockingGet();
  }

  @Override
  public String getSqlSeedPath() {
    return "../admin/src/test/resources/sqlseed/CacheWarmingRestSeed.sql";
  }

  @Override
  public List<String> getTenants() {
    return List.of("t1");
  }
  
  @Override
  public List<NewTopic> getTopicsToCreate() {
        return KafkaTopicMetadataTestData.getNewTopicListForKafkaTopicMetadataTest();
  }

}
