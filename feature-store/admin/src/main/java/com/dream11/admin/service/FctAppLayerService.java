package com.dream11.admin.service;

import com.dream11.admin.dto.fctapplayer.request.GetFeatureGroupCountRequest;
import com.dream11.admin.dto.fctapplayer.response.GetFeatureGroupResponse;
import com.dream11.admin.dto.fctapplayer.response.FeatureGroupCountResponse;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.util.VersionUtils;
import com.google.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FctAppLayerService {
  private final CassandraMetaStoreService cassandraMetaStoreService;

  public Single<List<GetFeatureGroupCountRequest.Filter>> getFeatureGroupFilters() {
    Single<List<String>> owners =
        cassandraMetaStoreService
            .getAllFeatureGroupsFromCache()
            .map(CassandraFeatureGroupMetadata::getOwner)
            .toList();

    Single<List<String>> fgTags =
        cassandraMetaStoreService
            .getAllFeatureGroupsFromCache()
            .map(CassandraFeatureGroupMetadata::getTags)
            .flatMap(Observable::fromIterable)
            .toList();
    Single<List<String>> featureTags =
        cassandraMetaStoreService
            .getAllFeatureGroupsFromCache()
            .map(CassandraFeatureGroupMetadata::getFeatureGroup)
            .map(CassandraFeatureGroup::getFeatures)
            .flatMap(Observable::fromIterable)
            .map(CassandraFeatureColumn::getTags)
            .flatMap(Observable::fromIterable)
            .toList();

    Single<GetFeatureGroupCountRequest.Filter> tagFilters =
        fgTags.flatMap(
            allFgTags ->
                featureTags.map(
                    allFeatureTags -> {
                      allFeatureTags.addAll(allFgTags);
                      return GetFeatureGroupCountRequest.Filter.builder()
                          .name(GetFeatureGroupCountRequest.Filter.FilterName.tags.name())
                          .value(new HashSet<>(allFeatureTags))
                          .build();
                    }));

    Single<GetFeatureGroupCountRequest.Filter> ownerFilters =
        owners.map(
            allFgOwners ->
                GetFeatureGroupCountRequest.Filter.builder()
                    .name(GetFeatureGroupCountRequest.Filter.FilterName.owners.name())
                    .value(new HashSet<>(allFgOwners))
                    .build());
    return Single.zip(tagFilters, ownerFilters, List::of);
  }

  public Single<GetFeatureGroupResponse> getFeatureGroup(
      String name, Integer version, String type) {
    String versionString = VersionUtils.getVersionString(version);
    return getAllAvailableFeatureGroupVersions(name)
        .flatMap(
            availableVersions ->
                cassandraMetaStoreService
                    .getCassandraFeatureGroup(name, versionString)
                    .map(
                        r ->
                            GetFeatureGroupResponse.builder()
                                .id(r.getName())
                                .title(r.getName())
                                .description(r.getDescription())
                                .version(version)
                                .allVersions(availableVersions)
                                //todo: fix this
                                //                  .lastValueUpdated()
                                .createdBy(r.getOwner())
                                .tags(r.getTags())
                                .type(r.getFeatureGroupType().name().toLowerCase())
                                .typesAvailable(
                                    r.getFeatureGroupType()
                                            == CassandraFeatureGroup.FeatureGroupType.ONLINE
                                        ? List.of(
                                            CassandraFeatureGroup.FeatureGroupType.ONLINE
                                                .name()
                                                .toLowerCase(),
                                            CassandraFeatureGroup.FeatureGroupType.OFFLINE
                                                .name()
                                                .toLowerCase())
                                        : List.of(
                                            CassandraFeatureGroup.FeatureGroupType.OFFLINE
                                                .name()
                                                .toLowerCase()))
                                .copyCode(new ArrayList<>())
                                .sinks(new ArrayList<>())
                                .build()));
  }

  public Single<List<Integer>> getAllAvailableFeatureGroupVersions(String name) {
    return cassandraMetaStoreService
        .getAllFeatureGroupsFromCache()
        .filter(r -> Objects.equals(r.getName(), name))
        .map(r -> VersionUtils.getVersionInteger(r.getVersion()))
        .toList();
  }

  public Single<FeatureGroupCountResponse> getFeatureGroupCount(
      String searchString,
      Map<GetFeatureGroupCountRequest.Filter.FilterName, Set<String>> filterNameSetMap) {

    return cassandraMetaStoreService
        .getAllFeatureGroupsFromCache()
        .filter(r -> filterFg(r, searchString, filterNameSetMap))
        .groupBy(r -> r.getFeatureGroup().getFeatureGroupType())
        .flatMap(
            fgTypeGroup ->
                fgTypeGroup
                    .count()
                    .map(count -> Pair.of(fgTypeGroup.getKey(), count))
                    .toObservable())
        .toMap(Pair::getKey, Pair::getValue)
        .map(
            map ->
                FeatureGroupCountResponse.builder()
                    .onlineCount(
                        map.containsKey(CassandraFeatureGroup.FeatureGroupType.ONLINE)
                            ? map.get(CassandraFeatureGroup.FeatureGroupType.ONLINE)
                            : 0)
                    .offlineCount(
                        map.containsKey(CassandraFeatureGroup.FeatureGroupType.OFFLINE)
                            ? map.get(CassandraFeatureGroup.FeatureGroupType.OFFLINE)
                            : 0)
                    .build());
  }

  // Extract function to gather all tags (Feature Group + Feature Columns)
  private static Set<String> getAllTags(
      CassandraFeatureGroupMetadata cassandraFeatureGroupMetadata) {
    Set<String> fgTagSet = new HashSet<>(cassandraFeatureGroupMetadata.getTags());
    for (CassandraFeatureColumn featureColumn :
        cassandraFeatureGroupMetadata.getFeatureGroup().getFeatures()) {
      fgTagSet.addAll(featureColumn.getTags());
    }
    return fgTagSet;
  }

  // Extract function to handle searchString filtering
  private static boolean matchesSearchString(
      CassandraFeatureGroupMetadata cassandraFeatureGroupMetadata,
      String searchString,
      Set<String> fgTagSet) {
    if (searchString == null || searchString.isEmpty()) {
      return true; // no search string, no filtering required
    }

    // Compare feature group name
    if (cassandraFeatureGroupMetadata
        .getFeatureGroup()
        .getFeatureGroupName()
        .contains(searchString)) {
      return true;
    }

    // Compare feature names
    for (CassandraFeatureColumn column :
        cassandraFeatureGroupMetadata.getFeatureGroup().getFeatures()) {
      if (column.getFeatureName().contains(searchString)) {
        return true;
      }
    }

    // Compare feature group tags
    for (String tag : fgTagSet) {
      if (tag.contains(searchString)) {
        return true;
      }
    }

    return false;
  }

  // Extract function to handle filterNameSetMap filtering
  private static boolean matchesFilterNameSetMap(
      CassandraFeatureGroupMetadata cassandraFeatureGroupMetadata,
      Map<GetFeatureGroupCountRequest.Filter.FilterName, Set<String>> filterNameSetMap,
      Set<String> fgTagSet) {
    if (filterNameSetMap == null || filterNameSetMap.isEmpty()) {
      return true; // no filters, no filtering required
    }

    // Check owner in filters
    if (filterNameSetMap.containsKey(GetFeatureGroupCountRequest.Filter.FilterName.owners)) {
      if (!filterNameSetMap
          .get(GetFeatureGroupCountRequest.Filter.FilterName.owners)
          .contains(cassandraFeatureGroupMetadata.getOwner())) {
        return false; // owner didn't match
      }
    }

    // Check tags in filters
    if (filterNameSetMap.containsKey(GetFeatureGroupCountRequest.Filter.FilterName.tags)) {
      Set<String> filteredTags = new HashSet<>(fgTagSet);
      filteredTags.retainAll(
          filterNameSetMap.get(GetFeatureGroupCountRequest.Filter.FilterName.tags));
      return !filteredTags.isEmpty(); // tags didn't match
    }

    return true;
  }

  // Main function that combines all filtering logic
  private static Boolean filterFg(
      CassandraFeatureGroupMetadata cassandraFeatureGroupMetadata,
      String searchString,
      Map<GetFeatureGroupCountRequest.Filter.FilterName, Set<String>> filterNameSetMap) {
    Set<String> fgTagSet = getAllTags(cassandraFeatureGroupMetadata);

    // Check search string matching
    if (!matchesSearchString(cassandraFeatureGroupMetadata, searchString, fgTagSet)) {
      return false; // search string didn't match
    }

    // Check filterNameSetMap matching
    return matchesFilterNameSetMap(
        cassandraFeatureGroupMetadata, filterNameSetMap, fgTagSet); // filters didn't match
  }
}
