package com.dream11.core.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class MysqlUtils {

  public static String convertStringToMysqlSearchPattern(String pattern) {
    return String.format("%%%s%%", pattern);
  }

  @SneakyThrows
  public static JsonArray rowSetToJson(RowSet<Row> rows) {
    List<String> columnNames = rows.columnsNames();
    JsonArray jsonArray = new JsonArray();
    for (Row row : rows) {
      JsonObject jsonObject = new JsonObject();
      for (String column : columnNames) {
        Object value = row.getValue(column);
        if (value != null && value.getClass() == LocalDateTime.class) {
          jsonObject.put(column, value.toString());
        } else {
          jsonObject.put(column, value);
        }
      }
      jsonArray.add(jsonObject);
    }
    return jsonArray;
  }

  @SneakyThrows
  public static List<JsonObject> rowSetToJsonList(RowSet<Row> rows) {
    List<String> columnNames = rows.columnsNames();
    List<JsonObject> jsonArray = new ArrayList<>();
    for (Row row : rows) {
      JsonObject jsonObject = new JsonObject();
      for (String column : columnNames) {
        Object value = row.getValue(column);
        if (value != null && value.getClass() == LocalDateTime.class) {
          jsonObject.put(column, value.toString());
        } else {
          jsonObject.put(column, value);
        }
      }
      jsonArray.add(jsonObject);
    }
    return jsonArray;
  }
}
