package org.logAnalyser.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConfWriteModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<String> filterableAttributes;

    private List<MicroServiceConfig> microservices;




}
