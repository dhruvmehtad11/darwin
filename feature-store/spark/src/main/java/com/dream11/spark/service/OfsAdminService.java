package com.dream11.spark.service;

import static com.dream11.spark.constant.Constants.OFS_V2_ADMIN_BASE_HOST;
import static com.dream11.spark.constant.Constants.OFS_V2_ADMIN_RUN_DATA_ENDPOINT;
import static com.dream11.spark.constant.Constants.OFS_V2_ADMIN_SCHEMA_ENDPOINT;
import static com.dream11.spark.constant.Constants.OFS_V2_ADMIN_TOPIC_ENDPOINT;
import static com.dream11.spark.constant.Constants.OFS_V2_KAFKA_BASE_HOST;

import com.dream11.spark.dto.DataResponse;
import com.dream11.spark.dto.ErrorResponse;
import com.dream11.spark.dto.FeatureGroupRunDataRequest;
import com.dream11.spark.dto.GetCassandraFeatureGroupSchemaResponse;
import com.dream11.spark.dto.GetTopicResponse;
import com.dream11.spark.utils.MessageUtils;
import com.dream11.spark.utils.ObjectMapperUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

public class OfsAdminService {
  private static final String PROTO_HTTP = "http";

  private final ObjectMapper objectMapper = ObjectMapperUtils.getDefaultObjectMapper();
  private final String ofsV2AdminHost;

  @Getter private final String kafkaHost;

  public OfsAdminService(Map<String, String> properties, String teamSuffix, String vpcSuffix) {
    if (Objects.equals(System.getProperty("app.environment"), "test")) {
      this.ofsV2AdminHost = System.getProperty("ofs.admin.host");
      this.kafkaHost = System.getProperty("ofs.kafka.host");
    } else if (properties.containsKey("sdk.python.environment")
        && Objects.equals(properties.get("sdk.python.environment"), "test")) {
      this.ofsV2AdminHost = properties.get("ofs.admin.host");
      this.kafkaHost = properties.get("ofs.kafka.host");
    } else {
      this.ofsV2AdminHost = String.format(OFS_V2_ADMIN_BASE_HOST, teamSuffix, vpcSuffix);
      this.kafkaHost = String.format(OFS_V2_KAFKA_BASE_HOST, teamSuffix, vpcSuffix);
    }
  }

  public String getKafkaTopicFromAdmin(String featureGroupName) {
    try {
      String param = "name=" + featureGroupName;
      URI uri = new URI(PROTO_HTTP, ofsV2AdminHost, OFS_V2_ADMIN_TOPIC_ENDPOINT, param, null);
      HttpURLConnection connection = getConnection(uri.toURL(), "GET");

      try (InputStream inputStream =
          (connection.getResponseCode() == 200)
              ? connection.getInputStream()
              : connection.getErrorStream()) {

        if (inputStream == null) {
          throw new RuntimeException("error fetching schema from admin: no response received");
        }

        byte[] body = readAllBytes(inputStream);
        if (connection.getResponseCode() != 200) {
          processOfsError(body);
        }

        DataResponse<GetTopicResponse> responseData =
            objectMapper.readValue(body, new TypeReference<DataResponse<GetTopicResponse>>() {});
        return responseData.getData().getTopic();
      } finally {
        connection.disconnect();
      }
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("error fetching feature group topic from admin %s", e.getMessage()), e);
    }
  }

  public GetCassandraFeatureGroupSchemaResponse getSchemaFromAdmin(
      String featureGroupName, String featureGroupVersion) {
    try {
      String param = "name=" + featureGroupName;
      if (featureGroupVersion != null) {
        param += "&version=" + featureGroupVersion;
      }

      URI uri = new URI(PROTO_HTTP, ofsV2AdminHost, OFS_V2_ADMIN_SCHEMA_ENDPOINT, param, null);
      HttpURLConnection connection = getConnection(uri.toURL(), "GET");

      try (InputStream inputStream =
          (connection.getResponseCode() == 200)
              ? connection.getInputStream()
              : connection.getErrorStream()) {

        if (inputStream == null) {
          throw new RuntimeException("error fetching schema from admin: no response received");
        }

        byte[] body = readAllBytes(inputStream);
        if (connection.getResponseCode() != 200) {
          processOfsError(body);
        }

        DataResponse<GetCassandraFeatureGroupSchemaResponse> responseData =
            objectMapper.readValue(
                body, new TypeReference<DataResponse<GetCassandraFeatureGroupSchemaResponse>>() {});
        return responseData.getData();
      } finally {
        connection.disconnect();
      }
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("error fetching schema from admin %s", e.getMessage()), e);
    }
  }

  public FeatureGroupRunDataRequest putRunData(FeatureGroupRunDataRequest request) {
    try {
      URI uri = new URI(PROTO_HTTP, ofsV2AdminHost, OFS_V2_ADMIN_RUN_DATA_ENDPOINT, "", null);
      HttpURLConnection connection = getConnection(uri.toURL(), "POST");
      connection.setDoOutput(true);

      try (OutputStream outputStream = connection.getOutputStream();
          OutputStreamWriter writer =
              new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
        writer.write(MessageUtils.parsePojo(request));
        writer.flush();
      } catch (Throwable e) {
        throw new RuntimeException(
            String.format("error sending request body to admin: %s", e.getMessage()), e);
      }

      try (InputStream inputStream =
          (connection.getResponseCode() == 200)
              ? connection.getInputStream()
              : connection.getErrorStream()) {

        if (inputStream == null) {
          throw new RuntimeException("error fetching schema from admin: no response received");
        }

        byte[] body = readAllBytes(inputStream);
        if (connection.getResponseCode() != 200) {
          processOfsError(body);
        }

        DataResponse<FeatureGroupRunDataRequest> responseData =
            objectMapper.readValue(body, new TypeReference<DataResponse<FeatureGroupRunDataRequest>>() {});
        return responseData.getData();
      } finally {
        connection.disconnect();
      }
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("error posting run data admin %s", e.getMessage()), e);
    }
  }

  private void processOfsError(byte[] body) {
    ErrorResponse errorResponse;
    try {
      errorResponse = objectMapper.readValue(body, ErrorResponse.class);
    } catch (Exception e) {
      throw new RuntimeException(String.format("response: %s", new String(body)));
    }
    throw new RuntimeException(
        String.format(
            "code: %s message: %s",
            errorResponse.getError().getCode(), errorResponse.getError().getMessage()));
  }

  private HttpURLConnection getConnection(URL url, String method) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
    return connection;
  }

  private static byte[] readAllBytes(InputStream inputStream){
    try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      byte[] data = new byte[8192]; // 8 KB buffer
      int bytesRead;
      while ((bytesRead = inputStream.read(data)) != -1) {
        buffer.write(data, 0, bytesRead);
      }
      return buffer.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
