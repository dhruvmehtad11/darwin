package com.dream11.core.dto.helper;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureVectorAndPrimaryKeyPair {
    private JsonObject featureVector;
    private List<Object> primaryKey;
    private Boolean failed;
    private Long size;
}
