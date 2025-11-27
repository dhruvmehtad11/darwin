package com.dream11.admin.rest;

import static com.dream11.core.constant.Constants.FeatureStoreType.cassandra;

import com.dream11.core.constant.Constants;
import com.dream11.core.dto.request.interfaces.UpdateEntityRequest;
import com.dream11.core.dto.response.interfaces.CreateEntityResponse;
import com.dream11.core.dto.response.interfaces.GetEntityMetadataResponse;
import com.dream11.admin.service.EntityService;
import com.dream11.core.dto.response.interfaces.SearchEntityResponse;
import com.dream11.core.dto.response.interfaces.SearchFeatureGroupResponse;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/entity")
public class FeatureStoreEntity {
  private final EntityService entityService;

  @POST
  @Path("/")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateEntityResponse>> createEntity(
      @RequestBody @NotNull(message = "body must not be null") Map<String, Object> createEntityRequest,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return entityService
        .createEntity(new JsonObject(createEntityRequest), featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  // to register metadata for existing entity
  @POST
  @Path("/register")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateEntityResponse>> registerEntity(
      @RequestBody @NotNull(message = "body must not be null") Map<String, Object> createEntityRequest,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return entityService
        .registerEntity(new JsonObject(createEntityRequest), featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateEntityResponse>> getEntity(
      @NotNull(message = "name must not be null") @QueryParam("name") String entityName,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return entityService
        .getEntity(entityName, featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateEntityRequest>> getEntity(
      @RequestBody @NotNull(message = "body must not be null") Map<String, Object> updateEntityRequest,
      @NotNull(message = "name must not be null") @QueryParam("name") String entityName,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return entityService
        .updateEntity(entityName, featureStoreType, new JsonObject(updateEntityRequest))
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/metadata")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetEntityMetadataResponse>> getEntityMetadata(
      @NotNull(message = "name must not be null") @QueryParam("name") String entityName,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return entityService
        .getEntityMetadata(entityName, featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<String>> getEntityMetadata(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @HeaderParam(value = "compressed-response") Boolean compressedResponse) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (compressedResponse == null) compressedResponse = true;
    return entityService.getAllEntities(featureStoreType, compressedResponse).to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all-updated")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<String>> getAllUpdatedEntityMetadata(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "timestamp must not be null") @QueryParam(value = "timestamp")
          Long timestamp,
      @HeaderParam(value = "compressed-response") Boolean compressedResponse) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (compressedResponse == null) compressedResponse = true;
    return entityService
        .getAllUpdatesEntities(featureStoreType, timestamp, compressedResponse)
        .to(RestResponse.jaxrsRestHandler());
  }
}
