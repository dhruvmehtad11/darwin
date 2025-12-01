package com.dream11.core.util;

import com.dream11.core.config.ConsumerConfig;
import java.util.HashMap;
import java.util.Map;
import com.dream11.core.config.KafkaAdminConfig;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class KafkaAdminUtils {
  public static Map<String, Object> getProperties(KafkaAdminConfig config) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("bootstrap.servers", config.getBootstrapServers());
    return properties;
  }
}