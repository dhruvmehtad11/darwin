package com.dream11.core.util;

import com.dream11.core.config.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DlqUtils {
  private static final String DEFAULT_DLQ_NAME = "default_dlq";
  public static final String DLQ_SUFFIX = "_dlq";
  public static String getDlqTopicName(String topicName){
    return topicName + DLQ_SUFFIX;
  }
  public static String getDlqTopicNameAsPerEnv() {
    String env = System.getProperty("app.environment", "");
    return String.format("%s_%s", env, DEFAULT_DLQ_NAME);
  }
}
