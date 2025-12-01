package com.dream11.admin.utils;

import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupsRequest;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;

import java.util.List;

public class FilterFeatureGroupUtils {
    public static Boolean filterFgBySearchString(CassandraFeatureGroupMetadata featureGroupMetaData, String searchString) {
        if(searchString == null || searchString.isEmpty())
            return true;
        return featureGroupMetaData.getName().toLowerCase().contains(searchString.toLowerCase())
                || featureGroupMetaData.getDescription().toLowerCase().contains(searchString.toLowerCase())
                || featureGroupMetaData.getOwner().toLowerCase().contains(searchString.toLowerCase())
                || featureGroupMetaData.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchString.toLowerCase()));

    }

    public static Boolean filterFgsByRequestFilters(CassandraFeatureGroupMetadata featureGroupMetaData, List<GetFeatureGroupsRequest.Tags> tags) {
        // add null check for tags as well
        if (tags == null || tags.isEmpty())
            return true;

        return tags
                .stream()
                .allMatch(filter -> {
                    if(filter.getValue().isEmpty())
                        return true;
                    if (filter.getName().equals("tags")) {
                        return filter
                                .getValue()
                                .stream()
                                .anyMatch(value ->
                                        featureGroupMetaData
                                                .getTags()
                                                .stream()
                                                .anyMatch(fgTag -> fgTag.equals(value)));
                    } else if(filter.getName().equals("owners")) {
                        return filter
                                .getValue()
                                .stream()
                                .anyMatch(owner -> owner
                                        .equals(featureGroupMetaData.getOwner()));
                    }

                    return true;
                });
    }
}
