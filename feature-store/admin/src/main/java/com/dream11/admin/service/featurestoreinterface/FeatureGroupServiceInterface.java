package com.dream11.admin.service.featurestoreinterface;

import com.dream11.core.dto.response.interfaces.CreateFeatureGroupResponse;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface FeatureGroupServiceInterface {
    Single<CreateFeatureGroupResponse> createFeatureGroup(JsonObject createFeatureGroupRequest);
}
