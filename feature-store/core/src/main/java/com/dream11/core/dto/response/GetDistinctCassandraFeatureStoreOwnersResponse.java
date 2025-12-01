package com.dream11.core.dto.response;

import com.dream11.core.dto.response.interfaces.GetDistinctOwnersResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetDistinctCassandraFeatureStoreOwnersResponse implements GetDistinctOwnersResponse {
    private List<String> owners;
}
