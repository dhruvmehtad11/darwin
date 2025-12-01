package com.dream11.core.dto.response;

import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.response.interfaces.CreateFeatureGroupResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCassandraFeatureGroupResponse implements CreateFeatureGroupResponse {
    @JsonProperty("featureGroup")
    private CassandraFeatureGroup featureGroup;

    @JsonProperty("version")
    private String version;
}
