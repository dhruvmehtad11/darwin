package com.dream11.core.dto.response;

import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.response.interfaces.GetFeatureGroupSchemaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCassandraFeatureGroupSchemaResponse implements GetFeatureGroupSchemaResponse {
    private List<CassandraFeatureColumn> schema;
    private List<String> primaryKeys;
    private String version;
    private CassandraFeatureGroup.FeatureGroupType featureGroupType;
}
