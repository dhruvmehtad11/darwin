package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.response.interfaces.SearchEntityResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllCassandraEntityResponse implements SearchEntityResponse {
    private List<CassandraEntityMetadata> entities;
}
