package com.dream11.admin.rest;

import static com.dream11.core.constant.Constants.FeatureStoreType.cassandra;

import com.dream11.admin.service.FeatureGroupService;
import com.dream11.core.constant.Constants;
import com.dream11.core.dto.response.AllCassandraFeatureGroupResponse;
import com.dream11.core.dto.tenant.AddTenantRequest;
import com.dream11.core.dto.tenant.GetAllTenantResponse;
import com.dream11.core.dto.tenant.UpdateTenantRequest;
import com.dream11.rest.io.Response;
import com.dream11.rest.io.RestResponse;
import com.google.inject.Inject;
import java.util.concurrent.CompletionStage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
@Path("/tenant")
public class Tenant {
  private final FeatureGroupService featureGroupService;
  
  @GET
  @Path("/feature-group/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<AllCassandraFeatureGroupResponse>> getAllFeatureGroups(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @NotNull(message = "name must not be null") @QueryParam(value = "tenant-name") String name,
      @QueryParam(value = "tenant-type") Constants.FeatureStoreTenantType type) {
    if (featureStoreType == null) featureStoreType = cassandra;
    if (type == null) type = Constants.FeatureStoreTenantType.ALL;
    return featureGroupService
        .getAllFeatureGroupsForTenant(featureStoreType, name, type)
        .to(RestResponse.jaxrsRestHandler());
  }

  @PUT
  @Path("/all")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<UpdateTenantRequest>> updateAllTenant(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType,
      @RequestBody @Valid UpdateTenantRequest request) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return featureGroupService
        .updateAllFeatureGroupTenant(featureStoreType, request)
        .andThen(Single.just(request))
        .to(RestResponse.jaxrsRestHandler());
  }

  @GET
  @Path("/list")
  @Consumes(MediaType.WILDCARD)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response<GetAllTenantResponse>> updateAllTenant(
      @HeaderParam(value = "feature-store-type") Constants.FeatureStoreType featureStoreType) {
    if (featureStoreType == null) featureStoreType = cassandra;
    return featureGroupService
        .getAllTenants(featureStoreType)
        .to(RestResponse.jaxrsRestHandler());
  }
}
