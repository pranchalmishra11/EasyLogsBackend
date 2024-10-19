package org.logAnalyser.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
@NoArgsConstructor
public class ErrorStatsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @NotEmpty
    private  String indexName;

    @NotNull
    @NotEmpty
    private List<String> microservices;
}
