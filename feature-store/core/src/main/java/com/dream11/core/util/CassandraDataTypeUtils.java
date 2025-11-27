package com.dream11.core.util;

import com.datastax.driver.core.BoundStatement;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.NotImplementedException;

public class CassandraDataTypeUtils {
  public static BoundStatement setBoundStatementValue(BoundStatement boundStatement, int index, Object value,
                                                      CassandraFeatureColumn.CassandraDataType dataType)
      throws UnknownHostException, ClassCastException, ParseException, UnsupportedEncodingException {
    switch (dataType) {
      case TEXT:
      case VARCHAR:
        // Check if the string can be encoded as UTF-8
        byte[] utf8Bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
        return boundStatement.setString(index, (String) value);
      case ASCII:
        if (((String) value).matches("\\A\\p{ASCII}*\\z")) {
          return boundStatement.setString(index, (String) value);
        } else {
          throw new IllegalArgumentException("Non-ASCII characters found in ASCII string: " + value);
        }
      case BLOB:
        //      case JSON:
        return boundStatement.setBytes(index,
            ByteBuffer.wrap(Base64.getDecoder().decode(value.toString()))); // only base64 encoded byte strings allowed
      case BOOLEAN:
        if (value instanceof String && (value.equals("true") || value.equals("false"))) {
          return boundStatement.setBool(index, Boolean.parseBoolean(value.toString()));
        } else if (value instanceof Boolean) {
          return boundStatement.setBool(index, (Boolean) value);
        } else {
          throw new IllegalArgumentException("Value is not a valid boolean representation: " + value);
        }
      case DECIMAL:
        return boundStatement.setDecimal(index, new BigDecimal(value.toString()));
      case DOUBLE:
        return boundStatement.setDouble(index, Double.parseDouble(value.toString()));
      case FLOAT:
        return boundStatement.setFloat(index, Float.parseFloat(value.toString()));
      case INT:
        return boundStatement.setInt(index, Integer.parseInt(value.toString()));
      case BIGINT:
        return boundStatement.setLong(index, Long.parseLong(value.toString()));
      case TIMESTAMP:
        if (value instanceof Long) {
          // Value is already in epoch format
          return boundStatement.setTimestamp(index, new Date((long) value));
        } else {
          // Try parsing the value as a date format
          String timestampString = value.toString();
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
          Date date = dateFormat.parse(timestampString);
          return boundStatement.setTimestamp(index, date);
        }
      case TIMEUUID:
      case UUID:
        return boundStatement.setUUID(index, UUID.fromString(value.toString()));
      case INET:
        return boundStatement.setInet(index, InetAddress.getByName(value.toString()));
      case VARINT:
        return boundStatement.setVarint(index, new BigInteger(value.toString()));
      //      case LIST:
      //        return boundStatement.setList(index, (List<?>) value);
      //      case SET:
      //        return boundStatement.setSet(index, (Set<?>) value);
      //      case MAP:
      //        return boundStatement.setMap(index, (Map<?, ?>) value);
      //      case TUPLE:
      //        return boundStatement.setTupleValue(index, (TupleValue) value);
      //      case UDT:
      //        return boundStatement.setUDTValue(index, (UDTValue) value);
      default:
        throw new NotImplementedException(String.format("Not implemented for data type %s", dataType));
    }
  }

