package com.dream11.spark.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
  private static final ObjectMapper objectMapper = ObjectMapperUtils.getDefaultObjectMapper();

  public static Map<String, Object> parseMessage(String message) throws Throwable {
    return objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
  }

  public static String parseMap(Map<String, Object> map) throws Throwable {
    return objectMapper.writeValueAsString(map);
  }

  public static <T> String parsePojo(T data) throws Throwable {
    return objectMapper.writeValueAsString(data);
  }

  public static Map<String, Object> parseJavaSparkRow(Row row){
    Map<String, Object> featureMap = new HashMap<>();
    StructField[] fields = row.schema().fields();
    for (int i = 0; i < fields.length; i++) {
      Object data = row.get(i);
      if (data == null)
        throw new RuntimeException(
            String.format("null value for column: %s", fields[i].name()));

      data = SchemaUtils.handleJavaSparkTypes(data, fields[i].dataType());
      featureMap.put(fields[i].name(), data);
    }
    return featureMap;
  }

  public static Map<String, Object> parseSparkRow(InternalRow row, StructType schema){
    Map<String, Object> featureMap = new HashMap<>();
    StructField[] fields = schema.fields();
    for (int i = 0; i < fields.length; i++) {
      Object data = row.get(i, fields[i].dataType());
      if (data == null)
        throw new RuntimeException(
            String.format("null value for column: %s", fields[i].name()));

      data = SchemaUtils.handleSparkTypes(data, fields[i].dataType());
      featureMap.put(fields[i].name(), data);
    }
    return featureMap;
  }
}
