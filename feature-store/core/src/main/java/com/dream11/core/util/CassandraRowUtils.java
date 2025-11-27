package com.dream11.core.util;

import static com.dream11.core.constant.Constants.EMPTY_JSON_OBJECT;
import static com.dream11.core.util.CassandraDataTypeUtils.setJsonKeyValue;
import static com.dream11.core.util.CassandraDataTypeUtils.setJsonKeyValueWithSize;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.helper.CassandraFetchRowWithMetadata;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CassandraRowUtils {
  public static JsonObject convertCassandraRowToJson(Row row) {
    try {
      JsonObject jsonObject = new JsonObject();

      // Get the column definitions to iterate through columns
      ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

      for (ColumnDefinitions.Definition definition : columnDefinitions) {
        String columnName = definition.getName();
        Object columnValue = row.getObject(columnName);
        CassandraFeatureColumn.CassandraDataType dataType =
            CassandraFeatureColumn.CassandraDataType.valueOf(
                definition.getType().toString().toUpperCase());
        setJsonKeyValue(columnName, columnValue, jsonObject, dataType);
      }
      return jsonObject;
    } catch (Exception e) {
      log.error(e.getMessage());
      return EMPTY_JSON_OBJECT;
    }
  }

  public static CassandraFetchRowWithMetadata convertCassandraRowToJsonWithMetadata(Row row) {
    try {
      JsonObject jsonObject = new JsonObject();
      long rowSize = 0L;

      // Get the column definitions to iterate through columns
      ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

      for (ColumnDefinitions.Definition definition : columnDefinitions) {
        String columnName = definition.getName();
        Object columnValue = row.getObject(columnName);
        CassandraFeatureColumn.CassandraDataType dataType =
            CassandraFeatureColumn.CassandraDataType.valueOf(
                definition.getType().toString().toUpperCase());
        rowSize += setJsonKeyValueWithSize(columnName, columnValue, jsonObject, dataType);
      }
      return CassandraFetchRowWithMetadata.builder().row(jsonObject).size(rowSize).build();
    } catch (Exception e) {
      log.error(e.getMessage());
      return CassandraFetchRowWithMetadata.builder().row(EMPTY_JSON_OBJECT).size(0L).build();
    }
  }

  public static List<Tuple> getEntityFeaturesTupleList(
      String entityName, List<CassandraFeatureColumn> cassandraFeatureColumns) {
    List<Tuple> tuples = new ArrayList<>();
    for (CassandraFeatureColumn featureColumn : cassandraFeatureColumns) {
      JsonArray tags = new JsonArray(featureColumn.getTags());
      tuples.add(
          Tuple.of(
              entityName,
              featureColumn.getFeatureName(),
              featureColumn.getFeatureDataType(),
              featureColumn.getDescription(),
              tags));
    }
    return tuples;
  }

  public static List<Tuple> getFeatureGroupFeaturesTupleList(
      String featureGroupName,
      String featureGroupVersion,
      List<CassandraFeatureColumn> cassandraFeatureColumns) {
    List<Tuple> tuples = new ArrayList<>();
    for (CassandraFeatureColumn featureColumn : cassandraFeatureColumns) {
      JsonArray tags = new JsonArray(featureColumn.getTags());
      tuples.add(
          Tuple.of(
              featureGroupName,
              featureGroupVersion,
              featureColumn.getFeatureName(),
              featureColumn.getFeatureDataType(),
              featureColumn.getDescription(),
              tags));
    }
    return tuples;
  }
}
