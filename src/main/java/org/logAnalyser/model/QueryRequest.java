package org.logAnalyser.model;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class QueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private  String indexName;
    private List<Map<String, String>> filterBy;

}
