package com.dream11.app.service;

import static com.dream11.core.error.ServiceError.OFS_V2_ADMIN_SERVICE_EXCEPTION;
import static com.dream11.core.error.ServiceError.OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION;
import static com.dream11.core.util.HttpResponseUtils.checkStatusAndConvert;
import static com.dream11.core.util.HttpResponseUtils.checkStatusAndConvertCompressed;

import com.dream11.common.app.AppContext;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.dto.helper.Data;
import com.dream11.core.dto.helper.cachekeys.CassandraEntityCacheKey;
import com.dream11.core.dto.helper.cachekeys.CassandraFeatureGroupCacheKey;
import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.VersionMetadata;
import com.dream11.core.dto.request.CreateCassandraEntityRequest;
import com.dream11.core.dto.request.CreateCassandraFeatureGroupRequest;
import com.dream11.core.dto.response.*;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.core.service.S3MetastoreService;
import com.dream11.core.util.ApiHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CassandraOfsV2AdminService {

  private final ApiHandler apiHandler;
  private final ObjectMapper objectMapper;
  private final ApplicationConfig applicationConfig;
  private final Vertx vertx = AppContext.getInstance(Vertx.class);
  private final S3MetastoreService s3MetastoreService =
      AppContext.getInstance(S3MetastoreService.class);

  public Single<CassandraFeatureGroupMetadata> getCassandraFeatureGroupMetadata(
      CassandraFeatureGroupCacheKey key) {
    return getCassandraFeatureGroupMetadata(key.getName(), key.getVersion());
  }

  public Single<CassandraFeatureGroupMetadata> getCassandraFeatureGroupMetadata(
      String name, String version) {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Map<String, String> params = new HashMap<>();
    params.put("name", name);
    params.put("version", version);
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetFeatureGroupMetadataEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers,
            params)
        .flatMap(
            res ->
                checkStatusAndConvert(
                    res,
                    new TypeReference<Data<GetCassandraFeatureGroupMetadataResponse>>() {},
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION))
        .map(GetCassandraFeatureGroupMetadataResponse::getFeatureGroupMetadata)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching feature group from admin", e);
              return s3MetastoreService
                  .getFeatureGroupMetadata(name, version)
                  .onErrorResumeNext(
                      s3exception -> {
                        log.error(
                            "s3 metastore exception while fetching feature group {} version {}",
                            name,
                            version,
                            e);
                        // known exception 4xx
                        if (s3exception instanceof ApiRestException)
                          return Single.error(
                              new ApiRestException(
                                  s3exception.getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION));
                        // unknown exception 5xx
                        return Single.error(
                            new ApiRestException(
                                s3exception.getMessage(),
                                ServiceError.OFS_V2_METASTORE_UNAVAILABLE_EXCEPTION));
                      });
            });
  }

  public Single<List<CassandraFeatureGroupMetadata>> getAllCassandraFeatureGroupMetadata() {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");

    Boolean compressedResponse = checkCompressedResponse();
    headers.put("compressed-response", compressedResponse.toString());
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetAllFeatureGroupMetadataEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers)
        .flatMap(
            res ->
                checkStatusAndConvertCompressed(
                    res,
                    AllCassandraFeatureGroupResponse.class,
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION,
                    vertx,
                    compressedResponse))
        .map(AllCassandraFeatureGroupResponse::getFeatureGroups)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching all feature group from admin", e);
              return s3MetastoreService
                  .getAllFeatureGroupMetadata()
                  .map(AllCassandraFeatureGroupResponse::getFeatureGroups)
                  .onErrorResumeNext(
                      s3exception -> {
                        log.error("s3 metastore exception while fetching all feature group", e);
                        // known exception 4xx
                        if (s3exception instanceof ApiRestException)
                          return Single.error(
                              new ApiRestException(
                                  s3exception.getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION));
                        // unknown exception 5xx
                        return Single.error(
                            new ApiRestException(
                                s3exception.getMessage(),
                                ServiceError.OFS_V2_METASTORE_UNAVAILABLE_EXCEPTION));
                      });
            });
  }

  // not using fallback for updates
  public Single<List<CassandraFeatureGroupMetadata>> getAllUpdatedCassandraFeatureGroupMetadata(
      Long timestamp) {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Boolean compressedResponse = checkCompressedResponse();
    headers.put("compressed-response", compressedResponse.toString());
    Map<String, String> params = new HashMap<>();
    params.put("timestamp", timestamp.toString());
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetAllUpdatedFeatureGroupMetadataEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers,
            params)
        .flatMap(
            res ->
                checkStatusAndConvertCompressed(
                    res,
                    AllCassandraFeatureGroupResponse.class,
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION,
                    vertx,
                    compressedResponse))
        .map(AllCassandraFeatureGroupResponse::getFeatureGroups)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching all updated feature groups from admin", e);
              return Single.error(e);
            });
  }

  public Single<List<VersionMetadata>> getAllCassandraFeatureGroupLatestVersions() {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Boolean compressedResponse = checkCompressedResponse();
    headers.put("compressed-response", compressedResponse.toString());
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetAllFeatureGroupLatestVersionEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers)
        .flatMap(
            res ->
                checkStatusAndConvertCompressed(
                    res,
                    AllCassandraFeatureGroupVersionResponse.class,
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION,
                    vertx,
                    compressedResponse))
        .map(AllCassandraFeatureGroupVersionResponse::getVersions)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching all feature group version from admin", e);
              return s3MetastoreService
                  .getAllFeatureGroupVersionMetadata()
                  .map(AllCassandraFeatureGroupVersionResponse::getVersions)
                  .onErrorResumeNext(
                      s3exception -> {
                        log.error(
                            "s3 metastore exception while fetching all feature group versions", e);
                        // known exception 4xx
                        if (s3exception instanceof ApiRestException)
                          return Single.error(
                              new ApiRestException(
                                  s3exception.getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION));
                        // unknown exception 5xx
                        return Single.error(
                            new ApiRestException(
                                s3exception.getMessage(),
                                ServiceError.OFS_V2_METASTORE_UNAVAILABLE_EXCEPTION));
                      });
            });
  }

  // not using fallback for updates
  public Single<List<VersionMetadata>> getAllUpdatedCassandraFeatureGroupLatestVersions(
      Long timestamp) {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Boolean compressedResponse = checkCompressedResponse();
    headers.put("compressed-response", compressedResponse.toString());
    Map<String, String> params = new HashMap<>();
    params.put("timestamp", timestamp.toString());
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetAllUpdatedFeatureGroupLatestVersionEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers,
            params)
        .flatMap(
            res ->
                checkStatusAndConvertCompressed(
                    res,
                    AllCassandraFeatureGroupVersionResponse.class,
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION,
                    vertx,
                    compressedResponse))
        .map(AllCassandraFeatureGroupVersionResponse::getVersions);
  }

  public Single<CassandraEntityMetadata> getCassandraEntityMetadata(CassandraEntityCacheKey key) {
    return getCassandraEntityMetadata(key.getName());
  }

  public Single<CassandraEntityMetadata> getCassandraEntityMetadata(String name) {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Map<String, String> params = new HashMap<>();
    params.put("name", name);
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetEntityMetadataEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers,
            params)
        .flatMap(
            res ->
                checkStatusAndConvert(
                    res,
                    new TypeReference<Data<GetCassandraEntityMetadataResponse>>() {},
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION))
        .map(GetCassandraEntityMetadataResponse::getEntityMetadata)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching entity from admin", e);
              return s3MetastoreService
                  .getEntityMetadata(name)
                  .onErrorResumeNext(
                      s3exception -> {
                        log.error("s3 metastore exception while fetching entity for {}", name, e);
                        // known exception 4xx
                        if (s3exception instanceof ApiRestException)
                          return Single.error(
                              new ApiRestException(
                                  s3exception.getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION));
                        // unknown exception 5xx
                        return Single.error(
                            new ApiRestException(
                                s3exception.getMessage(),
                                ServiceError.OFS_V2_METASTORE_UNAVAILABLE_EXCEPTION));
                      });
            });
  }

  public Single<List<CassandraEntityMetadata>> getAllCassandraEntityMetadata() {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Boolean compressedResponse = checkCompressedResponse();
    headers.put("compressed-response", compressedResponse.toString());
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetAllEntityMetadataEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers)
        .flatMap(
            res ->
                checkStatusAndConvertCompressed(
                    res,
                    AllCassandraEntityResponse.class,
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION,
                    vertx,
                    compressedResponse))
        .map(AllCassandraEntityResponse::getEntities)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching all entity from admin", e);
              return s3MetastoreService
                  .getAllEntityMetadata()
                  .map(AllCassandraEntityResponse::getEntities)
                  .onErrorResumeNext(
                      s3exception -> {
                        log.error("s3 metastore exception while fetching all entities", e);
                        // known exception 4xx
                        if (s3exception instanceof ApiRestException)
                          return Single.error(
                              new ApiRestException(
                                  s3exception.getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION));
                        // unknown exception 5xx
                        return Single.error(
                            new ApiRestException(
                                s3exception.getMessage(),
                                ServiceError.OFS_V2_METASTORE_UNAVAILABLE_EXCEPTION));
                      });
            });
  }

  // not using fallback for updates
  public Single<List<CassandraEntityMetadata>> getAllUpdatedCassandraEntityMetadata(
      Long timestamp) {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Boolean compressedResponse = checkCompressedResponse();
    headers.put("compressed-response", compressedResponse.toString());
    Map<String, String> params = new HashMap<>();
    params.put("timestamp", timestamp.toString());
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetAllUpdatedEntityMetadataEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers,
            params)
        .flatMap(
            res ->
                checkStatusAndConvertCompressed(
                    res,
                    AllCassandraEntityResponse.class,
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION,
                    vertx,
                    compressedResponse))
        .map(AllCassandraEntityResponse::getEntities)
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Single.error(e);
            });
  }

  public Single<VersionMetadata> getCassandraFeatureGroupLatestVersion(String name) {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    Map<String, String> params = new HashMap<>();
    params.put("name", name);
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminGetFeatureGroupLatestVersionEndpoint(),
            ApiHandler.RequestType.GET,
            new JsonObject(),
            headers,
            params)
        .flatMap(
            res ->
                checkStatusAndConvert(
                    res,
                    new TypeReference<Data<CassandraFeatureGroupVersionResponse>>() {},
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION))
        .map(CassandraFeatureGroupVersionResponse::getVersion)
        .onErrorResumeNext(
            e -> {
              log.error("error fetching feature group version from admin", e);
              return s3MetastoreService
                  .getFeatureGroupVersionMetadata(name)
                  .onErrorResumeNext(
                      s3exception -> {
                        log.error(
                            "s3 metastore exception while fetching feature group version for {}",
                            name,
                            e);
                        // known exception 4xx
                        if (s3exception instanceof ApiRestException)
                          return Single.error(
                              new ApiRestException(
                                  s3exception.getMessage(), OFS_V2_ADMIN_SERVICE_EXCEPTION));
                        // unknown exception 5xx
                        return Single.error(
                            new ApiRestException(
                                s3exception.getMessage(),
                                ServiceError.OFS_V2_METASTORE_UNAVAILABLE_EXCEPTION));
                      });
            });
  }

  public Single<CreateCassandraEntityResponse> createCassandraEntity(
      CreateCassandraEntityRequest createCassandraEntityRequest) throws JsonProcessingException {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    JsonObject body = new JsonObject(objectMapper.writeValueAsString(createCassandraEntityRequest));
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminCreateEntityEndpoint(),
            ApiHandler.RequestType.POST,
            body,
            headers)
        .flatMap(
            res ->
                checkStatusAndConvert(
                    res,
                    new TypeReference<Data<CreateCassandraEntityResponse>>() {},
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION))
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Single.error(e);
            });
  }

  public Single<CreateCassandraFeatureGroupResponse> createCassandraFeatureGroup(
      CreateCassandraFeatureGroupRequest createCassandraFeatureGroupRequest)
      throws JsonProcessingException {
    Map<String, String> headers = new HashMap<>();
    headers.put("feature-store-type", "cassandra");
    JsonObject body =
        new JsonObject(objectMapper.writeValueAsString(createCassandraFeatureGroupRequest));
    return apiHandler
        .call(
            applicationConfig.getOfsAdminHost(),
            applicationConfig.getOfsAdminCreateFeatureGroupEndpoint(),
            ApiHandler.RequestType.POST,
            body,
            headers)
        .flatMap(
            res ->
                checkStatusAndConvert(
                    res,
                    new TypeReference<Data<CreateCassandraFeatureGroupResponse>>() {},
                    objectMapper,
                    OFS_V2_ADMIN_SERVICE_UNKNOWN_EXCEPTION))
        .onErrorResumeNext(
            e -> {
              e.printStackTrace();
              return Single.error(e);
            });
  }

  private static Boolean checkCompressedResponse() {
    return !Objects.equals(System.getProperty("app.environment"), "local")
        && !Objects.equals(System.getProperty("app.environment"), "test");
  }
}
