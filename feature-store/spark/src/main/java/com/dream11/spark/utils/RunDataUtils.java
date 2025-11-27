package com.dream11.spark.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class RunDataUtils {
  public static String createRunId() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'run-'yyyy-MM-dd-HH-mm-ss")
        .withZone(ZoneOffset.UTC);
    return LocalDateTime.now().format(formatter);
  }
}
