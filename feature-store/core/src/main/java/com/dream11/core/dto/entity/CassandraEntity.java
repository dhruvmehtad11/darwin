package com.dream11.core.dto.entity;

import com.dream11.core.dto.entity.interfaces.Entity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraEntity implements Entity {
    private String tableName;
    private List<CassandraFeatureColumn> features;
    private List<String> primaryKeys;
    private Long ttl;

    public static Boolean searchSubString(CassandraEntity cassandraEntity, String searchPattern){
        if(cassandraEntity.getTableName().contains(searchPattern))
            return Boolean.TRUE;
        for (CassandraFeatureColumn feature : cassandraEntity.getFeatures()) {
            if(feature.getFeatureName().contains(searchPattern))
                return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
