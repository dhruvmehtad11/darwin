package com.dream11.core.util;

import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public class JsonConversionUtils {

    public static <T> Single<T> convertJsonSingle(
            JsonObject request, TypeReference<T> clazz, ObjectMapper objectMapper, ServiceError error) {
        return Single.just(request)
                .flatMap(
                        entityRequest -> {
                            T converted;
                            try {
                                converted = objectMapper.readValue(entityRequest.toString(), clazz);
                            } catch (Exception e) {
                                return Single.error(new ApiRestException(e, error));
                            }
                            return Single.just(converted);
                        });
    }
    public static <T> Single<T> convertJsonSingle(
            JsonObject request, Class<T> clazz, ObjectMapper objectMapper, ServiceError error) {
        return Single.just(request)
                .flatMap(
                        entityRequest -> {
                            T converted;
                            try {
                                converted = objectMapper.readValue(entityRequest.toString(), clazz);
                            } catch (Exception e) {
                                return Single.error(new ApiRestException(e, error));
                            }
                            return Single.just(converted);
                        });
    }

  public static <T> Single<T> convertJsonSingle(
      String request, Class<T> clazz, ObjectMapper objectMapper, ServiceError error) {
    return Single.just(request)
        .flatMap(
            entityRequest -> {
              T converted;
              try {
                converted = objectMapper.readValue(entityRequest, clazz);
              } catch (Exception e) {
                return Single.error(new ApiRestException(e, error));
              }
              return Single.just(converted);
            });
  }

    public static <T> Maybe<T> convertJsonMaybe(
            JsonObject request, Class<T> clazz, ObjectMapper objectMapper, ServiceError error) {
        return Maybe.just(request)
                .flatMap(
                        entityRequest -> {
                            T converted;
                            try {
                                converted = objectMapper.readValue(entityRequest.toString(), clazz);
                            } catch (Exception e) {
                                return Maybe.error(new ApiRestException(e, error));
                            }
                            return Maybe.just(converted);
                        });
    }

    public static <T> Observable<T> convertJsonObservable(
            JsonObject request, Class<T> clazz, ObjectMapper objectMapper, ServiceError error) {
        return Observable.just(request)
                .flatMap(
                        entityRequest -> {
                            T converted;
                            try {
                                converted = objectMapper.readValue(entityRequest.toString(), clazz);
                            } catch (Exception e) {
                                return Observable.error(new ApiRestException(e, error));
                            }
                            return Observable.just(converted);
                        });
    }
}