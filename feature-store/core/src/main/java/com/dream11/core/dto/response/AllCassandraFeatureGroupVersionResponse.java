package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.VersionMetadata;
import com.dream11.core.dto.response.interfaces.AllFeatureGroupVersionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllCassandraFeatureGroupVersionResponse implements AllFeatureGroupVersionResponse {
    List<VersionMetadata> versions;
}
