package com.dream11.core.dto.response;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.response.interfaces.CreateEntityResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCassandraEntityResponse implements CreateEntityResponse {
    @JsonProperty("entity")
    private CassandraEntity entity;
}
