package com.dream11.admin.service;

import static com.dream11.core.constant.Constants.DEFAULT_VERSION;
import static com.dream11.core.error.ServiceError.*;
import static com.dream11.core.util.CassandraDataTypeUtils.getFeatureDataTypeMap;
import static com.dream11.core.util.FeatureNameUtils.getCassandraEntityFeatureSet;
import static com.dream11.core.util.JsonConversionUtils.convertJsonSingle;

import com.dream11.admin.dao.featuredao.CassandraFeatureDao;
import com.dream11.admin.service.featurestoreinterface.FeatureStoreServiceInterface;
import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featuredata.CassandraFeatureData;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.featuregroup.interfaces.FeatureGroup;
import com.dream11.core.dto.helper.CassandraFeatureGroupEntityPair;
import com.dream11.core.dto.helper.CassandraFeatureVectorAndPrimaryKeyPair;
import com.dream11.core.dto.request.*;
import com.dream11.core.dto.response.ReadCassandraFeaturesResponse;
import com.dream11.core.dto.response.WriteBulkCassandraFeaturesResponse;
import com.dream11.core.dto.response.WriteCassandraFeaturesResponse;
import com.dream11.core.dto.response.interfaces.CreateEntityResponse;
import com.dream11.core.dto.response.interfaces.CreateFeatureGroupResponse;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.core.util.VersionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CassandraFeatureStoreService implements FeatureStoreServiceInterface {
  private final CassandraFeatureDao cassandraFeatureDao;
  private final CassandraMetaStoreService cassandraMetaStoreService;
  private final ObjectMapper objectMapper;

  public Single<CreateEntityResponse> createEntity(JsonObject createEntityRequest, Boolean onlyRegister) {
    return convertJsonSingle(
        createEntityRequest,
        CreateCassandraEntityRequest.class,
        objectMapper,
        ENTITY_REGISTRATION_INVALID_REQUEST_EXCEPTION)
        .flatMap(
            request -> {
              if (!CreateCassandraEntityRequest.validateRequest(request)) {
                return Single.error(
                    new ApiRestException(ENTITY_REGISTRATION_INVALID_REQUEST_EXCEPTION));
              }
              return Single.just(request);
            })
        .flatMap(
            request -> {
              if (!CreateCassandraEntityRequest.validateSchema(request)) {
                return Single.error(
                    new ApiRestException(ENTITY_SCHEMA_VALIDATION_EXCEPTION));
              }
              return Single.just(request);
            })
        .flatMap(cassandraMetaStoreService::checkEntityExists)
        .flatMap(
            r ->
                cassandraMetaStoreService.atomicAddEntityToMetaStoreAndCassandra(
                    r,
                    cassandraFeatureDao::createEntity,
                    onlyRegister));
  }

  @Override
  public Single<CreateEntityResponse> createEntity(JsonObject createEntityRequest) {
    return createEntity(createEntityRequest, false);
  }

  private Completable deleteEntity(CassandraEntity entity) {
    return cassandraFeatureDao.deleteEntity(entity);
  }

  public Single<CreateFeatureGroupResponse> createFeatureGroup(
      JsonObject createFeatureGroupRequest, Boolean upgradeVersion, Boolean onlyRegister) {
    return convertJsonSingle(
        createFeatureGroupRequest,
        CreateCassandraFeatureGroupRequest.class,
        objectMapper,
        FEATURE_GROUP_REGISTRATION_INVALID_REQUEST_EXCEPTION)
        .flatMap(
            request -> {
              if (!CreateCassandraFeatureGroupRequest.validateRequest(request)) {
                return Single.error(
                    new ApiRestException(FEATURE_GROUP_REGISTRATION_INVALID_REQUEST_EXCEPTION));
              }
              return Single.just(request);
            })
        .flatMap(
            request ->
                cassandraMetaStoreService
                    .getFeatureGroupLatestVersion(request)
                    .flatMap(
                        latestVersion -> {
                          if (latestVersion.equals(DEFAULT_VERSION)) {
                            return Single.just(VersionUtils.bumpVersion(latestVersion));
                          } else if (upgradeVersion) {
                            return cassandraMetaStoreService
                                .getCassandraFeatureGroupMetaDataFromDb(
                                    request.getFeatureGroup().getFeatureGroupName(), latestVersion)
                                .filter(r -> r.getFeatureGroup().getVersionEnabled())
                                .switchIfEmpty(
                                    Single.error(
                                        new ApiRestException(VERSIONING_DISABLED_EXCEPTION)))
                                .map(
                                    r -> {
                                      request
                                          .getFeatureGroup()
                                          .setVersionEnabled(
                                              Boolean.TRUE); // handling corner case here
                                      return VersionUtils.bumpVersion(latestVersion);
                                    });
                          } else {
                            return Single.just(latestVersion);
                          }
                        })
                    .flatMap(
                        versionToAdd ->
                            cassandraMetaStoreService
                                .checkFeatureGroupExists(request, versionToAdd)
                                .flatMap(cassandraMetaStoreService::validateFeatureGroupAndEntity)
                                .flatMap(
                                    r ->
                                        cassandraMetaStoreService
                                            .atomicAddFeatureGroupToMetaStoreAndCassandra(
                                                r,
                                                versionToAdd,
                                                cassandraFeatureDao::createFeatureGroup,
                                                onlyRegister))));
  }

  public Single<CreateFeatureGroupResponse> createFeatureGroup(
      JsonObject createFeatureGroupRequest, Boolean upgradeVersion) {
    return createFeatureGroup(createFeatureGroupRequest, upgradeVersion, false);
  }

  public Completable deleteFeatureGroup(FeatureGroup featureGroup, String version) {
    return cassandraFeatureDao.deleteFeatureGroup(featureGroup, version);
  }

  private Single<CreateCassandraFeatureGroupRequest> validateFeatureGroupAcrossAllFeatureGroups(
      CreateCassandraFeatureGroupRequest createCassandraFeatureGroupRequest) {
    return cassandraMetaStoreService
        .getAllFeatureGroupsForAnEntity(
            createCassandraFeatureGroupRequest.getFeatureGroup().getEntityName())
        .map(
            r ->
                checkIfFgValidAgainstAllFg(createCassandraFeatureGroupRequest.getFeatureGroup(), r))
        .filter(r -> r)
        .switchIfEmpty(
            Single.error(
                new ApiRestException(FEATURE_GROUP_REGISTRATION_INVALID_COLUMN_NAMES_EXCEPTION)))
        .map(ignore -> createCassandraFeatureGroupRequest);
  }

  private Boolean checkIfFgValidAgainstAllFg(
      CassandraFeatureGroup currentFeatureGroup,
      List<CassandraFeatureGroup> registeredCassandraFeatureGroups) {
    Set<String> tableColumnNames = new HashSet<>();
    for (CassandraFeatureGroup featureGroup : registeredCassandraFeatureGroups) {
      for (CassandraFeatureColumn featureColumn : featureGroup.getFeatures()) {
        tableColumnNames.add(featureColumn.getFeatureName());
      }
    }

    for (CassandraFeatureColumn featureColumn : currentFeatureGroup.getFeatures()) {
      if (tableColumnNames.contains(featureColumn.getFeatureName())) {
        return Boolean.FALSE;
      }
    }
    return Boolean.TRUE;
  }
}
