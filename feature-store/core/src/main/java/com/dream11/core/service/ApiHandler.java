package com.dream11.core.service;

import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Handles the API request and response. */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class ApiHandler {

  /** The web client. */
  private final WebClient d11WebClient;

  /**
   * Calls an API with default query params.
   *
   * @param host the API host
   * @param endpoint the API endpoint
   * @param reqType the request type
   * @param body the request body
   * @param headers the request headers
   * @return the API response
   * @throws ApiRestException when an exception occurs while sending the request
   */
  public Single<HttpResponse<Buffer>> call(
      String host,
      String endpoint,
      RequestType reqType,
      JsonObject body,
      Map<String, String> headers)
      throws ApiRestException {
    return call(host, endpoint, reqType, body, headers, null);
  }

  /**
   * Calls an API with specified query params.
   *
   * @param host the API host
   * @param endpoint the API endpoint
   * @param reqType the request type
   * @param body the request body
   * @param headers the request headers
   * @param queryParams the query parameters
   * @return the API response
   * @throws ApiRestException when an exception occurs while sending the request
   */
  public Single<HttpResponse<Buffer>> call(
      String host,
      String endpoint,
      RequestType reqType,
      JsonObject body,
      Map<String, String> headers,
      Map<String, String> queryParams) {

    return Single.just(createRequest(host, endpoint, reqType))
        .map(
            req -> {
              addHeaders(req, headers);
              if (queryParams != null) {
                addQueryParams(req, queryParams);
              }
              return req;
            })
        .flatMap(
            req ->
                req.rxSendJsonObject(body)
                    .doOnError(
                        e -> {
                          throw new ApiRestException(
                              e.getMessage(), ServiceError.WEB_CLIENT_EXCEPTION);
                        }));
  }

  private HttpRequest<Buffer> createRequest(String host, String endpoint, RequestType reqType) {
    switch (reqType) {
      case POST:
        return d11WebClient.getWebClient().postAbs(host + endpoint);
      case PUT:
        return d11WebClient.getWebClient().putAbs(host + endpoint);
      default:
        return d11WebClient.getWebClient().getAbs(host + endpoint);
    }
  }

  private void addHeaders(HttpRequest<Buffer> request, Map<String, String> headers) {
    headers.forEach(request::putHeader);
  }

  private void addQueryParams(HttpRequest<Buffer> request, Map<String, String> queryParams) {
    queryParams.forEach(request::addQueryParam);
  }

  /** The request types. */
  public enum RequestType {
    GET,
    POST,
    PUT
  }
}
