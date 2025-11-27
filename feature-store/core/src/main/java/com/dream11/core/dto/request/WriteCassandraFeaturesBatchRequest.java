package com.dream11.core.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteCassandraFeaturesBatchRequest implements BatchWriteRequest {
    private List<WriteCassandraFeaturesRequest> batches;

    public static Boolean validate(WriteCassandraFeaturesBatchRequest request){
        if(request.getBatches() == null || request.getBatches().isEmpty()){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

