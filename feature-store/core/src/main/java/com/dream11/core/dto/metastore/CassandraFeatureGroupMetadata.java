package com.dream11.core.dto.metastore;

import static com.dream11.core.constant.Constants.*;
import static com.dream11.core.dto.metastore.TenantConfig.DEFAULT_TENANT_CONFIG;

import com.dream11.core.dto.featuregroup.CassandraFeatureGroup;
import com.dream11.core.dto.metastore.interfaces.FeatureGroupMetadata;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraFeatureGroupMetadata implements FeatureGroupMetadata {
  private String name;

  @JsonAlias("version_enabled")
  private Boolean versionEnabled;

  private String version;

  @JsonAlias("feature_group_type")
  private CassandraFeatureGroup.FeatureGroupType featureGroupType;

  @JsonAlias("feature_group")
  private CassandraFeatureGroup featureGroup;

  private String owner;

  @Builder.Default private State state = State.LIVE;
  @Builder.Default private List<String> tags = EMPTY_TAG_ARRAY;

  @JsonAlias("tenant_config")
  @Builder.Default private TenantConfig tenant = DEFAULT_TENANT_CONFIG;
  private String description;

  @JsonAlias("created_at")
  private Date createdAt;

  @JsonAlias("updated_at")
  private Date updatedAt;

  public enum State {
    LIVE,
    ARCHIVED
  }
}
