package com.dream11.core.dto.metastore;

import static com.dream11.core.constant.Constants.*;

import com.dream11.core.dto.entity.CassandraEntity;
import com.dream11.core.dto.metastore.interfaces.EntityMetadata;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CassandraEntityMetadata implements EntityMetadata {
  private String name;
  private CassandraEntity entity;
  @Builder.Default private State state = State.LIVE;
  private String owner;
  @Builder.Default private List<String> tags = EMPTY_TAG_ARRAY;
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
