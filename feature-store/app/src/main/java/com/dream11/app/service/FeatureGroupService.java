package com.dream11.app.service;

import static com.dream11.core.constant.Constants.*;

import com.dream11.app.error.LegacyStackException;
import com.dream11.core.constant.Constants;
import com.dream11.core.dto.helper.LegacyFeatureStoreResponse;
import com.dream11.core.dto.helper.LegacyReadHelper;
import com.dream11.core.dto.helper.MultiBulkReadResponseHelper;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKey;
import com.dream11.core.dto.request.ReadCassandraFeaturesRequest;
import com.dream11.core.dto.request.legacystack.*;
import com.dream11.core.dto.response.*;
import com.dream11.core.dto.response.interfaces.*;
import com.dream11.core.error.ApiRestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FeatureGroupService {

  private final CassandraFeatureStoreService cassandraFeatureStoreService;
  private final CassandraOfsV2AdminService cassandraOfsV2AdminService;
  private final FeatureGroupMetricService featureGroupMetricService;
  private final ObjectMapper objectMapper;

  public Single<WriteFeaturesResponse> writeFeaturesV2(
      JsonObject writeFeaturesRequest,
      Constants.FeatureStoreType featureStoreType,
      Boolean replicationEnabled) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService
            .writeFeatures(replicationEnabled, writeFeaturesRequest)
            .map(r -> r);
    }
  }

  public Single<WriteBulkFeaturesResponse> writeFeaturesV1(
      JsonObject writeFeaturesRequest,
      Constants.FeatureStoreType featureStoreType,
      Boolean replicationEnabled) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService
            .writeFeaturesBulk(replicationEnabled, writeFeaturesRequest)
            .map(r -> r);
    }
  }

  public Single<ReadFeaturesResponse> readFeatures(
      JsonObject readFeaturesRequest, Constants.FeatureStoreType featureStoreType) {
    long startTime = System.currentTimeMillis();
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService
            .readFeatures(readFeaturesRequest)
            .doOnSuccess(
                r ->
                    featureGroupMetricService.registerCassandraFeatureGroupReadLatency(
                        CassandraFeatureGroupCacheKey.builder()
                            .name(r.getFeatureGroupName())
                            .version(r.getFeatureGroupVersion())
                            .build(),
                        System.currentTimeMillis() - startTime))
            .map(r -> r);
    }
  }

  public Single<ReadCassandraPartitionResponse> readFeaturePartition(
      JsonObject readFeaturesRequest, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService.readPartition(readFeaturesRequest);
    }
  }

  public Single<List<ReadFeaturesResponse>> multiReadFeatures(
      JsonObject readFeaturesRequest, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService.multiReadFeatures(readFeaturesRequest);
    }
  }

  public Single<LegacyFeatureStoreResponse<LegacyBatchWriteResponse>> legacyBatchWriteFeatures(
      LegacyBatchWriteFeaturesRequest legacyBatchWriteFeaturesRequest) {
    return cassandraFeatureStoreService
        .legacyBatchWriteFeatures(legacyBatchWriteFeaturesRequest)
        .map(
            r ->
                LegacyFeatureStoreResponse.<LegacyBatchWriteResponse>builder()
                    .data(r)
                    .code(SUCCESS_HTTP_STATUS_CODE)
                    .build());
  }

  public Single<LegacyFeatureStoreResponse<Object>> legacyReadFeatures(
      LegacyReadFeaturesRequest legacyReadFeaturesRequest) {
    LegacyReadHelper legacyReadHelper = new LegacyReadHelper(legacyReadFeaturesRequest);
    ReadCassandraFeaturesRequest readCassandraFeaturesRequest =
        LegacyReadHelper.getReadRequest(legacyReadHelper);
    return cassandraFeatureStoreService
        .readFeatures(readCassandraFeaturesRequest, true)
        .map(r -> LegacyReadHelper.getResponseMap(legacyReadHelper, r))
        .map(
            r ->
                LegacyFeatureStoreResponse.builder()
                    .code(SUCCESS_HTTP_STATUS_CODE)
                    .message(r)
                    .build())
        .onErrorResumeNext(
            e -> {
              if (Objects.equals(e.getMessage(), EMPTY_RESPONSE_ERROR_STRING)) {
                return Single.error(
                    LegacyStackException.getException(
                        LegacyFeatureStoreResponse.builder()
                            .code(NOT_FOUND_HTTP_STATUS_CODE)
                            .message(NOT_FOUND_ERROR_MESSAGE)
                            .body(legacyReadFeaturesRequest)
                            .build(),
                        NOT_FOUND_HTTP_STATUS_CODE,
                        objectMapper));
              } else if (e instanceof ApiRestException) {
                return Single.error(
                    LegacyStackException.getException(
                        LegacyFeatureStoreResponse.builder()
                            .code(((ApiRestException) e).getHttpStatusCode())
                            .message(e.getMessage())
                            .body(legacyReadFeaturesRequest)
                            .build(),
                        ((ApiRestException) e).getHttpStatusCode(),
                        objectMapper));
              }
              return Single.error(e);
            });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Single<AbstractMap.SimpleEntry<String, LegacyFeatureStoreResponse<Object>>>
      legacyReadFeaturesWithFgName(LegacyReadFeaturesRequest legacyReadFeaturesRequest) {
    LegacyReadHelper legacyReadHelper = new LegacyReadHelper(legacyReadFeaturesRequest);
    ReadCassandraFeaturesRequest readCassandraFeaturesRequest =
        LegacyReadHelper.getReadRequest(legacyReadHelper);
    return cassandraFeatureStoreService
        .readFeatures(readCassandraFeaturesRequest, true)
        .map(
            r ->
                new AbstractMap.SimpleEntry<>(
                    r.getFeatureGroupName(), LegacyReadHelper.getResponseMap(legacyReadHelper, r)))
        .map(
            r ->
                new AbstractMap.SimpleEntry<>(
                    r.getKey(),
                    LegacyFeatureStoreResponse.builder()
                        .code(SUCCESS_HTTP_STATUS_CODE)
                        .message(r.getValue())
                        .build()))
        .onErrorResumeNext(
            e -> {
              if (Objects.equals(e.getMessage(), EMPTY_RESPONSE_ERROR_STRING)) {
                return Single.just(
                    new AbstractMap.SimpleEntry<>(
                        readCassandraFeaturesRequest.getFeatureGroupName(),
                        LegacyStackException.getException(
                                LegacyFeatureStoreResponse.builder()
                                    .code(NOT_FOUND_HTTP_STATUS_CODE)
                                    .message(NOT_FOUND_ERROR_MESSAGE)
                                    .body(legacyReadFeaturesRequest)
                                    .build(),
                                NOT_FOUND_HTTP_STATUS_CODE,
                                objectMapper)
                            .getResponse()));
              } else if (e instanceof ApiRestException) {
                return Single.just(
                    new AbstractMap.SimpleEntry<>(
                        readCassandraFeaturesRequest.getFeatureGroupName(),
                        LegacyStackException.getException(
                                LegacyFeatureStoreResponse.builder()
                                    .code(((ApiRestException) e).getHttpStatusCode())
                                    .message(e.getMessage())
                                    .body(legacyReadFeaturesRequest)
                                    .build(),
                                ((ApiRestException) e).getHttpStatusCode(),
                                objectMapper)
                            .getResponse()));
              }
              return Single.error(e);
            });
  }

  public Single<LegacyFeatureStoreResponse<List<Map<String, Object>>>> legacyBulkReadFeatures(
      LegacyBulkReadFeaturesRequest legacyBulkReadFeaturesRequest) {
    LegacyReadHelper legacyReadHelper = new LegacyReadHelper(legacyBulkReadFeaturesRequest);
    ReadCassandraFeaturesRequest readCassandraFeaturesRequest =
        LegacyReadHelper.getReadRequest(legacyReadHelper);
    return cassandraFeatureStoreService
        .readFeatures(readCassandraFeaturesRequest, true)
        .map(r -> LegacyReadHelper.getBulkResponseMap(legacyReadHelper, r))
        .map(
            r ->
                LegacyFeatureStoreResponse.<List<Map<String, Object>>>builder()
                    .code(SUCCESS_HTTP_STATUS_CODE)
                    .data(r)
                    .message(DATA_FETCH_SUCCESS_MESSAGE)
                    .build())
        .onErrorResumeNext(
            e -> {
              log.error(e.getMessage());
              return Single.just(
                  LegacyFeatureStoreResponse.<List<Map<String, Object>>>builder()
                      .code(SUCCESS_HTTP_STATUS_CODE)
                      .data(new ArrayList<>())
                      .message(DATA_FETCH_SUCCESS_MESSAGE)
                      .build());
            });
  }

  public Single<LegacyFeatureStoreResponse<Object>> legacyCreateTable(
      LegacyCreateTableRequest legacyCreateTableRequest) throws JsonProcessingException {
    return cassandraOfsV2AdminService
        .createCassandraEntity(
            LegacyCreateTableRequest.getCreateCassandraEntityRequest(legacyCreateTableRequest))
        .map(
            r ->
                LegacyFeatureStoreResponse.builder()
                    .code(SUCCESS_HTTP_STATUS_CODE)
                    .message(SUCCESS_TABLE_CREATION)
                    .build())
        .onErrorResumeNext(
            e -> {
              if (Objects.equals(e.getMessage(), EMPTY_RESPONSE_ERROR_STRING)) {
                return Single.error(
                    LegacyStackException.getException(
                        LegacyFeatureStoreResponse.builder()
                            .code(NOT_FOUND_HTTP_STATUS_CODE)
                            .message(NOT_FOUND_ERROR_MESSAGE)
                            .body(legacyCreateTableRequest)
                            .build(),
                        NOT_FOUND_HTTP_STATUS_CODE,
                        objectMapper));
              } else if (e instanceof ApiRestException) {
                return Single.error(
                    LegacyStackException.getException(
                        LegacyFeatureStoreResponse.builder()
                            .code(((ApiRestException) e).getHttpStatusCode())
                            .message(e.getMessage())
                            .body(legacyCreateTableRequest)
                            .build(),
                        ((ApiRestException) e).getHttpStatusCode(),
                        objectMapper));
              }
              return Single.error(e);
            });
  }

  public Single<LegacyFeatureStoreResponse<Object>> legacyAlterTable(
      LegacyAlterTableRequest legacyAlterTableRequest) throws JsonProcessingException {
    LegacyAlterTableRequest.validate(legacyAlterTableRequest);
    return LegacyAlterTableRequest.getCreateCassandraFeatureGroupRequest(legacyAlterTableRequest)
        .flatMap(
            request ->
                cassandraOfsV2AdminService
                    .createCassandraFeatureGroup(request)
                    .map(
                        r ->
                            LegacyFeatureStoreResponse.builder()
                                .code(SUCCESS_HTTP_STATUS_CODE)
                                .message(SUCCESS_TABLE_ALTERATION)
                                .build()))
        .onErrorResumeNext(
            e -> {
              if (Objects.equals(e.getMessage(), EMPTY_RESPONSE_ERROR_STRING)) {
                return Single.error(
                    LegacyStackException.getException(
                        LegacyFeatureStoreResponse.builder()
                            .code(NOT_FOUND_HTTP_STATUS_CODE)
                            .message(NOT_FOUND_ERROR_MESSAGE)
                            .body(legacyAlterTableRequest)
                            .build(),
                        NOT_FOUND_HTTP_STATUS_CODE,
                        objectMapper));
              } else if (e instanceof ApiRestException) {
                return Single.error(
                    LegacyStackException.getException(
                        LegacyFeatureStoreResponse.builder()
                            .code(((ApiRestException) e).getHttpStatusCode())
                            .message(e.getMessage())
                            .body(legacyAlterTableRequest)
                            .build(),
                        ((ApiRestException) e).getHttpStatusCode(),
                        objectMapper));
              }
              return Single.error(e);
            });
  }

  public Single<Map<String, List<LegacyFeatureStoreResponse<Object>>>> legacyMultiGetFeatures(
      LegacyMultiBulkReadFeaturesRequest legacyMultiBulkReadFeaturesRequest) {
    return Observable.fromIterable(legacyMultiBulkReadFeaturesRequest.getBatches())
        .flatMapSingle(
            legacyReadFeaturesRequest -> {
              // imp!!
              legacyReadFeaturesRequest.patchRequest();
              return legacyReadFeaturesWithFgName(legacyReadFeaturesRequest)
                  .map(
                      res ->
                          MultiBulkReadResponseHelper.builder()
                              .featureGroupName(res.getKey())
                              .features(res.getValue())
                              .build());
            })
        .groupBy(MultiBulkReadResponseHelper::getFeatureGroupName)
        .flatMap(
            r ->
                r.map(MultiBulkReadResponseHelper::getFeatures)
                    .toList()
                    .map(li -> new AbstractMap.SimpleEntry<>(r.getKey(), li))
                    .toObservable())
        .toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue);
  }
}
