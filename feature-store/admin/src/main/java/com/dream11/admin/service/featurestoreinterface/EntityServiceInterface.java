package com.dream11.admin.service.featurestoreinterface;

import com.dream11.core.dto.response.interfaces.CreateEntityResponse;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

public interface EntityServiceInterface {
    Single<CreateEntityResponse> createEntity(JsonObject createEntityRequest);
}
