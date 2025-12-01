package com.dream11.populator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFileState {
  @JsonIgnore private static final Integer DEFAULT_RETRIES = 3;

  private String tableName;
  private Integer fileId;
  @Builder.Default private State state = State.PENDING;
  @Builder.Default private Status status = Status.RUNNING;
  @Builder.Default private Integer retry = DEFAULT_RETRIES;
  private Long updatedAt;

  public enum State {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
  };

  public enum Status {
    RUNNING,
    PAUSED
  };
}
