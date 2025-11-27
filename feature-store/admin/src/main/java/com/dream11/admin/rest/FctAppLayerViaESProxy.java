package com.dream11.admin.rest;

import com.dream11.admin.dto.esproxy.EntityResponse;
import com.dream11.admin.dto.fctapplayer.request.CopyCodeRequest;
import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupCountRequest;
import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupsRequest;
import com.dream11.admin.dto.fctapplayer.response.FeatureGroupCountResponse;
import com.dream11.admin.dto.fctapplayer.response.FeatureGroupFeatureResponse;
import com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse;
import com.dream11.admin.dto.fctapplayer.response.RunDataResponse;
import com.dream11.admin.dto.helper.FctAppLayerBaseResponse;
import com.dream11.admin.dto.helper.FctAppLayerBaseResponseWithPageAndOffset;
import com.dream11.admin.service.EsProxyViaMetaStoreService;
import com.dream11.admin.service.FctAppLayerViaESProxyService;
import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.error.ApiRestException;
import com.dream11.core.util.VersionUtils;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.dream11.core.constant.Constants.ERROR_HTTP_STATUS_CODE;
import static com.dream11.core.constant.Constants.SUCCESS_HTTP_STATUS_CODE;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Path("/FeatureStore")
public class FctAppLayerViaESProxy {
    private final FctAppLayerViaESProxyService fctAppLayerViaESProxyService;
    private final EsProxyViaMetaStoreService esProxyViaMetaStoreService;

