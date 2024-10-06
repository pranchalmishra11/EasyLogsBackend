package org.logAnalyser.model;


import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class MatchAttributes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type;
    private String field;
    private Integer position;
    private String preDelimiter;
    private String prefix;
    private String suffix;

}
