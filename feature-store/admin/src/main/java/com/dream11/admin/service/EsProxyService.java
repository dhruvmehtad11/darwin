package com.dream11.admin.service;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class EsProxyService {
    public enum AggregationSearchOrder {
        asc,
        desc
    }

    public enum FeatureGroupType {
        online,
        offline
    }
}
