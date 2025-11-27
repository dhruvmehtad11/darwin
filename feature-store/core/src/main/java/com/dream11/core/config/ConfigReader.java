package com.dream11.core.config;

import com.dream11.common.app.AppContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigReader {
  private final static String envConfigSuffix =
      java.util.Optional.ofNullable(System.getProperty("app.environment")).orElse("dev") + ".conf";

  @SneakyThrows
  public static ConsumerConfig readConsumerConfigFromFile() {
    String configFile = "config/kafka-consumer/kafka-consumer-" + envConfigSuffix;
    Config config =
        ConfigFactory.load(configFile)
            .withFallback(
                ConfigFactory.load(
                    "config/kafka-consumer/kafka-consumer-default.conf"));
    return AppContext.getInstance(ObjectMapper.class).readValue(
        config.root().render(ConfigRenderOptions.concise()), ConsumerConfig.class);
  }

  @SneakyThrows
  public static ProducerConfig readProducerConfigFromFile() {
    String configFile = "config/kafka-producer" + "/kafka-producer-" + envConfigSuffix;
    Config config =
        ConfigFactory.load(configFile)
            .withFallback(
                ConfigFactory.load(
                    "config/kafka-producer" + "/kafka-producer-default.conf"));
    return AppContext.getInstance(ObjectMapper.class).readValue(
        config.root().render(ConfigRenderOptions.concise()), ProducerConfig.class);
  }

  @SneakyThrows
  public static KafkaAdminConfig readKafkaAdminConfigFromFile() {
    String configFile = "config/kafka-admin" + "/kafka-admin-" + envConfigSuffix;
    Config config =
        ConfigFactory.load(configFile)
            .withFallback(
                ConfigFactory.load(
                    "config/kafka-admin" + "/kafka-admin-default.conf"));
    return AppContext.getInstance(ObjectMapper.class).readValue(
        config.root().render(ConfigRenderOptions.concise()), KafkaAdminConfig.class);
  }

  @SneakyThrows
  public static HelixConfig readHelixConfigFromFile() {
    String configFile = "config/helix" + "/helix-" + envConfigSuffix;
    Config config =
        ConfigFactory.load(configFile)
            .withFallback(
                ConfigFactory.load(
                    "config/helix" + "/helix-default.conf"));
    return AppContext.getInstance(ObjectMapper.class).readValue(
        config.root().render(ConfigRenderOptions.concise()), HelixConfig.class);
  }
  
}
