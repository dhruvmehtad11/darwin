package com.dream11.spark.MockServers;

import static com.dream11.spark.constants.ExpectedDataPath.OfsMockDataPath;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

public class OfsMock {
  public static final JsonObject serverMocks;

  static {
    serverMocks = JsonParser.parseString(getFileBuffer(OfsMockDataPath)).getAsJsonObject();
  }

  public static void mockServer(WireMockServer server) {
    serverMocks.entrySet().forEach(entry -> {
      String mockName = entry.getKey();
      JsonElement value = entry.getValue();
      JsonObject mockDetails = value.getAsJsonObject();

      String endpoint = mockDetails.get("endpoint").getAsString();
      String method = mockDetails.get("method").getAsString();
      int status = mockDetails.get("status").getAsInt();
      String responseBody = mockDetails.get("body").toString();
      String requestBody = null;

      if (mockDetails.get("requestBody") != null) {
        requestBody = mockDetails.get("requestBody").toString();
      }
      Map<String, String> params = new HashMap<>();
      if (mockDetails.get("params") != null) {
        params = mockDetails.get("params").getAsJsonObject()
            .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, r -> r.getValue().getAsString()));
      }
      applyMock(server, mockName, endpoint, method, status, requestBody, responseBody, mockDetails.get("params") != null, params);
    });
  }

  private static void applyMock(WireMockServer server, String mockName, String endpoint, String method, int status, String requestBody,
                                String responseBody, boolean params, Map<String, String> paramMap) {
    if (params) {
      if (requestBody == null) {
        MappingBuilder mappingBuilder = WireMock.request(method, WireMock.urlPathMatching(endpoint));
        for(Map.Entry<String, String> entry:paramMap.entrySet()){
          mappingBuilder.withQueryParam(entry.getKey(), WireMock.equalTo(entry.getValue()));
        }
        server.stubFor(mappingBuilder
            .willReturn(WireMock.aResponse().withStatus(status).withBody(responseBody)));
      } else {

        MappingBuilder mappingBuilder = WireMock.request(method, WireMock.urlPathMatching(endpoint))
            .withRequestBody(WireMock.equalToJson(requestBody, true, false));
        for(Map.Entry<String, String> entry:paramMap.entrySet()){
          mappingBuilder.withQueryParam(entry.getKey(), WireMock.equalTo(entry.getValue()));
        }
        server.stubFor(mappingBuilder
            .willReturn(WireMock.aResponse().withStatus(status).withBody(responseBody)));
      }
    } else if (requestBody == null) {
      server.stubFor(WireMock.request(method, WireMock.urlMatching(endpoint))
          .willReturn(WireMock.aResponse()
              .withStatus(status)
              .withBody(responseBody)));
    } else {
      server.stubFor(WireMock.request(method, WireMock.urlMatching(endpoint))
          .withRequestBody(WireMock.equalToJson(requestBody, true, false))
          .willReturn(WireMock.aResponse()
              .withStatus(status)
              .withBody(responseBody)));
    }
  }

  @SneakyThrows
  public static String getFileBuffer(String path) {
    return IOUtils.toString(new FileReader(path));
  }
}
