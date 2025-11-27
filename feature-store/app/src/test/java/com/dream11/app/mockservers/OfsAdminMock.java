package com.dream11.app.mockservers;

import static com.dream11.app.constants.ExpectedDataPath.OfsAdminMockDataPath;
import static com.dream11.core.util.Utils.getFileBuffer;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.vertx.core.json.JsonObject;

public class OfsAdminMock {
  public static final JsonObject serverMocks = new JsonObject(getFileBuffer(OfsAdminMockDataPath));

  public static void mockServer(WireMockServer server) {
    serverMocks.forEach(entry -> {
      String mockName = entry.getKey();
      JsonObject mockDetails = (JsonObject) entry.getValue();

      String endpoint = mockDetails.getString("endpoint");
      String method = mockDetails.getString("method");
      int status = mockDetails.getInteger("status");
      String responseBody = mockDetails.getJsonObject("body").toString();
      String requestBody = null;

      if (mockDetails.containsKey("requestBody")) {
        requestBody = mockDetails.getJsonObject("requestBody").toString();
      }

      applyMock(server, mockName, endpoint, method, status, requestBody, responseBody, mockDetails.containsKey("params"));
    });
  }

  private static void applyMock(WireMockServer server, String mockName, String endpoint, String method, int status, String requestBody,
                                String responseBody, boolean params) {
    if (params) {
      if (requestBody == null) {
        server.stubFor(WireMock.request(method, WireMock.urlPathMatching(endpoint))
            .willReturn(WireMock.aResponse()
                .withStatus(status)
                .withBody(responseBody)));
      } else {
        server.stubFor(WireMock.request(method, WireMock.urlPathMatching(endpoint))
            .withRequestBody(WireMock.equalTo(requestBody))
            .willReturn(WireMock.aResponse()
                .withStatus(status)
                .withBody(responseBody)));
      }
    } else if (requestBody == null) {
      server.stubFor(WireMock.request(method, WireMock.urlMatching(endpoint))
          .willReturn(WireMock.aResponse()
              .withStatus(status)
              .withBody(responseBody)));
    } else {
      server.stubFor(WireMock.request(method, WireMock.urlMatching(endpoint))
          .withRequestBody(WireMock.equalTo(requestBody))
          .willReturn(WireMock.aResponse()
              .withStatus(status)
              .withBody(responseBody)));
    }
  }
}
