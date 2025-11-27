package com.dream11.core.dto.metastore;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VersionMetadata {
    String name;
    @JsonAlias("latest_version")
    private String latestVersion;
    @JsonAlias("updated_at")
    Date updatedAt;
}
