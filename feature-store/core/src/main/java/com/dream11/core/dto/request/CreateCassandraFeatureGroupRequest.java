package com.dream11.core.dto.request;

import static com.dream11.core.constant.Constants.EMAIL_REGEX;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.request.interfaces.CreateFeatureGroupRequest;
import com.dream11.core.util.FeatureNameUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCassandraFeatureGroupRequest implements CreateFeatureGroupRequest {
  @NotNull
  private CassandraFeatureGroup featureGroup;
  @NotNull
  private String owner;
  private List<String> tags;
  private String description;

  public static Boolean validateRequest(CreateCassandraFeatureGroupRequest createCassandraFeatureGroupRequest) {
    if (createCassandraFeatureGroupRequest.getFeatureGroup() == null
        || createCassandraFeatureGroupRequest.getFeatureGroup().getFeatureGroupName() == null
        || createCassandraFeatureGroupRequest.getFeatureGroup().getEntityName() == null
        || createCassandraFeatureGroupRequest.getFeatureGroup().getFeatures() == null
        || createCassandraFeatureGroupRequest.getFeatureGroup().getFeatures().isEmpty()
        || createCassandraFeatureGroupRequest.getOwner() == null
        || !createCassandraFeatureGroupRequest.getOwner().matches(EMAIL_REGEX)) {
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }

  public static Boolean validateFgAndEntity(
      CassandraFeatureGroup featureGroup, CassandraEntity cassandraEntity) {

    Set<String> entityFeatureNameSet = new HashSet<>();
    for (CassandraFeatureColumn featureColumn : cassandraEntity.getFeatures()) {
      entityFeatureNameSet.add(featureColumn.getFeatureName());
    }

    Set<String> fgFeatureNameSet = new HashSet<>();

    for (CassandraFeatureColumn featureColumn : featureGroup.getFeatures()) {
      // checking col separator not present in fg feature name
      if (!FeatureNameUtils.isFeatureNameValid(featureColumn.getFeatureName())) {
        return Boolean.FALSE;
      }
      // checking duplicates and feature col name matching entity col name
      if (entityFeatureNameSet.contains(featureColumn.getFeatureName())
          || fgFeatureNameSet.contains(featureColumn.getFeatureName())) {
        return Boolean.FALSE;
      }
      fgFeatureNameSet.add(featureColumn.getFeatureName());
    }
    return Boolean.TRUE;
  }
}
