package org.logAnalyser.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MicroServiceConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String logPath;
}
