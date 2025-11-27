package com.dream11.core.dto.response;

import com.dream11.core.dto.response.interfaces.WriteFeaturesResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteCassandraFeaturesResponse implements WriteFeaturesResponse {
    private String featureGroupName;
    private String featureGroupVersion;
    private List<String> featureColumns;
    private List<List<Object>> successfulRows;
    private List<List<Object>> failedRows;

    @JsonIgnore
    private List<Long> payloadSizes;
}
