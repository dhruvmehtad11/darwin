package com.dream11.admin.rest;

import static com.dream11.core.constant.Constants.FeatureStoreType.cassandra;

import com.dream11.admin.dao.metastore.MetaStoreRunsV2Dao;
import com.dream11.admin.service.FeatureGroupService;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.constant.Constants;
import com.dream11.core.dto.consumer.LegacyGetFeatureGroupTopicResponse;
import com.dream11.core.dto.request.FeatureGroupRunDataRequest;
import com.dream11.core.dto.request.UpdateFeatureGroupTtlRequest;
import com.dream11.core.dto.request.interfaces.UpdateFeatureGroupRequest;
import com.dream11.core.dto.response.CassandraFeatureGroupVersionResponse;
import com.dream11.core.dto.response.FeatureGroupRunDataResponse;
import com.dream11.core.dto.response.GetTopicResponse;
import com.dream11.core.dto.response.interfaces.*;
import com.dream11.core.dto.tenant.AddTenantRequest;
import com.dream11.core.dto.tenant.TenantDto;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/feature-group")
public class FeatureStoreFeatureGroup {
  private final FeatureGroupService featureGroupService;

  @POST
  @Path("/")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateFeatureGroupResponse>> createFeatureGroup(
      @RequestBody @NotNull(message = "body must not be null")
          Map<String, Object> createFeatureGroupRequest,
      @HeaderParam("upgrade") Boolean upgradeVersion,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    if (upgradeVersion == null) {
      upgradeVersion = false;
    }
    return featureGroupService
        .createFeatureGroup(
            new JsonObject(createFeatureGroupRequest), featureStoreType, upgradeVersion)
        .to(RestResponse.jaxrsRestHandler());
  }

  // to register metadata for existing feature group
  @POST
  @Path("/register")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateFeatureGroupResponse>> registerFeatureGroup(
      @RequestBody @NotNull(message = "body must not be null")
          Map<String, Object> createFeatureGroupRequest,
      @HeaderParam("upgrade") Boolean upgradeVersion,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    if (upgradeVersion == null) {
      upgradeVersion = false;
    }
    return featureGroupService
        .registerFeatureGroup(
            new JsonObject(createFeatureGroupRequest), featureStoreType, upgradeVersion)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CreateFeatureGroupResponse>> getFeatureGroup(
      @NotNull(message = "name must not be null") @QueryParam("name") String featureGroupName,
      @QueryParam("version") String version,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getFeatureGroup(featureGroupName, version, featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateFeatureGroupRequest>> updateFeatureGroup(
      @RequestBody @NotNull(message = "body must not be null")
          Map<String, Object> updateFeatureGroupRequest,
      @NotNull(message = "name must not be null") @QueryParam("name") String featureGroupName,
      @NotNull(message = "version must not be null") @QueryParam("version") String version,
      @HeaderParam("feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .updateFeatureGroup(
            featureGroupName, version, featureStoreType, new JsonObject(updateFeatureGroupRequest))
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/schema")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetFeatureGroupSchemaResponse>> getFeatureGroupSchema(
      @NotNull(message = "name must not be null") @QueryParam("name") String featureGroupName,
      @QueryParam("version") String version,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getFeatureGroupSchema(featureGroupName, version, featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/metadata")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetFeatureGroupMetadataResponse>> getFeatureGroupMetadata(
      @NotNull(message = "name must not be null") @QueryParam("name") String featureGroupName,
      @QueryParam("version") String version,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getFeatureGroupMetadata(featureGroupName, version, featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/version")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<CassandraFeatureGroupVersionResponse>> getFeatureGroupVersion(
      @NotNull(message = "name must not be null") @QueryParam("name") String featureGroupName,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getFeatureGroupLatestVersion(featureStoreType, featureGroupName)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<String>> getAllFeatureGroups(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @HeaderParam(value = "compressed-response") Boolean compressedResponse) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (compressedResponse == null) compressedResponse = true;
    return featureGroupService
        .getAllFeatureGroups(featureStoreType, compressedResponse)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/all-updated")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<String>> getAllUpdatedFeatureGroups(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "timestamp must not be null") @QueryParam(value = "timestamp")
          Long timestamp,
      @HeaderParam(value = "compressed-response") Boolean compressedResponse) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (compressedResponse == null) compressedResponse = true;
    return featureGroupService
        .getAllUpdatedFeatureGroups(featureStoreType, timestamp, compressedResponse)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/version/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<String>> getAllFeatureGroupLatestVersion(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @HeaderParam(value = "compressed-response") Boolean compressedResponse) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (compressedResponse == null) compressedResponse = true;
    return featureGroupService
        .getAllFeatureGroupLatestVersion(featureStoreType, compressedResponse)
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/version/all-updated")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<String>> getAllUpdatedFeatureGroupLatestVersion(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "timestamp must not be null") @QueryParam(value = "timestamp")
          Long timestamp,
      @HeaderParam(value = "compressed-response") Boolean compressedResponse) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (compressedResponse == null) compressedResponse = true;
    return featureGroupService
        .getAllUpdatedFeatureGroupLatestVersion(featureStoreType, timestamp, compressedResponse)
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/tenant")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<AddTenantRequest>> addTenant(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @RequestBody @Valid AddTenantRequest request) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .addTenant(featureStoreType, request.getFeatureGroupName(), request.getTenantConfig())
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/tenant")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<TenantDto>> getTenant(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "name must not be null") @QueryParam(value = "feature-group-name") String fgName) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getTenant(featureStoreType, fgName)
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/ttl")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateFeatureGroupTtlRequest>> getTenant(
      @RequestBody @NotNull(message = "body must not be null") UpdateFeatureGroupTtlRequest UpdateFeatureGroupTtlRequest,
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .updateTtl(featureStoreType, UpdateFeatureGroupTtlRequest)
        .andThen(Single.just(UpdateFeatureGroupTtlRequest))
        .to(RestResponse.jaxrsRestHandler());
  }

  // legacy used by sdk
  @GET
  @Path("/{feature-group-name}/topic")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<LegacyGetFeatureGroupTopicResponse> getTenantTopic(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "name must not be null") @PathParam(value = "feature-group-name") String fgName) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return CompletableFutureUtils.fromSingle(
        featureGroupService
            .getTenantTopic(featureStoreType, fgName)
            .map(LegacyGetFeatureGroupTopicResponse::getSuccessResponse));
  }

  @GET
  @Path("/topic")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetTopicResponse>> getTenantTopicV2(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "name must not be null") @QueryParam(value = "name") String fgName) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getTenantTopic(featureStoreType, fgName)
        .map(topic -> GetTopicResponse.builder().featureGroupName(fgName).topic(topic).build())
        .to(RestResponse.jaxrsRestHandler());
  }

  @POST
  @Path("/run-data")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<FeatureGroupRunDataRequest>> getTenantTopicV2(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @RequestBody @Valid @NotNull FeatureGroupRunDataRequest request) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .putFeatureGroupRun(featureStoreType, request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/run-data")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<List<FeatureGroupRunDataResponse>>> getRunData(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "name must not be null") @QueryParam("name") String featureGroupName,
      @QueryParam("version") String version) {
    if (featureStoreType == null) {
      featureStoreType = cassandra;
    }
    return featureGroupService
        .getFeatureGroupRun(featureStoreType, featureGroupName, version)
        .to(RestResponse.jaxrsRestHandler());
  }
}
