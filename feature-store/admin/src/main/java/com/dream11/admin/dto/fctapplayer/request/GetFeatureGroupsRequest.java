package com.dream11.admin.dto.fctapplayer.request;

import com.dream11.admin.service.EsProxyService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetFeatureGroupsRequest {
    private List<Tags> filters;
    private Integer offset;
    @JsonProperty("page_size")
    private Integer pageSize;
    @JsonProperty("search_string")
    private String searchString;
    @JsonProperty("sort_by")
    private String sortBy;
    @JsonProperty("sort_order")
    private EsProxyService.AggregationSearchOrder sortOrder;
    @Builder.Default
    private EsProxyService.FeatureGroupType type = EsProxyService.FeatureGroupType.online;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tags {
        private String name;
        private List<String> value;
    }

    public static List<Tags> getFilters(List<GetFeatureGroupCountRequest.Filter> filters) {
        List<Tags> li = new ArrayList<>();
        for (GetFeatureGroupCountRequest.Filter filter : filters) {
            li.add(Tags.builder().name(filter.getName()).value(new ArrayList<>(filter.getValue())).build());
        }
        return li;
    }
}