  public static void setJsonKeyValue(String key, Object value, JsonObject jsonObject, CassandraFeatureColumn.CassandraDataType dataType) {
    if (value == null) {
      jsonObject.put(key, (Object) null);
      return;
    }
    switch (dataType) {
      case TEXT:
      case ASCII:
      case VARCHAR:
        jsonObject.put(key, (String) value);
        break;
      case BLOB:
        jsonObject.put(key, Base64.getEncoder().encodeToString(((ByteBuffer) value).array()));
        break;
      case BOOLEAN:
        jsonObject.put(key, (Boolean) value);
        break;
      case DECIMAL:
        jsonObject.put(key, ((BigDecimal) value).doubleValue());
        break;
      case DOUBLE:
        jsonObject.put(key, (Double) value);
        break;
      case FLOAT:
        jsonObject.put(key, (Float) value);
        break;
      case INT:
        jsonObject.put(key, (Integer) value);
        break;
      case BIGINT:
        jsonObject.put(key, (Long) value);
        break;
      case TIMESTAMP:
        jsonObject.put(key, ((Date) value).getTime());
        break;
      case TIMEUUID:
      case UUID:
        jsonObject.put(key, value.toString());
        break;
      case INET:
        jsonObject.put(key, ((InetAddress) value).getHostAddress());
        break;
      case VARINT:
        jsonObject.put(key, ((BigInteger) value).longValue());
        break;
      default:
        throw new IllegalArgumentException("Invalid data type");
    }
  }

  public static long setBoundStatementValueWithSize(BoundStatement boundStatement, int index,
                                                                              Object value,
                                                                              CassandraFeatureColumn.CassandraDataType dataType)
      throws UnknownHostException, ParseException, UnsupportedEncodingException {
    byte[] bytes;
    switch (dataType) {
      case TEXT:
      case VARCHAR:
      case ASCII:
        bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
        boundStatement.setString(index, (String) value);
        break;
      case BLOB:
        bytes = Base64.getDecoder().decode(value.toString());
        boundStatement.setBytes(index, ByteBuffer.wrap(bytes));
        break;
      case BOOLEAN:
        boolean boolValue;
        if (value instanceof String && (value.equals("true") || value.equals("false"))) {
          boolValue = Boolean.parseBoolean(value.toString());
        } else if (value instanceof Boolean) {
          boolValue = (Boolean) value;
        } else {
          throw new IllegalArgumentException("Value is not a valid boolean representation: " + value);
        }
        boundStatement.setBool(index, boolValue);
        bytes = new byte[]{(byte) (boolValue ? 1 : 0)};
        break;
      case DECIMAL:
        bytes = ((BigDecimal) value).unscaledValue().toByteArray();
        boundStatement.setDecimal(index, new BigDecimal(value.toString()));
        break;
      case DOUBLE:
        ByteBuffer doubleBuffer = ByteBuffer.allocate(Double.BYTES);
        doubleBuffer.putDouble(Double.parseDouble(value.toString()));
        bytes = doubleBuffer.array();
        boundStatement.setDouble(index, Double.parseDouble(value.toString()));
        break;
      case FLOAT:
        ByteBuffer floatBuffer = ByteBuffer.allocate(Float.BYTES);
        floatBuffer.putFloat(Float.parseFloat(value.toString()));
        bytes = floatBuffer.array();
        boundStatement.setFloat(index, Float.parseFloat(value.toString()));
        break;
      case INT:
        ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
        intBuffer.putInt(Integer.parseInt(value.toString()));
        bytes = intBuffer.array();
        boundStatement.setInt(index, Integer.parseInt(value.toString()));
        break;
      case BIGINT:
        ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        longBuffer.putLong(Long.parseLong(value.toString()));
        bytes = longBuffer.array();
        boundStatement.setLong(index, Long.parseLong(value.toString()));
        break;
      case TIMESTAMP:
        long timestamp;
        if (value instanceof Long) {
          timestamp = (Long) value;
        } else {
          String timestampString = value.toString();
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
          Date date = dateFormat.parse(timestampString);
          timestamp = date.getTime();
        }
        ByteBuffer timestampBuffer = ByteBuffer.allocate(Long.BYTES);
        timestampBuffer.putLong(timestamp);
        bytes = timestampBuffer.array();
        boundStatement.setTimestamp(index, new Date(timestamp));
        break;
      case UUID:
      case TIMEUUID:
        UUID uuid = UUID.fromString(value.toString());
        ByteBuffer uuidBuffer = ByteBuffer.wrap(new byte[16]);
        uuidBuffer.putLong(uuid.getMostSignificantBits());
        uuidBuffer.putLong(uuid.getLeastSignificantBits());
        bytes = uuidBuffer.array();
        boundStatement.setUUID(index, uuid);
        break;
      case INET:
        InetAddress inet = InetAddress.getByName(value.toString());
        bytes = inet.getAddress();
        boundStatement.setInet(index, inet);
        break;
      case VARINT:
        bytes = new BigInteger(value.toString()).toByteArray();
        boundStatement.setVarint(index, new BigInteger(value.toString()));
        break;
      default:
        throw new UnsupportedOperationException(String.format("Not implemented for data type %s", dataType));
    }

    return bytes.length;
  }

