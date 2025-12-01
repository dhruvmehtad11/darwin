package com.dream11.spark.utils;

import com.dream11.spark.dto.CassandraFeatureColumn;
import com.dream11.spark.dto.GetCassandraFeatureGroupSchemaResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.unsafe.types.UTF8String;

public class SchemaUtils {

  public static StructType getSparkSchemaForFg(GetCassandraFeatureGroupSchemaResponse response) {
    List<StructField> fields = new ArrayList<>();
    Set<String> primaryKeysSet = new HashSet<>(response.getPrimaryKeys());

    for (CassandraFeatureColumn featureColumn : response.getSchema()) {
      // explicit check for legacy schema DO NOT REMOVE
      if (Objects.equals(featureColumn.getFeatureName(), "fg_version")) continue;

      // app layer does not allow null values
      boolean isNullable = false;
      boolean isPrimary = primaryKeysSet.contains(featureColumn.getFeatureName());
      fields.add(
          DataTypes.createStructField(
              featureColumn.getFeatureName(),
              getSparkDataType(featureColumn.getFeatureDataType()),
              isNullable,
              Metadata.fromJson(String.format("{\"isPrimary\": %s }", isPrimary))));
    }
    return DataTypes.createStructType(fields);
  }

  private static DataType getSparkDataType(CassandraFeatureColumn.CassandraDataType dataType) {
    switch (dataType.name()) {
      case "TEXT":
      case "ASCII":
      case "UUID":
      case "TIMEUUID":
      case "INET":
      case "VARCHAR":
        return DataTypes.StringType;
        // special case: converts to base64 encoded string
      case "BLOB":
        return DataTypes.BinaryType;
      case "BOOLEAN":
        return DataTypes.BooleanType;
        // special case: converts to string and handled via big decimal
      case "VARINT":
        // handles precision of 38 digits(max)
        return DataTypes.createDecimalType(38, 0);
        // special case: converts to string and handled via big decimal
      case "DECIMAL":
        // handles precision of 38 digits(max) and scale of 24 digits
        // basically 24 digits after decimal
        return DataTypes.createDecimalType(38, 24);
      case "DOUBLE":
        return DataTypes.DoubleType;
      case "FLOAT":
        return DataTypes.FloatType;
      case "INT":
        return DataTypes.IntegerType;
      case "BIGINT":
        return DataTypes.LongType;
        // special case: converts to long for epoch
      case "TIMESTAMP":
        return DataTypes.TimestampType;
      default:
        throw new IllegalArgumentException("Unsupported Spark data type: " + dataType.name());
    }
  }

  public static void validateSchema(StructType expected, StructType actual) {
    Map<String, StructField> expectedMap = new HashMap<>();
    Set<String> primaryKeys = new HashSet<>();
    for (StructField field : expected.fields()) {
      expectedMap.put(field.name(), field);
      if (!field.nullable()) primaryKeys.add(field.name());
    }

    for (StructField field : actual.fields()) {
      if (!expectedMap.containsKey(field.name()))
        throw new RuntimeException(String.format("unknown field in schema: %s", field.name()));

      StructField expectedField = expectedMap.get(field.name());
      if (!field.dataType().equals(expectedField.dataType()))
        throw new RuntimeException(
            String.format(
                "type mismatch for column: %s expected: %s actual: %s",
                field.name(), field.dataType(), expectedField.dataType()));

      if (!expectedField.nullable() && field.nullable())
        throw new RuntimeException(
            String.format("field marked as nullable for non-nullable field: %s", field.name()));

      primaryKeys.remove(field.name());
    }
    // not all primary keys are present
    if (!primaryKeys.isEmpty())
      throw new RuntimeException(String.format("primary keys missing in schema: %s", primaryKeys));
  }

  public static Object handleSparkTypes(Object data, DataType dataType) {
    switch (dataType.typeName()) {
      case "string":
        return ((UTF8String) data).toString();
      case "integer":
        return ((Integer) data);
      case "long":
        return ((Long) data);
      case "float":
        return ((Float) data);
      case "double":
        return ((Double) data);
      case "boolean":
        return ((Boolean) data);
      case "binary":
        // returns base64 string
        return Base64.getEncoder().encode((byte[]) data);
      case "timestamp":
        // returns epoch in millis
        return ((Long) data) / 1000;
      case "decimal(38,0)":
        return ((org.apache.spark.sql.types.Decimal) data).toJavaBigInteger();
      case "decimal(38,24)":
        return ((org.apache.spark.sql.types.Decimal) data).toJavaBigDecimal();
      default:
        return data;
    }
  }

  public static Object handleJavaSparkTypes(Object data, DataType dataType) {
    switch (dataType.typeName()) {
      case "string":
        return data.toString();
      case "integer":
        return ((Integer) data);
      case "long":
        return ((Long) data);
      case "float":
        return ((Float) data);
      case "double":
        return ((Double) data);
      case "boolean":
        return ((Boolean) data);
      case "binary":
        // returns base64 string
        return Base64.getEncoder().encode((byte[]) data);
      case "timestamp":
        // returns epoch in millis
        return ((java.sql.Timestamp) data).getTime() / 1000;
      case "decimal(38,0)":
        return ((BigDecimal) data).toBigInteger();
      case "decimal(38,24)":
        return (BigDecimal) data;
      default:
        return data;
    }
  }
}
