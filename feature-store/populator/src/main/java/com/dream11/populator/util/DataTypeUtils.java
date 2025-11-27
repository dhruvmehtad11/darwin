package com.dream11.populator.util;

import io.delta.kernel.data.Row;
import io.delta.kernel.types.BinaryType;
import io.delta.kernel.types.BooleanType;
import io.delta.kernel.types.DataType;
import io.delta.kernel.types.DecimalType;
import io.delta.kernel.types.DoubleType;
import io.delta.kernel.types.FloatType;
import io.delta.kernel.types.IntegerType;
import io.delta.kernel.types.LongType;
import io.delta.kernel.types.StringType;
import io.delta.kernel.types.StructField;
import io.delta.kernel.types.StructType;
import io.delta.kernel.types.TimestampType;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataTypeUtils {
  public static Map<String, Object> convertRowToJsonObject(Row row, StructType readSchema) {
    Set<String> readColumns = new HashSet<>(readSchema.fieldNames());

    StructType rowType = row.getSchema();
    Map<String, Object> rowObject = new HashMap<>();
    for (int index = 0; index < rowType.length(); index++) {
      StructField field = rowType.at(index);
      DataType fieldType = field.getDataType();
      String name = field.getName();

      // to avoid metadata columns
      if(!readColumns.contains(name))
        continue;

      if (row.isNullAt(index)) {
        rowObject.put(name, null);
        continue;
      }

      Object value;
      if (fieldType instanceof BooleanType) {
        value = row.getBoolean(index);
      } else if (fieldType instanceof BinaryType) {
        value = Base64.getEncoder().encode(row.getBinary(index));
      } else if (fieldType instanceof IntegerType) {
        value = row.getInt(index);
      } else if (fieldType instanceof LongType) {
        value = row.getLong(index);
      } else if (fieldType instanceof FloatType) {
        value = row.getFloat(index);
      } else if (fieldType instanceof DoubleType) {
        value = row.getDouble(index);
      } else if (fieldType instanceof TimestampType) {
        // app layer handles in millis or TZ string
        value = row.getLong(index) / 1000;
      } else if (fieldType instanceof StringType) {
        value = row.getString(index);
      } else if (fieldType instanceof DecimalType
          && ((DecimalType) fieldType).getPrecision() == 38
          && ((DecimalType) fieldType).getScale() == 0) {
        value = row.getDecimal(index).toBigInteger();
      } else if (fieldType instanceof DecimalType
          && ((DecimalType) fieldType).getPrecision() == 38
          && ((DecimalType) fieldType).getScale() == 24) {
        value = row.getDecimal(index);
      } else {
        throw new RuntimeException(String.format("data type not not supported %s", fieldType));
      }

      rowObject.put(name, value);
    }

    return rowObject;

    // public static Object handleSparkTypes(Object data, DataType dataType) {
    //    switch (dataType.typeName()) {
    //      case "string":
    //        return ((UTF8String) data).toString();
    //      case "integer":
    //        return ((Integer) data);
    //      case "long":
    //        return ((Long) data);
    //      case "float":
    //        return ((Float) data);
    //      case "double":
    //        return ((Double) data);
    //      case "boolean":
    //        return ((Boolean) data);
    //      case "binary":
    //        // returns base64 string
    //        return Base64.getEncoder().encode((byte[]) data);
    //      case "timestamp":
    //        // returns epoch in millis
    //        return ((Long) data) / 1000;
    //      case "decimal(38,0)":
    //        return ((org.apache.spark.sql.types.Decimal) data).toJavaBigInteger();
    //      case "decimal(38,24)":
    //        return ((org.apache.spark.sql.types.Decimal) data).toJavaBigDecimal();
    //      default:
    //        return data;
    //    }
    //  }

  }

  public static Map<String, String> parseSchema(StructType schema){
    Map<String, String> schemaMap = new HashMap<>();
    for(StructField field:schema.fields()){
      schemaMap.put(field.getName(), field.getDataType().toString());
    }
    return schemaMap;
  }
}
