package org.logAnalyser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConfWriteModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> filterableAttributes;

    @NotNull
    @NotEmpty
    private String pipelineId;

    @NotNull
    @NotEmpty
    private List<MicroServiceConfig> microservices;

    @NotNull
    @NotEmpty
    private String indexName;

    @NotNull
    @NotEmpty
    private String pipelineOwner;

    private String description;

    @NotNull
    @NotEmpty
    private List<MatchAttributes> matchAttributes;

    @NotNull
    private ZonedDateTime last_modified;

    @NotNull
    private Integer workers;

    @NotNull
    private Integer batchSize;

    @NotNull
    private Integer batchDelay;




}
