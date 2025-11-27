package com.dream11.core.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureBatchData {
    private String featureGroupName;
    private String featureGroupVersion;
    private List<String> featureNames;
    private List<List<Object>> featureValues;
}
