package com.dream11.core.util;

import com.dream11.core.config.ProducerConfig;
import io.vertx.core.json.JsonObject;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@UtilityClass
public class ProducerUtils {
  public static Map<String, String> getProperties(ProducerConfig config) {
    Map<String, String> properties = new HashMap<>();
    properties.put("bootstrap.servers", config.getBootstrapServers());
    properties.put("key.serializer", config.getKeySerializer());
    properties.put("value.serializer", config.getValueSerializer());
    return properties;
  }

}