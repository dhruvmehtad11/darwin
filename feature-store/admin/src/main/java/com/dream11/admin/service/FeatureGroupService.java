package com.dream11.admin.service;

import com.dream11.admin.dto.fctapplayer.response.RunDataResponse;
import com.dream11.core.constant.Constants;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.TenantConfig;
import com.dream11.core.dto.request.FeatureGroupRunDataRequest;
import com.dream11.core.dto.request.UpdateFeatureGroupTtlRequest;
import com.dream11.core.dto.request.interfaces.UpdateFeatureGroupRequest;
import com.dream11.core.dto.response.*;
import com.dream11.core.dto.response.interfaces.*;
import com.dream11.core.dto.tenant.GetAllTenantResponse;
import com.dream11.core.dto.tenant.TenantDto;
import com.dream11.core.dto.tenant.UpdateTenantRequest;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FeatureGroupService {
  private final CassandraFeatureStoreService cassandraFeatureStoreService;
  private final CassandraMetaStoreService cassandraMetaStoreService;

  public Single<CreateFeatureGroupResponse> createFeatureGroup(
      JsonObject createFeatureGroupRequest,
      Constants.FeatureStoreType featureStoreType,
      Boolean upgradeVersion) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService.createFeatureGroup(
            createFeatureGroupRequest, upgradeVersion);
    }
  }

  public Single<CreateFeatureGroupResponse> registerFeatureGroup(
      JsonObject createFeatureGroupRequest,
      Constants.FeatureStoreType featureStoreType,
      Boolean upgradeVersion) {
    switch (featureStoreType) {
      default:
        return cassandraFeatureStoreService.createFeatureGroup(
            createFeatureGroupRequest, upgradeVersion, true);
    }
  }

  public Single<CreateFeatureGroupResponse> getFeatureGroup(
      String name, String version, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
            .getCassandraFeatureGroup(name, version)
            .map(
                e ->
                    CreateCassandraFeatureGroupResponse.builder()
                        .featureGroup(e.getFeatureGroup())
                        .version(e.getVersion())
                        .build());
    }
  }

  public Single<UpdateFeatureGroupRequest> updateFeatureGroup(
      String name,
      String version,
      Constants.FeatureStoreType featureStoreType,
      JsonObject updateFeatureGroupRequest) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
            .updateCassandraFeatureGroup(name, version, updateFeatureGroupRequest)
            .map(r -> r);
    }
  }

  public Single<GetFeatureGroupMetadataResponse> getFeatureGroupMetadata(
      String name, String version, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
            .getCassandraFeatureGroupMetaData(name, version)
            .map(
                r ->
                    GetCassandraFeatureGroupMetadataResponse.builder()
                        .featureGroupMetadata(r)
                        .build());
    }
  }

  public Single<GetFeatureGroupSchemaResponse> getFeatureGroupSchema(
      String name, String version, Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getCassandraFeatureGroupSchema(name, version).map(r -> r);
    }
  }

  public Single<String> getAllFeatureGroups(
      Constants.FeatureStoreType featureStoreType, Boolean compressedResponse) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getAllFeatureGroupsFromCacheCompressed(compressedResponse);
    }
  }

  public Single<String> getAllUpdatedFeatureGroups(
      Constants.FeatureStoreType featureStoreType, Long timestamp, Boolean compressedResponse) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getUpdatedCassandraFeatureGroupsFromCacheCompressed(
            timestamp, compressedResponse);
    }
  }

  public Single<CassandraFeatureGroupVersionResponse> getFeatureGroupLatestVersion(
      Constants.FeatureStoreType featureStoreType, String featureGroupName) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService
            .getCassandraFeatureGroupVersion(featureGroupName)
            .map(r -> CassandraFeatureGroupVersionResponse.builder().version(r).build());
    }
  }

  public Single<String> getAllFeatureGroupLatestVersion(
      Constants.FeatureStoreType featureStoreType, Boolean compressedResponse) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getAllFeatureGroupVersionFromCacheCompressed(
            compressedResponse);
    }
  }

  public Single<String> getAllUpdatedFeatureGroupLatestVersion(
      Constants.FeatureStoreType featureStoreType, Long timestamp, Boolean compressedResponse) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getUpdatedCassandraFeatureGroupVersionFromCacheCompressed(
            timestamp, compressedResponse);
    }
  }

  public Completable addTenant(
      Constants.FeatureStoreType featureStoreType, String fgName, TenantConfig tenantConfig) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.addTenant(fgName, tenantConfig);
    }
  }

  public Single<TenantDto> getTenant(Constants.FeatureStoreType featureStoreType, String fgName) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getTenant(fgName);
    }
  }

  public Completable updateTtl(
      Constants.FeatureStoreType featureStoreType, UpdateFeatureGroupTtlRequest request) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.updateTtl(request);
    }
  }

  public Single<String> getTenantTopic(Constants.FeatureStoreType featureStoreType, String fgName) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getFeatureGroupTenantTopic(fgName);
    }
  }

  public Completable putFeatureGroupRun(
      Constants.FeatureStoreType featureStoreType,
      FeatureGroupRunDataRequest featureGroupRunDataRequest) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.putRunData(featureGroupRunDataRequest);
    }
  }

  public Single<List<FeatureGroupRunDataResponse>> getFeatureGroupRun(
      Constants.FeatureStoreType featureStoreType,
      String name, String version) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getRunData(name, version);
    }
  }

  public Single<AllCassandraFeatureGroupResponse> getAllFeatureGroupsForTenant(
      Constants.FeatureStoreType featureStoreType, String tenantName,  Constants.FeatureStoreTenantType tenantType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getAllFeatureGroupsForTenant(tenantName, tenantType)
            .toList()
            .map(li -> AllCassandraFeatureGroupResponse.builder().featureGroups(li).build());
    }
  }

  public Completable updateAllFeatureGroupTenant(
      Constants.FeatureStoreType featureStoreType, UpdateTenantRequest request) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.updateAllFeatureGroupsForTenant(request);
    }
  }

  public Single<GetAllTenantResponse> getAllTenants(
      Constants.FeatureStoreType featureStoreType) {
    switch (featureStoreType) {
      default:
        return cassandraMetaStoreService.getAllTenants();
    }
  }
}
