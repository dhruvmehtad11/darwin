package com.dream11.core.dto.featurecolumn;


import com.dream11.core.dto.featurecolumn.interfaces.FeatureColumn;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureColumn implements FeatureColumn {

    @JsonProperty("name")
    private String featureName;

    @JsonProperty("type")
    private CassandraDataType featureDataType;

    @JsonProperty("description")
    @Builder.Default
    private String description = "";

    @JsonProperty("tags")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    public enum CassandraDataType  {
        TEXT,
        ASCII,
        VARCHAR,
        BLOB,
        BOOLEAN,
        DECIMAL,
        DOUBLE,
        FLOAT,
        INT,
        BIGINT,
        TIMESTAMP,
        TIMEUUID,
        UUID,
        INET,
        VARINT,
//        LIST,
//        SET,
//        MAP,
//        TUPLE,
//        UDT,
//        JSON  //does not work
    }

}
