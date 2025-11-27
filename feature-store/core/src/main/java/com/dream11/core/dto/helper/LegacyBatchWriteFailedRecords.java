package com.dream11.core.dto.helper;

import com.dream11.core.dto.request.legacystack.LegacyWriteFeaturesRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyBatchWriteFailedRecords {
    @JsonProperty("Success")
    @Builder.Default
    private Boolean success = false;

    @JsonProperty("Err")
    private String error;

    @JsonProperty("Record")
    private LegacyWriteFeaturesRequest record;
}
