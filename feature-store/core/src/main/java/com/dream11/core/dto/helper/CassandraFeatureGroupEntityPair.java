package com.dream11.core.dto.helper;

import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureGroupEntityPair {
    private CassandraFeatureGroupMetadata featureGroupMetadata;
    private CassandraEntityMetadata entityMetadata;
}
