package com.dream11.core.dto.response.interfaces;

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
public class FeatureGroupRunMetaData {
    private String name;
    private List<RunsMetaData> data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RunsMetaData {
        private String name;
        private Double timestamp;

        @JsonProperty("time_taken")
        private Double timeTaken;

        @JsonProperty("sample_data")
        private List<SampleData> sampleData;
        private Integer count;
        private String status = RunStatus.SUCCESS.name();

        @JsonProperty("error_message")
        private String errorMessage = "";
    }

    public enum RunStatus {
        SUCCESS,
        FAILED
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SampleData {
        private String key;
        private Object value;
    }
}
