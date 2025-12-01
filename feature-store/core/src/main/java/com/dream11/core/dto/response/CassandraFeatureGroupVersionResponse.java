package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.VersionMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureGroupVersionResponse {
    VersionMetadata version;
}
