package com.dream11.admin.rest;

import static com.dream11.core.constant.Constants.FeatureStoreType.cassandra;

import com.dream11.core.constant.Constants;
import com.dream11.core.dto.response.interfaces.GetDistinctOwnersResponse;
import com.dream11.core.dto.response.interfaces.GetDistinctTagsResponse;
import com.dream11.core.dto.response.interfaces.SearchEntityResponse;
import com.dream11.core.dto.response.interfaces.SearchFeatureGroupResponse;
import com.dream11.admin.service.SearchService;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/search")
public class Search {
  private final SearchService searchService;

  @GET
  @Path("/all-feature-group-owners")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetDistinctOwnersResponse>> getFeatureGroupOwners(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return searchService
        .getDistinctFeatureGroupOwners(featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all-entity-owners")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetDistinctOwnersResponse>> getEntityOwners(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return searchService
        .getDistinctEntityOwners(featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all-feature-group-tags")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetDistinctTagsResponse>> getFeatureGroupTags(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return searchService
        .getDistinctFeatureGroupTags(featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all-entity-tags")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetDistinctTagsResponse>> getEntityTags(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return searchService
        .getDistinctEntityTags(featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/feature-groups")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<SearchFeatureGroupResponse>> searchFeatureGroup(
      @RequestBody Map<String, Object> searchFeatureGroupRequest,
      @QueryParam("page-size") Integer pageSize,
      @QueryParam("page-offset") Integer pageOffset,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (pageSize == null) pageSize = 20;
    if (pageOffset == null) pageOffset = 0;
    return searchService
        .searchCassandraFeatureGroups(
            new JsonObject(searchFeatureGroupRequest), featureStoreType, pageSize, pageOffset)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/entities")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<SearchEntityResponse>> searchEntity(
      @RequestBody Map<String, Object> searchEntityRequest,
      @QueryParam("page-size") Integer pageSize,
      @QueryParam("page-offset") Integer pageOffset,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (pageSize == null) pageSize = 20;
    if (pageOffset == null) pageOffset = 0;
    return searchService
        .searchCassandraEntities(
            new JsonObject(searchEntityRequest), featureStoreType, pageSize, pageOffset)
        .to(RestResponse.jaxrsRestHandler());
  }
}
