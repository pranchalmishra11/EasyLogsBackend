package org.logAnalyser.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConfFileResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String message;
    private String generatedFilePath;




}
