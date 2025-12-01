package com.dream11.populator;

import static com.dream11.core.util.Utils.getAndAssertResponse;
import static com.dream11.populator.expectedrequestresponse.PopulatorWriterTest.SuccessWriterTest;

import com.dream11.core.config.ConfigReader;
import com.dream11.core.config.ConsumerConfig;
import com.dream11.core.util.ConsumerUtils;
import com.google.gson.JsonParser;
import io.vertx.core.json.JsonObject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import io.vertx.reactivex.kafka.client.consumer.KafkaConsumerRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SetUp.class)
public class WriterIT {
  @Test
  public void successWriteFeaturesTest() throws InterruptedException {
    JsonObject expectedResponse = SuccessWriterTest.getJsonObject("response");
    getAndAssertResponse(SuccessWriterTest.getJsonObject("request"), expectedResponse);
    Thread.sleep(30 * 1_000);

    ConsumerConfig config = ConfigReader.readConsumerConfigFromFile();
    config.setClientId("test-client");
    config.setGroupId("test");
    Map<String, Object> conf =
        ConsumerUtils.getProperties(config).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    int recordCount = 0;
    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(conf)) {
      consumer.subscribe(Set.of("test-topic"));
      List<ConsumerRecord<String, String>> recordList = new ArrayList<>();
      long start = System.currentTimeMillis();
      // 5 records, 2 versions, 3 records per version but deleted 1 file to mimic compaction
      while (recordCount != 5) {
        // exit after 1 min
        if (System.currentTimeMillis() - start > 60 * 1_000)
          throw new RuntimeException("timeout waiting for all records");

        ConsumerRecords<String, String> records =
            consumer.poll(Duration.of(5_000, ChronoUnit.MILLIS));
        recordCount += records.count();
        Iterable<ConsumerRecord<String, String>> itr = records.records("test-top");
        itr.forEach(recordList::add);
      }

      for(ConsumerRecord<String, String> record: recordList){
        Map<String, String> headers = new HashMap<>();
        record
            .headers()
            .iterator()
            .forEachRemaining(h -> headers.put(h.key(), new String(h.value())));
        assert headers.containsKey("sdk-version");
        assert Objects.equals(headers.get("sdk-version"), "v2");
        assert headers.containsKey("feature-group-name");
        assert Objects.equals(headers.get("feature-group-name"), "fg1");
        assert headers.containsKey("feature-group-version");
        assert Objects.equals(headers.get("feature-group-version"), "v1");
        assert headers.containsKey("run-id");
        assert Objects.equals(headers.get("run-id"), "run-01");
      }
    }
  }
}
