package com.dream11.core.dto.helper;

import com.datastax.driver.core.BoundStatement;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CassandraFetchRowWithMetadata {
  private JsonObject row;
  private Long size;
}
