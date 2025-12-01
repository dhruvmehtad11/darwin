package com.dream11.core.dto.request;

import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.request.interfaces.UpdateEntityRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCassandraEntityRequest implements UpdateEntityRequest {
    private CassandraEntityMetadata.State state;
}
