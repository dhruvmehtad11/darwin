package com.dream11.admin.service;

import static com.dream11.core.constant.Constants.*;

import com.dream11.admin.dao.metastore.MetaStoreSearchDao;
import com.dream11.core.dto.helper.CassandraOwnerDto;
import com.dream11.core.dto.helper.CassandraSearchEntityByTagsDto;
import com.dream11.core.dto.helper.CassandraSearchFeatureGroupByTagsDto;
import com.dream11.core.dto.helper.CassandraTagDto;
import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.request.SearchCassandraEntityRequest;
import com.dream11.core.dto.request.SearchCassandraFeatureGroupRequest;
import com.dream11.core.dto.response.GetDistinctCassandraFeatureStoreOwnersResponse;
import com.dream11.core.dto.response.GetDistinctCassandraFeatureStoreTagsResponse;
import com.dream11.core.dto.response.SearchCassandraEntityResponse;
import com.dream11.core.dto.response.SearchCassandraFeatureGroupResponse;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.error.ServiceError;
import com.dream11.core.util.JsonConversionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class CassandraMetaStoreSearchService {
  private final MetaStoreSearchDao metaStoreSearchDao;
  private final CassandraMetaStoreService cassandraMetaStoreService;
  private final ObjectMapper objectMapper;

  public Single<GetDistinctCassandraFeatureStoreOwnersResponse> getFeatureGroupOwners() {
    return getOwnersUsingTableName(CASSANDRA_FEATURE_GROUP_METADATA_TABLE_NAME);
  }

  public Single<GetDistinctCassandraFeatureStoreOwnersResponse> getEntityOwners() {
    return getOwnersUsingTableName(CASSANDRA_ENTITY_METADATA_TABLE_NAME);
  }

  public Single<GetDistinctCassandraFeatureStoreTagsResponse> getFeatureGroupTags() {
    return getTagsUsingTableName(CASSANDRA_FEATURE_GROUP_TAG_MAP_TABLE_NAME);
  }

  public Single<GetDistinctCassandraFeatureStoreTagsResponse> getEntityTags() {
    return getTagsUsingTableName(CASSANDRA_ENTITY_TAG_MAP_TABLE_NAME);
  }

  private Single<GetDistinctCassandraFeatureStoreOwnersResponse> getOwnersUsingTableName(
      String tableName) {
    return metaStoreSearchDao
        .getDistinctCassandraOwners(tableName)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraOwnerDto.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION))
        .map(CassandraOwnerDto::getOwner)
        .toList()
        .filter(json -> !json.isEmpty())
        .map(li -> GetDistinctCassandraFeatureStoreOwnersResponse.builder().owners(li).build())
        .switchIfEmpty(
            Single.just(
                GetDistinctCassandraFeatureStoreOwnersResponse.builder()
                    .owners(new ArrayList<>())
                    .build()));
  }

  private Single<GetDistinctCassandraFeatureStoreTagsResponse> getTagsUsingTableName(
      String tableName) {
    return metaStoreSearchDao
        .getDistinctCassandraTags(tableName)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraTagDto.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION))
        .map(CassandraTagDto::getTag)
        .toList()
        .filter(json -> !json.isEmpty())
        .map(li -> GetDistinctCassandraFeatureStoreTagsResponse.builder().tags(li).build())
        .switchIfEmpty(
            Single.just(
                GetDistinctCassandraFeatureStoreTagsResponse.builder()
                    .tags(new ArrayList<>())
                    .build()));
  }

  public Single<SearchCassandraEntityResponse> searchEntities(
      JsonObject searchEntityRequest, Integer pageSize, Integer pageOffset) {
    return JsonConversionUtils.convertJsonSingle(
            searchEntityRequest,
            SearchCassandraEntityRequest.class,
            objectMapper,
            ServiceError.SEARCH_ENTITY_INVALID_REQUEST_EXCEPTION)
        .flatMap(
            searchCassandraEntityRequest ->
                Single.just(searchCassandraEntityRequest)
                    .map(SearchCassandraEntityRequest::validateRequest)
                    .filter(r -> r)
                    .switchIfEmpty(
                        Single.error(
                            new ApiRestException(
                                ServiceError.SEARCH_ENTITY_INVALID_REQUEST_EXCEPTION)))
                    .map(ignore -> searchCassandraEntityRequest))
        .flatMap(r -> searchEntityInOrder(r, pageSize, pageOffset));
  }

  public Single<SearchCassandraFeatureGroupResponse> searchFeatureGroups(
      JsonObject searchFeatureGroupsRequest, Integer pageSize, Integer pageOffset) {
    return JsonConversionUtils.convertJsonSingle(
            searchFeatureGroupsRequest,
            SearchCassandraFeatureGroupRequest.class,
            objectMapper,
            ServiceError.SEARCH_ENTITY_INVALID_REQUEST_EXCEPTION)
        .flatMap(
            searchCassandraFeatureGroupRequest ->
                Single.just(searchCassandraFeatureGroupRequest)
                    .map(SearchCassandraFeatureGroupRequest::validateRequest)
                    .filter(r -> r)
                    .switchIfEmpty(
                        Single.error(
                            new ApiRestException(
                                ServiceError.SEARCH_ENTITY_INVALID_REQUEST_EXCEPTION)))
                    .map(ignore -> searchCassandraFeatureGroupRequest))
        .flatMap(r -> searchFeatureGroupInOrder(r, pageSize, pageOffset));
  }

  private Single<SearchCassandraEntityResponse> searchEntityInOrder(
      SearchCassandraEntityRequest searchCassandraEntityRequest,
      Integer pageSize,
      Integer pageOffset) {
    return metaStoreSearchDao
        .searchCassandraEntityInOrder(
            searchCassandraEntityRequest.getOwners(),
            searchCassandraEntityRequest.getTags(),
            searchCassandraEntityRequest.getSearchPattern(),
            pageSize,
            pageOffset)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraEntityMetadata.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION))
        .toList()
        .map(li -> SearchCassandraEntityResponse.builder().entities(li).build());
  }

  private Single<SearchCassandraFeatureGroupResponse> searchFeatureGroupInOrder(
      SearchCassandraFeatureGroupRequest searchCassandraFeatureGroupRequest,
      Integer pageSize,
      Integer pageOffset) {
    return metaStoreSearchDao
        .searchCassandraFeatureGroupInOrder(
            searchCassandraFeatureGroupRequest.getOwners(),
            searchCassandraFeatureGroupRequest.getTags(),
            searchCassandraFeatureGroupRequest.getSearchPattern(),
            pageSize,
            pageOffset)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraFeatureGroupMetadata.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION))
        .toList()
        .map(li -> SearchCassandraFeatureGroupResponse.builder().featureGroups(li).build());
  }

  private Observable<CassandraEntityMetadata> searchCassandraEntityUsingTags(List<String> tags) {
    return metaStoreSearchDao
        .searchCassandraEntityUsingTags(tags)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraSearchEntityByTagsDto.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION))
        .map(CassandraSearchEntityByTagsDto::getEntityName)
        .flatMapSingle(cassandraMetaStoreService::getCassandraEntityMetaData);
  }

  private Observable<CassandraEntityMetadata> searchCassandraEntityUsingOwners(
      List<String> owners) {
    return metaStoreSearchDao
        .searchCassandraEntityUsingOwners(owners)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraEntityMetadata.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION));
  }

  private Observable<CassandraEntityMetadata> searchCassandraEntityUsingPattern(String pattern) {
    return metaStoreSearchDao
        .searchCassandraEntityUsingPattern(pattern)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraEntityMetadata.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION));
  }

  private Observable<CassandraFeatureGroupMetadata> searchCassandraFeatureGroupUsingPattern(
      String pattern) {
    return metaStoreSearchDao
        .searchCassandraFeatureGroupUsingPattern(pattern)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraFeatureGroupMetadata.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION));
  }

  private Observable<CassandraFeatureGroupMetadata> searchCassandraFeatureGroupUsingOwners(
      List<String> owners) {
    return metaStoreSearchDao
        .searchCassandraFeatureGroupUsingOwners(owners)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraFeatureGroupMetadata.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION));
  }

  private Observable<CassandraFeatureGroupMetadata> searchCassandraFeatureGroupUsingTags(
      List<String> tags) {
    return metaStoreSearchDao
        .searchCassandraFeatureGroupUsingTags(tags)
        .flatMap(
            jsonObject ->
                JsonConversionUtils.convertJsonObservable(
                    jsonObject,
                    CassandraSearchFeatureGroupByTagsDto.class,
                    objectMapper,
                    ServiceError.SERVICE_UNKNOWN_EXCEPTION))
        .flatMapSingle(
            cassandraSearchFeatureGroupByTagsDto ->
                cassandraMetaStoreService.getCassandraFeatureGroupMetaData(
                    cassandraSearchFeatureGroupByTagsDto.getFeatureGroupName(),
                    cassandraSearchFeatureGroupByTagsDto.getFeatureGroupVersion()));
  }
}
