package com.dream11.core.dto.response;

import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.response.interfaces.SearchEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCassandraEntityResponse implements SearchEntityResponse {
    private List<CassandraEntityMetadata> entities;
}
