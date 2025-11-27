//package com.dream11.admin.rest;
//
//import static com.dream11.core.constant.Constants.ERROR_HTTP_STATUS_CODE;
//import static com.dream11.core.constant.Constants.SUCCESS_HTTP_STATUS_CODE;
//
//import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupCountRequest;
//import com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse;
//import com.dream11.admin.dto.fctapplayer.response.FeatureGroupCountResponse;
//import com.dream11.admin.dto.helper.FctAppLayerBaseResponse;
//import com.dream11.admin.service.FctAppLayerService;
//import com.dream11.common.util.CompletableFutureUtils;
//import com.dream11.core.dto.response.interfaces.CreateEntityResponse;
//import com.dream11.rest.io.Response;
//import com.google.inject.Inject;
//import io.reactivex.Single;
//import java.util.List;
//import java.util.concurrent.CompletionStage;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.MediaType;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@RequiredArgsConstructor(onConstructor = @__(@Inject))
//@Path("/FeatureStore")
//public class FctAppLayer {
//  private final FctAppLayerService fctAppLayerService;
//
//  @GET
//  @Path("/feature-group-runs")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<Response<CreateEntityResponse>> getFeatureGroupRuns() {
//    return null;
//  }
//
//  @POST
//  @Path("/get-feature-groups")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<Response<CreateEntityResponse>> getFeatureGroups() {
//    return null;
//  }
//
//  @POST
//  @Path("/feature-groups/count")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<FctAppLayerBaseResponse<FeatureGroupCountResponse>> getFeatureGroupsCount(
//      GetFeatureGroupCountRequest getFeatureGroupCountRequest) {
//    return fctAppLayerService
//        .getFeatureGroupCount(
//            getFeatureGroupCountRequest.getSearchString(),
//            getFeatureGroupCountRequest.getFilterMap())
//        .map(
//            r ->
//                FctAppLayerBaseResponse.<FeatureGroupCountResponse>builder()
//                    .status(FctAppLayerBaseResponse.Status.SUCCESS)
//                    .statusCode(SUCCESS_HTTP_STATUS_CODE)
//                    .data(r)
//                    .build())
//        .onErrorResumeNext(
//            e ->
//                Single.just(
//                    FctAppLayerBaseResponse.<FeatureGroupCountResponse>builder()
//                        .status(FctAppLayerBaseResponse.Status.ERROR)
//                        .statusCode(ERROR_HTTP_STATUS_CODE)
//                        .build()))
//        .to(CompletableFutureUtils::fromSingle);
//  }
//
//  @GET
//  @Path("/group-filters")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<FctAppLayerBaseResponse<List<GetFeatureGroupCountRequest.Filter>>>
//      getFeatureGroupFilters() {
//    return fctAppLayerService
//        .getFeatureGroupFilters()
//        .map(
//            r ->
//                FctAppLayerBaseResponse.<List<GetFeatureGroupCountRequest.Filter>>builder()
//                    .status(FctAppLayerBaseResponse.Status.SUCCESS)
//                    .statusCode(SUCCESS_HTTP_STATUS_CODE)
//                    .data(r)
//                    .build())
//        .onErrorResumeNext(
//            e ->
//                Single.just(
//                    FctAppLayerBaseResponse.<List<GetFeatureGroupCountRequest.Filter>>builder()
//                        .status(FctAppLayerBaseResponse.Status.ERROR)
//                        .statusCode(ERROR_HTTP_STATUS_CODE)
//                        .build()))
//        .to(CompletableFutureUtils::fromSingle);
//  }
//
//  @GET
//  @Path("/get-feature-of-feature-group")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<Response<CreateEntityResponse>> getFeatureGroupFeatures(
//      @QueryParam("search_string") String searchString,
//      @QueryParam("feature_group_id") String featureGroupName,
//      @QueryParam("version") Integer version,
//      @QueryParam("page_size") Integer pageSize,
//      @QueryParam("offset") Integer offset,
//      @QueryParam("sort_by") String sortBy,
//      @QueryParam("sort_order") String sortOrder,
//      @QueryParam("type") String type) {
//    if (searchString == null) searchString = "";
//    if (featureGroupName == null) featureGroupName = "";
//    if (version == null) version = -1;
//    if (pageSize == null) pageSize = 10;
//    if (offset == null) offset = 0;
//    if (sortBy == null) sortBy = "name";
//    if (sortOrder == null) sortOrder = "desc";
//    if (type == null) type = "online";
//    return null;
//  }
//
//  @GET
//  @Path("/get-feature-group")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<FctAppLayerBaseResponse<GetFeatureGroupResponse>> getFeatureGroup(
//      @QueryParam("id") String featureGroupName,
//      @QueryParam("version") Integer version,
//      @QueryParam("type") String featureGroupType) {
//    return fctAppLayerService
//        .getFeatureGroup(featureGroupName, version, featureGroupType)
//        .map(
//            r ->
//                FctAppLayerBaseResponse.<GetFeatureGroupResponse>builder()
//                    .status(FctAppLayerBaseResponse.Status.SUCCESS)
//                    .statusCode(SUCCESS_HTTP_STATUS_CODE)
//                    .data(r)
//                    .build())
//        .onErrorResumeNext(
//            e ->
//                Single.just(
//                    FctAppLayerBaseResponse.<GetFeatureGroupResponse>builder()
//                        .status(FctAppLayerBaseResponse.Status.ERROR)
//                        .statusCode(ERROR_HTTP_STATUS_CODE)
//                        .build()))
//        .to(CompletableFutureUtils::fromSingle);
//  }
//
//  @GET
//  @Path("/feature-groups/{feature_group_name}/entities")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<Response<CreateEntityResponse>> getFeatureGroupEntities(
//      @PathParam("feature_group_name") String featureGroupName) {
//    return null;
//  }
//
//  @POST
//  @Path("/copy-code/features")
//  @Consumes(MediaType.WILDCARD)
//  @Produces(MediaType.APPLICATION_JSON)
//  public CompletionStage<Response<CreateEntityResponse>> featureGroupCopyCode() {
//    return null;
//  }
//}
