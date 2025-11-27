package com.dream11.admin.service;

import com.dream11.admin.dao.metastore.MetaStoreRunsDao;
import com.dream11.admin.dto.esproxy.EntityResponse;
import com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse;
import com.dream11.admin.dto.helper.EsProxyBaseResponse;
import com.dream11.core.dto.response.interfaces.FeatureGroupRunMetaData;
import com.dream11.core.util.VersionUtils;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.dream11.admin.service.FctAppLayerViaESProxyService.convertGetFgResponse;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EsProxyViaMetaStoreService {
    private final CassandraMetaStoreService cassandraMetaStoreService;
    private final MetaStoreRunsDao metaStoreRunsDao;

    public Single<List<EntityResponse>> getFeatureGroupEntities(String fgName, Integer version) {
        return cassandraMetaStoreService
                .getFeatureGroupFromCacheWithLegacyResponse(fgName, VersionUtils.getVersionString(version))
                .flatMap(
                        r ->
                                Observable.fromIterable(r.getFeatureGroup().getEntities())
                                        .flatMapSingle(cassandraMetaStoreService::getEntityFromCacheWithLegacyResponse)
                                        .map(EntityResponse::getResponse)
                                        .toList());
    }

    public Single<GetFeatureGroupResponse> getFeatureGroup(
            String name, Integer version, String type) {
        return cassandraMetaStoreService
                .getFeatureGroupFromCacheWithLegacyResponse(name, VersionUtils.getVersionString(version))
                .map(getFeatureGroupResponse -> convertGetFgResponse(getFeatureGroupResponse, name, type))
                .onErrorResumeNext(
                        e -> {
                            log.error(e.getMessage());
                            return Single.just(GetFeatureGroupResponse.builder().build());
                        });
    }

    private Single<List<FeatureGroupRunMetaData.RunsMetaData>> getFeatureGroupsRunsData(FeatureGroupRunMetaData request) {
        return Single.just(request.getData());
    }
}