    @GET
    @Path("/feature-group-runs")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponse<List<RunDataResponse>>> getFeatureGroupRuns(
            @QueryParam("fg_name")
            String fgName,

            @QueryParam("version")
            @DefaultValue("1")
            Integer version) {
        return fctAppLayerViaESProxyService
                .getFeatureGroupsFromMetaStore(fgName, VersionUtils.getVersionString(version))
                .map(
                        r ->
                                FctAppLayerBaseResponse.<List<RunDataResponse>>builder()
                                        .status(FctAppLayerBaseResponse.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e ->
                                Single.just(
                                        FctAppLayerBaseResponse.<List<RunDataResponse>>builder()
                                                .status(FctAppLayerBaseResponse.Status.ERROR)
                                                .statusCode(ERROR_HTTP_STATUS_CODE)
                                                .build()))
                .to(CompletableFutureUtils::fromSingle);
    }

    @POST
    @Path("/get-feature-groups")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponseWithPageAndOffset<List<GetFeatureGroupResponse>>>
    getFeatureGroups(@RequestBody GetFeatureGroupsRequest request) {
        return fctAppLayerViaESProxyService
                .getFeatureGroups(request)
                .to(CompletableFutureUtils::fromSingle);
    }


    @POST
    @Path("/feature-groups/count")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponse<FeatureGroupCountResponse>> getFeatureGroupsCount(
            GetFeatureGroupCountRequest getFeatureGroupCountRequest) {
        return fctAppLayerViaESProxyService
                .getFeatureGroupCount(getFeatureGroupCountRequest)
                .map(
                        r ->
                                FctAppLayerBaseResponse.<FeatureGroupCountResponse>builder()
                                        .status(FctAppLayerBaseResponse.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e ->
                        {
                            log.error("get feature groups count error:: ", e);
                            return Single.just(
                                    FctAppLayerBaseResponse.<FeatureGroupCountResponse>builder()
                                            .status(FctAppLayerBaseResponse.Status.ERROR)
                                            .statusCode(ERROR_HTTP_STATUS_CODE)
                                            .build());
                        })
                .to(CompletableFutureUtils::fromSingle);
    }

    @GET
    @Path("/group-filters")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponse<List<GetFeatureGroupCountRequest.Filter>>>
    getFeatureGroupFilters() {
        return fctAppLayerViaESProxyService
                .getFeatureGroupFilters()
                .map(
                        r ->
                                FctAppLayerBaseResponse.<List<GetFeatureGroupCountRequest.Filter>>builder()
                                        .status(FctAppLayerBaseResponse.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e ->
                                Single.just(
                                        FctAppLayerBaseResponse.<List<GetFeatureGroupCountRequest.Filter>>builder()
                                                .status(FctAppLayerBaseResponse.Status.ERROR)
                                                .statusCode(ERROR_HTTP_STATUS_CODE)
                                                .build()))
                .to(CompletableFutureUtils::fromSingle);
    }

    @GET
    @Path("/get-feature-of-feature-group")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<
            FctAppLayerBaseResponseWithPageAndOffset<List<FeatureGroupFeatureResponse>>>
    getFeatureGroupFeatures(
            @QueryParam("feature_group_id")
            @DefaultValue("")
            String featureGroupName,

            @QueryParam("search_string")
            @DefaultValue("")
            String searchString,

            @QueryParam("version")
            @DefaultValue("1")
            Integer version,

            @QueryParam("page_size")
            @DefaultValue("10")
            Integer pageSize,

            @QueryParam("offset")
            @DefaultValue("0")
            Integer offset,

            @QueryParam("sort_by")
            @DefaultValue("name")
            String sortBy,

            @QueryParam("sort_order")
            @DefaultValue("desc")
            String sortOrder,

            @QueryParam("type")
            @DefaultValue("online")
            String type
    ) {
        return fctAppLayerViaESProxyService
                .getFeatureGroupFeatures(
                        searchString, featureGroupName, version, pageSize, offset, sortBy, sortOrder, type)
                .map(
                        r ->
                                FctAppLayerBaseResponseWithPageAndOffset
                                        .<List<FeatureGroupFeatureResponse>>builder()
                                        .status(FctAppLayerBaseResponseWithPageAndOffset.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .pageSize(pageSize)
                                        .offset(offset)
                                        .totalRecordsCount(r.size())
                                        .resultSize(r.size())
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e -> {
                            log.error("error in getting features of FGs {}", e.toString());
                            return Single.just(
                                    FctAppLayerBaseResponseWithPageAndOffset
                                            .<List<FeatureGroupFeatureResponse>>builder()
                                            .status(FctAppLayerBaseResponseWithPageAndOffset.Status.ERROR)
                                            .statusCode(ERROR_HTTP_STATUS_CODE)
                                            .build());
                        })
                .to(CompletableFutureUtils::fromSingle);
    }

    @GET
    @Path("/get-feature-group")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponse<GetFeatureGroupResponse>> getFeatureGroup(
            @QueryParam("id") String featureGroupName,
            @QueryParam("version")
            @DefaultValue("1")
            Integer version,
            @QueryParam("type") String featureGroupType) {
        return esProxyViaMetaStoreService
                .getFeatureGroup(featureGroupName, version, featureGroupType)
                .map(
                        r ->
                                FctAppLayerBaseResponse.<GetFeatureGroupResponse>builder()
                                        .status(FctAppLayerBaseResponse.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e ->
                                Single.just(
                                        FctAppLayerBaseResponse.<GetFeatureGroupResponse>builder()
                                                .status(FctAppLayerBaseResponse.Status.ERROR)
                                                .statusCode(ERROR_HTTP_STATUS_CODE)
                                                .build()))
                .to(CompletableFutureUtils::fromSingle);
    }

    @GET
    @Path("/feature-groups/{feature_group_name}/entities")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponse<List<EntityResponse>>> getFeatureGroupEntities(
            @PathParam("feature_group_name") String featureGroupName,
            @QueryParam("version") Integer version) {
        return esProxyViaMetaStoreService
                .getFeatureGroupEntities(featureGroupName, version)
                .map(
                        r ->
                                FctAppLayerBaseResponse.<List<EntityResponse>>builder()
                                        .status(FctAppLayerBaseResponse.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e -> {
                            if (e instanceof ApiRestException) {
                                return Single.just(
                                        FctAppLayerBaseResponse.<List<EntityResponse>>builder()
                                                .status(FctAppLayerBaseResponse.Status.ERROR)
                                                .statusCode(((ApiRestException) e).getHttpStatusCode()) // Set the 404 status code
                                                .build());
                            }
                            return Single.just(
                                    FctAppLayerBaseResponse.<List<EntityResponse>>builder()
                                            .status(FctAppLayerBaseResponse.Status.ERROR)
                                            .statusCode(ERROR_HTTP_STATUS_CODE)
                                            .build());
                        })
                .to(CompletableFutureUtils::fromSingle);
    }

    @POST
    @Path("/copy-code/features")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<FctAppLayerBaseResponse<String>> featureGroupCopyCode(
            CopyCodeRequest copyCodeRequest) {
        return Single.just(
                        FctAppLayerViaESProxyService.getCopyCodeForFeatures(
                                copyCodeRequest.getFeatureGroupName(), copyCodeRequest.getFeatureTitles()))
                .map(
                        r ->
                                FctAppLayerBaseResponse.<String>builder()
                                        .status(FctAppLayerBaseResponse.Status.SUCCESS)
                                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                                        .data(r)
                                        .build())
                .onErrorResumeNext(
                        e ->
                                Single.just(
                                        FctAppLayerBaseResponse.<String>builder()
                                                .status(FctAppLayerBaseResponse.Status.ERROR)
                                                .statusCode(ERROR_HTTP_STATUS_CODE)
                                                .build()))
                .to(CompletableFutureUtils::fromSingle);
    }
}
