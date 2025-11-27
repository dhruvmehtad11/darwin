package com.dream11.core.dto.request;

import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.request.interfaces.UpdateFeatureGroupRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCassandraFeatureGroupRequest implements UpdateFeatureGroupRequest {
    private CassandraFeatureGroupMetadata.State state;
}