  public static long setJsonKeyValueWithSize(String key, Object value, JsonObject jsonObject,
                                                     CassandraFeatureColumn.CassandraDataType dataType) {
    long size; // Initialize size variable

    if (value == null) {
      jsonObject.put(key, (Object) null);
      return 0;
    }

    switch (dataType) {
      case TEXT:
      case ASCII:
      case VARCHAR:
        String strValue = (String) value;
        jsonObject.put(key, strValue);
        size = strValue.getBytes(StandardCharsets.UTF_8).length; // Size in bytes
        break;
      case BLOB:
        byte[] blobBytes = ((ByteBuffer) value).array();
        jsonObject.put(key, Base64.getEncoder().encodeToString(blobBytes));
        size = blobBytes.length; // Size in bytes
        break;
      case BOOLEAN:
        jsonObject.put(key, (Boolean) value);
        size = 1; // Size of a boolean
        break;
      case DECIMAL:
        jsonObject.put(key, ((BigDecimal) value).doubleValue());
        size = ((BigDecimal) value).unscaledValue().toByteArray().length; // Size in bytes
        break;
      case DOUBLE:
        jsonObject.put(key, (Double) value);
        size = Double.BYTES; // Size of double
        break;
      case FLOAT:
        jsonObject.put(key, (Float) value);
        size = Float.BYTES; // Size of float
        break;
      case INT:
        jsonObject.put(key, (Integer) value);
        size = Integer.BYTES; // Size of int
        break;
      case BIGINT:
        jsonObject.put(key, (Long) value);
        size = Long.BYTES; // Size of long
        break;
      case TIMESTAMP:
        jsonObject.put(key, ((Date) value).getTime());
        size = Long.BYTES; // Size of timestamp
        break;
      case TIMEUUID:
      case UUID:
        String uuidString = value.toString();
        jsonObject.put(key, uuidString);
        size = uuidString.getBytes(StandardCharsets.UTF_8).length; // Size in bytes
        break;
      case INET:
        String inetAddress = ((InetAddress) value).getHostAddress();
        jsonObject.put(key, inetAddress);
        size = inetAddress.getBytes(StandardCharsets.UTF_8).length; // Size in bytes
        break;
      case VARINT:
        size = ((BigInteger) value).toByteArray().length; // Size in bytes
        jsonObject.put(key, ((BigInteger) value).longValue());
        break;
      default:
        throw new IllegalArgumentException("Invalid data type");
    }

    return size;
  }


  public static Map<String, CassandraFeatureColumn.CassandraDataType> getFeatureDataTypeMap(List<CassandraFeatureColumn> columns,
                                                                                            Boolean lowercaseSchema) {
    Map<String, CassandraFeatureColumn.CassandraDataType> dataTypeMap = new HashMap<>();
    for (CassandraFeatureColumn column : columns) {
      if (!lowercaseSchema) {
        if (dataTypeMap.containsKey(column.getFeatureName())) {
          throw new RuntimeException("duplicates in column names");
        }
        dataTypeMap.put(column.getFeatureName(), column.getFeatureDataType());
      } else {
        if (dataTypeMap.containsKey(column.getFeatureName().toLowerCase())) {
          throw new RuntimeException("duplicates in column names");
        }
        dataTypeMap.put(column.getFeatureName().toLowerCase(), column.getFeatureDataType());
      }
    }
    return dataTypeMap;
  }
}
