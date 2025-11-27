package com.dream11.admin.service;

import static com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupsRequest.getFilters;
import static com.dream11.admin.utils.FilterFeatureGroupUtils.filterFgBySearchString;
import static com.dream11.admin.utils.FilterFeatureGroupUtils.filterFgsByRequestFilters;
import static com.dream11.core.constant.Constants.ERROR_HTTP_STATUS_CODE;
import static com.dream11.core.constant.Constants.SUCCESS_HTTP_STATUS_CODE;
import static com.dream11.core.util.VersionUtils.getVersionInteger;
import static com.dream11.core.util.VersionUtils.getVersionString;

import com.dream11.admin.dao.metastore.MetaStoreRunsDao;
import com.dream11.admin.dto.esproxy.AggregationSearchFeatureGroupResponse;
import com.dream11.admin.dto.esproxy.GetFeatureGroupRunResponse;
import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupCountRequest;
import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupsRequest;
import com.dream11.admin.dto.fctapplayer.response.FeatureGroupCountResponse;
import com.dream11.admin.dto.fctapplayer.response.FeatureGroupFeatureResponse;
import com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse;
import com.dream11.admin.dto.fctapplayer.response.RunDataResponse;
import com.dream11.admin.dto.helper.FctAppLayerBaseResponseWithPageAndOffset;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.response.GetDistinctCassandraFeatureStoreOwnersResponse;
import com.dream11.core.dto.response.GetDistinctCassandraFeatureStoreTagsResponse;
import com.dream11.core.util.VersionUtils;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FctAppLayerViaESProxyService {
    private final CassandraMetaStoreSearchService cassandraMetaStoreSearchService;
    private final CassandraMetaStoreService cassandraMetaStoreService;
    private final MetaStoreRunsDao metaStoreRunsDao;


    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    public Single<List<GetFeatureGroupCountRequest.Filter>> getFeatureGroupFilters() {
        Single<GetDistinctCassandraFeatureStoreTagsResponse> getTags = cassandraMetaStoreSearchService.getFeatureGroupTags();
        Single<GetDistinctCassandraFeatureStoreOwnersResponse> getOwners = cassandraMetaStoreSearchService.getFeatureGroupOwners();

        return Single.zip(
                getTags,
                getOwners,
                (tags, owners) -> {
                    Set<String> distinctTags = new TreeSet<>() {
                    };
                    Set<String> distinctOwners = new TreeSet<>() {
                    };

                    distinctTags.addAll(tags.getTags());
                    distinctOwners.addAll(owners.getOwners());

                    return List.of(
                            GetFeatureGroupCountRequest.Filter.builder()
                                    .name(GetFeatureGroupCountRequest.Filter.FilterName.tags.name())
                                    .value(distinctTags)
                                    .build(),
                            GetFeatureGroupCountRequest.Filter.builder()
                                    .name(GetFeatureGroupCountRequest.Filter.FilterName.owners.name())
                                    .value(distinctOwners)
                                    .build());
                });
    }

    public Single<FeatureGroupCountResponse> getFeatureGroupCount(
            GetFeatureGroupCountRequest request) {
        Single<Long> onlineCount =
                getFeatureGroups(
                        GetFeatureGroupsRequest.builder()
                                .filters(getFilters(request.getFilters()))
                                .type(EsProxyService.FeatureGroupType.online)
                                .searchString(request.getSearchString())
                                .sortOrder(EsProxyService.AggregationSearchOrder.asc)
                                .sortBy("name")
                                .offset(0)
                                .pageSize(10)
                                .build())
                        .map(r -> (long) r.getTotalRecordsCount());

        Single<Long> offlineCount =
                getFeatureGroups(
                        GetFeatureGroupsRequest.builder()
                                .filters(getFilters(request.getFilters()))
                                .type(EsProxyService.FeatureGroupType.offline)
                                .searchString(request.getSearchString())
                                .sortOrder(EsProxyService.AggregationSearchOrder.asc)
                                .sortBy("name")
                                .offset(0)
                                .pageSize(10)
                                .build())
                        .map(r -> (long) r.getTotalRecordsCount());

        return Single.zip(
                onlineCount,
                offlineCount,
                (a, b) -> FeatureGroupCountResponse.builder().onlineCount(a).offlineCount(b).build());
    }

    public Single<FctAppLayerBaseResponseWithPageAndOffset<List<GetFeatureGroupResponse>>> getFeatureGroups(GetFeatureGroupsRequest request) {
        return cassandraMetaStoreService
                .getAllFeatureGroupsFromCache()
                // filter by fg type
                .filter(f -> {
                    if (request.getType() != null && request.getType().name().equalsIgnoreCase(EsProxyService.FeatureGroupType.offline.name())) {
                        return true;
                    }
                    return f
                            .getFeatureGroupType()
                            .name()
                            .equalsIgnoreCase(EsProxyService.FeatureGroupType.online.name());
                })
                // filter fgs using name, description, created by, tags and then return the response
                .filter(featureGroupMetaData ->
                        filterFgBySearchString(featureGroupMetaData, request.getSearchString())
                                && filterFgsByRequestFilters(featureGroupMetaData, request.getFilters()))
                .sorted((f1, f2) -> {
                    // currently sort by name is only possible
                    int comparison = f1.getFeatureGroup().getFeatureGroupName().compareTo(f2.getFeatureGroup().getFeatureGroupName());
                    return request.getSortOrder() == EsProxyService.AggregationSearchOrder.asc ? comparison : -comparison;
                })
                .map(fg -> getFeatureGroupResponse(fg, request))
            .groupBy(GetFeatureGroupResponse::getId)
            .flatMapSingle(groupedFg ->
                groupedFg.toList().map(list -> {
                  List<Integer> versions = list.stream()
                      .map(GetFeatureGroupResponse::getVersion)
                      .collect(Collectors.toList());

                  GetFeatureGroupResponse latest = list.stream()
                      .max(Comparator.comparingInt(GetFeatureGroupResponse::getVersion))
                      .orElseThrow();

                  latest.setAllVersions(versions);
                  return latest;
                }))
            .flatMap(this::setFeatureGroupRuns)
                .toList()
                .map(fg -> getPaginatedFgResponse(fg, request.getOffset(), request.getPageSize()))
                .map(response -> getFctAppLayerBaseResponseWithPageAndOffset(response, request, response.size()))
                .onErrorResumeNext(e -> handleError(e, "error fetching feature group"));
    }

    private Observable<GetFeatureGroupResponse> setFeatureGroupRuns(GetFeatureGroupResponse fg) {
        if (fg.getType().equalsIgnoreCase(CassandraFeatureGroup.FeatureGroupType.OFFLINE.name())) {
            return
                    getFeatureGroupsFromMetaStore(fg.getTitle(), VersionUtils.getVersionString(fg.getVersion()))
                            .onErrorResumeNext(e -> {
                                log.error(e.getMessage());
                                return Single.just(new ArrayList<>());
                            })
                            .flattenAsObservable(runs -> runs)
                            .map(run -> run.getStatus() == null || run.getStatus().isEmpty() ? "FAILED" : run.getStatus())
                            .toList()
                            .map(statuses -> {
                                fg.setLastFiveRuns(statuses);
                                return fg;
                            })
                            .toObservable();
        }
        return Observable.just(fg);
    }

    private <T> List<T> getPaginatedFgResponse(List<T> fgList, Integer offset, Integer pageSize) {
        // get all elements of this list from index offset to offset+pageSize
        List<T> response = new ArrayList<T>();
        for (int i = offset; i < offset + pageSize && i < fgList.size(); i++) {
            response.add(fgList.get(i));
        }
        return response;
    }

    private FctAppLayerBaseResponseWithPageAndOffset<List<GetFeatureGroupResponse>> getFctAppLayerBaseResponseWithPageAndOffset(List<GetFeatureGroupResponse> fgResponse, GetFeatureGroupsRequest request, Integer totalSize) {
        return FctAppLayerBaseResponseWithPageAndOffset.<List<GetFeatureGroupResponse>>builder()
                .status(FctAppLayerBaseResponseWithPageAndOffset.Status.SUCCESS)
                .statusCode(SUCCESS_HTTP_STATUS_CODE)
                .pageSize(request.getPageSize())
                .offset(request.getOffset())
                .totalRecordsCount(totalSize)
                .resultSize(fgResponse.size())
                .data(fgResponse)
                .build();
    }

    private GetFeatureGroupResponse getFeatureGroupResponse(CassandraFeatureGroupMetadata fg, GetFeatureGroupsRequest request) {
        return GetFeatureGroupResponse.builder()
                .id(fg.getFeatureGroup().getFeatureGroupName())
                .title(fg.getFeatureGroup().getFeatureGroupName())
                .description(fg.getDescription())
                .version(getVersionInteger(fg.getVersion()))
                .allVersions(List.of(getVersionInteger(fg.getVersion())))
                .lastValueUpdated(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(fg.getCreatedAt()))
                .createdBy(fg.getOwner())
                .tags(fg.getTags())
                .type(request.getType().name())
                .typesAvailable(
                        fg.getFeatureGroupType().name().equals(CassandraFeatureGroup.FeatureGroupType.ONLINE.name())
                                ? List.of(
                                CassandraFeatureGroup.FeatureGroupType.ONLINE.name().toLowerCase(),
                                CassandraFeatureGroup.FeatureGroupType.OFFLINE.name().toLowerCase())
                                : List.of(CassandraFeatureGroup.FeatureGroupType.OFFLINE.name().toLowerCase())
                )
                .copyCode(
                        List.of(
                                GetFeatureGroupResponse.CopyCode.builder()
                                        .name("Spark Data Frame")
                                        .value(getCopyCodeStatements(fg.getFeatureGroup().getFeatureGroupName(), request.getType().name()))
                                        .build()
                        )
                )
                .sinks(List.of(GetFeatureGroupResponse.Sinks.builder().location("").type("").build()))
                .lastFiveRuns(new ArrayList<>())
                .build();
    }


    private Single<FctAppLayerBaseResponseWithPageAndOffset<List<GetFeatureGroupResponse>>> buildFinalResponse(
            GetFeatureGroupsRequest request,
            AggregationSearchFeatureGroupResponse aggregationSearchFeatureGroupResponse,
            List<GetFeatureGroupResponse> featureGroupResponses) {

        return Single.just(
                FctAppLayerBaseResponseWithPageAndOffset.<List<GetFeatureGroupResponse>>builder()
                        .status(FctAppLayerBaseResponseWithPageAndOffset.Status.SUCCESS)
                        .statusCode(SUCCESS_HTTP_STATUS_CODE)
                        .pageSize(request.getPageSize())
                        .offset(request.getOffset())
                        .totalRecordsCount(aggregationSearchFeatureGroupResponse.getResponse().getTotal().getValue())
                        .resultSize(aggregationSearchFeatureGroupResponse.getResponse().getAggregations().size())
                        .data(featureGroupResponses)
                        .build());
    }

    private Single<FctAppLayerBaseResponseWithPageAndOffset<List<GetFeatureGroupResponse>>> handleError(Throwable e, String message) {
        log.error(message, e);
        return Single.just(
                FctAppLayerBaseResponseWithPageAndOffset.<List<GetFeatureGroupResponse>>builder()
                        .status(FctAppLayerBaseResponseWithPageAndOffset.Status.ERROR)
                        .statusCode(ERROR_HTTP_STATUS_CODE)
                        .build());
    }

    public static GetFeatureGroupResponse convertGetFgResponse(
            com.dream11.admin.dto.esproxy.GetFeatureGroupResponse getFeatureGroupResponse,
            String name,
            String type) {
        return GetFeatureGroupResponse.builder()
                .id(getFeatureGroupResponse.getFeatureGroup().getName())
                .title(getFeatureGroupResponse.getFeatureGroup().getName())
                .description(getFeatureGroupResponse.getFeatureGroup().getDescription())
                .version(getFeatureGroupResponse.getFeatureGroup().getVersion())
                .allVersions(List.of(getFeatureGroupResponse.getFeatureGroup().getVersion()))
                .lastValueUpdated(getFeatureGroupResponse.getFeatureGroup().getCreatedAt())
                .createdBy(getFeatureGroupResponse.getFeatureGroup().getUser())
                .tags(getFeatureGroupResponse.getFeatureGroup().getTags())
                .type(type)
                .typesAvailable(
                        getFeatureGroupResponse.getFeatureGroup().getIsOnlineEnabled()
                                ? List.of(
                                CassandraFeatureGroup.FeatureGroupType.ONLINE.name().toLowerCase(),
                                CassandraFeatureGroup.FeatureGroupType.OFFLINE.name().toLowerCase())
                                : List.of(CassandraFeatureGroup.FeatureGroupType.OFFLINE.name().toLowerCase()))
                .copyCode(
                        List.of(
                                GetFeatureGroupResponse.CopyCode.builder()
                                        .name("Spark Data Frame")
                                        .value(getCopyCodeStatements(name, type))
                                        .build()))
                .sinks(getFeatureGroupResponse.getFeatureGroup().getSinks())
                .build();
    }

    public Single<List<FeatureGroupFeatureResponse>> getFeatureGroupFeatures(
            String searchString,
            String featureGroupName,
            Integer version,
            Integer pageSize,
            Integer offset,
            String sortBy,
            String sortOrder,
            String type) {
        return cassandraMetaStoreService
                .getFeatureGroupFromCacheWithLegacyResponse(featureGroupName, getVersionString(version))
                .map(r -> r.getFeatureGroup().getSchema())
                .flattenAsObservable(r -> r)
                .map(r -> getFeatureGroupFeatures(r, featureGroupName))
                .filter(response -> filterFeatureGroupFeatures(response, searchString))
                .sorted((f1, f2) -> {
                    int comparison = f1.getTitle().compareTo(f2.getTitle());
                    return Objects.equals(sortOrder, EsProxyService.AggregationSearchOrder.asc.name()) ? comparison : -comparison;
                })
                .toList()
                .map(fgList -> {
                    log.info("list length {}", fgList.size());
                    return getPaginatedFgResponse(fgList, offset, pageSize);
                });
    }

    private Boolean filterFeatureGroupFeatures(
            FeatureGroupFeatureResponse response, String searchString) {
        return
                response.getTitle().toLowerCase().contains(searchString.toLowerCase())
                        || response.getDescription().toLowerCase().contains(searchString.toLowerCase())
                        || response.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchString.toLowerCase()))
                        || response.getType().toLowerCase().contains(searchString);
    }

    private FeatureGroupFeatureResponse getFeatureGroupFeatures(com.dream11.admin.dto.esproxy.GetFeatureGroupResponse.FeatureGroup.SubSchema fg, String fgName) {

        return FeatureGroupFeatureResponse.builder()
                .title(fg.getName())
                .description(fg.getDescription())
                .tags(fg.getTags())
                .type(fg.getType())
                .copyCode(
                        List.of(
                                GetFeatureGroupResponse.CopyCode.builder()
                                        .name("Spark Data Frame")
                                        .value(
                                                getCopyCodeForFeatures(fgName, List.of(fg.getName())))
                                        .build()))
                .isPrimaryKey(true)
                .build();
    }

    public static String getCopyCodeStatements(String feature_group_name, String feature_group_type) {
        if (Objects.equals(feature_group_type, "offline"))
            return String.format(
                    "import darwin\ndarwin.init()\ndarwin.get_registered_feature_group('%s').get_offline_features()"
                            + ".to_spark_df()",
                    feature_group_name);
        return String.format(
                "import darwin\ndarwin.init()\ndarwin.get_registered_feature_group('%s').get_online_features(entity_name=${{entity}}, "
                        + "primary_keys_with_values = ${{join_keys_dict}})",
                feature_group_name);
    }

    public static String getCopyCodeForFeatures(String featureGroupName, List<String> featureTitles) {
        StringBuilder featuresListStr = new StringBuilder("[");

        for (int i = 0; i < featureTitles.size(); i++) {
            featuresListStr.append("'").append(featureTitles.get(i)).append("'");
            if (i < featureTitles.size() - 1) {
                featuresListStr.append(", ");
            }
        }
        featuresListStr.append("]");

        return "import darwin;\ndarwin.init();\ndarwin.get_registered_feature_group('"
                + featureGroupName
                + "')"
                + ".get_online_features(entity_name=${entity}, primary_keys_with_values=${join_keys_dict}, "
                + "features="
                + featuresListStr
                + ")";
    }

    public Single<List<RunDataResponse>> getFeatureGroupsFromMetaStore(String fgName, String version) {
        return metaStoreRunsDao
                .getRunsForFg(fgName, version)
                .flattenAsObservable(r -> r)
                .map(
                        r -> GetFeatureGroupRunResponse.FeatureGroupRun.RunData
                                .builder()
                                .name(r.getString("name"))
                                .count(r.getInteger("row_count"))
                                .errorMessage(r.getString("error_message"))
                                .timeTaken(r.getDouble("time_taken"))
                                .status(r.getString("status"))
                                .timestamp(r.getDouble("timestamp"))
                                .sampleData(r.
                                        getJsonArray("sample_data")
                                        .stream()
                                        .map(obj -> ((JsonObject) obj).getMap())
                                        .collect(Collectors.toList()))
                                .build()
                )
                .map(RunDataResponse::getResponse)
                .toList();
    }
}
