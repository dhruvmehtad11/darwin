package com.dream11.core.util;

import com.dream11.core.config.ConsumerConfig;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@UtilityClass
public class ConsumerUtils {
  public static Map<String, String> getProperties(ConsumerConfig config) {
    Map<String, String> properties = new HashMap<>();
    properties.put("group.id", config.getGroupId());
    properties.put("client.id", config.getClientId());
    properties.put("bootstrap.servers", config.getBootstrapServers());
    properties.put("key.deserializer", config.getKeyDeserializer());
    properties.put("value.deserializer", config.getValueDeserializer());
    properties.put("auto.offset.reset", config.getAutoOffsetReset());
    properties.put("enable.auto.commit", config.getEnableAutoCommit().toString());
    return properties;
  }
}