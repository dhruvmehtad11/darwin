package com.dream11.core.util;

import com.dream11.core.dto.helper.Data;
import com.dream11.core.dto.helper.ErrorResponse;
import com.dream11.core.dto.helper.LegacyFeatureStoreResponse;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import static com.dream11.core.constant.Constants.DATA_STRING_TYPE_REFERENCE;
import static com.dream11.core.error.ServiceError.OFS_V2_ADMIN_SERVICE_EXCEPTION;
import static com.dream11.core.error.ServiceError.OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION;
import static com.dream11.core.util.JsonConversionUtils.convertJsonSingle;

@Slf4j
public class HttpResponseUtils {
  public static <T> Single<T> checkStatusAndConvert(
      HttpResponse<Buffer> response,
      TypeReference<Data<T>> clazz,
      ObjectMapper objectMapper,
      ServiceError error) {
    if (response.statusCode() != 200) {
      return convertJsonSingle(response.bodyAsJsonObject(), ErrorResponse.class, objectMapper, error)
          .onErrorResumeNext(Single.error(
              new ApiRestException(
                  response.statusMessage(), OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION, response.statusCode())))
          .flatMap(e -> Single.error(new ApiRestException(e.getError().getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION)))
          .map(ignore -> {
            throw new ApiRestException(
                response.statusMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION, response.statusCode()); // place holder
          });
    }
    return convertJsonSingle(response.bodyAsJsonObject(), clazz, objectMapper, error)
        .map(Data::getData);
  }

  public static <T> Single<LegacyFeatureStoreResponse<T>> checkStatusAndConvertLegacyResponse(
      HttpResponse<Buffer> response,
      TypeReference<LegacyFeatureStoreResponse<T>> clazz,
      ObjectMapper objectMapper,
      ServiceError error) {
    if (response.statusCode() != 200) {
      return convertJsonSingle(response.bodyAsJsonObject(), ErrorResponse.class, objectMapper, error)
          .onErrorResumeNext(Single.error(
              new ApiRestException(
                  response.statusMessage(), OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION, response.statusCode())))
          .flatMap(e -> Single.error(new ApiRestException(e.getError().getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION)))
          .map(ignore -> {
            throw new ApiRestException(
                response.statusMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION, response.statusCode()); // place holder
          });
    }
    return convertJsonSingle(response.bodyAsJsonObject(), clazz, objectMapper, error);
  }

  public static <T> Single<T> checkStatusAndConvertCompressed(
      HttpResponse<Buffer> response,
      Class<T> clazz,
      ObjectMapper objectMapper,
      ServiceError error,
      Vertx vertx,
      Boolean compressedResponse) {
    if (response.statusCode() != 200) {
      return convertJsonSingle(response.bodyAsJsonObject(), ErrorResponse.class, objectMapper, error)
          .onErrorResumeNext(Single.error(
              new ApiRestException(
                  response.statusMessage(), OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION, response.statusCode())))
          .flatMap(e -> Single.error(new ApiRestException(e.getError().getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION)))
          .map(ignore -> {
            throw new ApiRestException(
                response.statusMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION, response.statusCode()); // place holder
          });
    }
    if(!compressedResponse)
      return convertJsonSingle(response.bodyAsJsonObject(), DATA_STRING_TYPE_REFERENCE, objectMapper, error)
          .flatMap(res -> convertJsonSingle(res.getData(), clazz, objectMapper, error));

    return convertJsonSingle(response.bodyAsJsonObject(), DATA_STRING_TYPE_REFERENCE, objectMapper, error)
        .flatMap(r -> CompressionUtils.decompressNonBlocking(vertx, r.getData()))
        .flatMap(res -> convertJsonSingle(res, clazz, objectMapper, error));
  }
}
