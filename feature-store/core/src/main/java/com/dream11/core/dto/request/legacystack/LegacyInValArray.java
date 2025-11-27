package com.dream11.core.dto.request.legacystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LegacyInValArray {
    @NotNull
    @JsonProperty("ColumnName")
    private String columnName;

    @NotNull
    @JsonProperty("Values")
    private List<Object> Values;

    // converting to lowercase to support legacy stack
    public void patchRequest(){
        columnName = columnName.toLowerCase();
    }
}
