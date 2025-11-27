package com.dream11.admin.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
