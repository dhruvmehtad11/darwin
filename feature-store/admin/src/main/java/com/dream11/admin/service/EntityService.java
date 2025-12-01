package com.dream11.admin.service;

import com.dream11.core.constant.Constants;
import com.dream11.core.dto.request.interfaces.UpdateEntityRequest;
import com.dream11.core.dto.response.CreateCassandraEntityResponse;
import com.dream11.core.dto.response.GetCassandraEntityMetadataResponse;
import com.dream11.core.dto.response.interfaces.CreateEntityResponse;
import com.dream11.core.dto.response.interfaces.GetEntityMetadataResponse;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class EntityService {

  private final CassandraFeatureStoreService cassandraFeatureStoreService;
  private final CassandraMetaStoreService cassandraMetaStoreService;

  public Single<CreateEntityResponse> createEntity(
      JsonObject createEntityRequest, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService.createEntity(createEntityRequest);
    }
  }

  public Single<CreateEntityResponse> registerEntity(
      JsonObject createEntityRequest, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService.createEntity(createEntityRequest, true);
    }
  }

  public Single<CreateEntityResponse> getEntity(
      String name, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
            .getCassandraEntity(name)
            .map(e -> CreateCassandraEntityResponse.builder().entity(e).build());
    }
  }

  public Single<GetEntityMetadataResponse> getEntityMetadata(
      String name, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
            .getCassandraEntityMetaData(name)
            .map(r -> GetCassandraEntityMetadataResponse.builder().entityMetadata(r).build());
    }
  }
  public Single<String> getAllEntities(Constants.FeatureStoreType featureStoreType, Boolean compressedResponse) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
                .getAllEntitiesFromCacheCompressed(compressedResponse);
    }
  }

  public Single<String> getAllUpdatesEntities(Constants.FeatureStoreType featureStoreType, Long timestamp, Boolean compressedResponse) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
                .getUpdatedCassandraEntitiesFromCacheCompressed(timestamp, compressedResponse);
    }
  }

  public Single<UpdateEntityRequest> updateEntity(
      String name,
      Constants.FeatureStoreType featureStoreType,
      JsonObject updateFeatureGroupRequest) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.updateCassandraEntity(name, updateFeatureGroupRequest).map(r -> r);
    }
  }
}
