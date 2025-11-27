package com.dream11.admin.service;

import com.dream11.core.constant.Constants;
import com.dream11.core.dto.response.interfaces.GetDistinctOwnersResponse;
import com.dream11.core.dto.response.interfaces.GetDistinctTagsResponse;
import com.dream11.core.dto.response.interfaces.SearchEntityResponse;
import com.dream11.core.dto.response.interfaces.SearchFeatureGroupResponse;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class SearchService {
  private final CassandraMetaStoreSearchService cassandraSearchService;

  public Single<GetDistinctOwnersResponse> getDistinctFeatureGroupOwners(
      Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraSearchService.getFeatureGroupOwners().map(r -> r);
    }
  }

  public Single<GetDistinctOwnersResponse> getDistinctEntityOwners(
      Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraSearchService.getEntityOwners().map(r -> r);
    }
  }

  public Single<GetDistinctTagsResponse> getDistinctFeatureGroupTags(
      Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraSearchService.getFeatureGroupTags().map(r -> r);
    }
  }

  public Single<GetDistinctTagsResponse> getDistinctEntityTags(
      Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraSearchService.getEntityTags().map(r -> r);
    }
  }

  public Single<SearchEntityResponse> searchCassandraEntities(
      JsonObject searchEntityRequest,
      Constants.FeatureStoreType featureStoreType,
      Integer pageSize,
      Integer pageOffset) {
    switch (featureStoreType) {
      default:
        return cassandraSearchService.searchEntities(searchEntityRequest, pageSize, pageOffset).map(r -> r);
    }
  }

  public Single<SearchFeatureGroupResponse> searchCassandraFeatureGroups(
      JsonObject searchFeatureGroupRequest,
      Constants.FeatureStoreType featureStoreType,
      Integer pageSize,
      Integer pageOffset) {
    switch (featureStoreType) {
      default:
        return cassandraSearchService.searchFeatureGroups(searchFeatureGroupRequest, pageSize, pageOffset).map(r -> r);
    }
  }
}
