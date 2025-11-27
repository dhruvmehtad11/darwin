package com.dream11.core.dto.request;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import com.dream11.core.dto.request.interfaces.CreateEntityRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

import static com.dream11.core.constant.Constants.EMAIL_REGEX;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCassandraEntityRequest implements CreateEntityRequest {
  @NotNull
  private CassandraEntity entity;
  @NotNull
  private String owner;
  private List<String> tags;
  private String description;

  public static Boolean validateRequest(CreateCassandraEntityRequest createCassandraEntityRequest) {
    if (createCassandraEntityRequest.getEntity() == null
        || createCassandraEntityRequest.getEntity().getTableName() == null
        || createCassandraEntityRequest.getEntity().getFeatures() == null
        || createCassandraEntityRequest.getEntity().getFeatures().isEmpty()
        || createCassandraEntityRequest.getEntity().getPrimaryKeys() == null
        || createCassandraEntityRequest.getEntity().getPrimaryKeys().isEmpty()
        || createCassandraEntityRequest.getOwner() == null
        || !createCassandraEntityRequest.getOwner().matches(EMAIL_REGEX)) {
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  public static Boolean validateSchema(CreateCassandraEntityRequest createCassandraEntityRequest) {
    Set<String> featureNames = new HashSet<>();
    for (CassandraFeatureColumn featureColumn : createCassandraEntityRequest.entity.getFeatures()) {
      // checking duplicates
      if (featureNames.contains(featureColumn.getFeatureName())) {
        return Boolean.FALSE;
      }
      featureNames.add(featureColumn.getFeatureName());
    }
    Set<String> pkNames = new HashSet<>();
    for (String pkName : createCassandraEntityRequest.entity.getPrimaryKeys()) {
      // checking duplicates in pk set and validating all pk present in feature set
      if (!featureNames.contains(pkName) || pkNames.contains(pkName)) {
        return Boolean.FALSE;
      }
      pkNames.add(pkName);
    }
    return Boolean.TRUE;
  }
}
