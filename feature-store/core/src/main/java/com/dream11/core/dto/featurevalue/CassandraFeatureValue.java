package com.dream11.core.dto.featurevalue;

import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featurevalue.interfaces.FeatureValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureValue implements FeatureValue {
    private CassandraFeatureColumn cassandraFeatureGroup;
    private Object value;
}
