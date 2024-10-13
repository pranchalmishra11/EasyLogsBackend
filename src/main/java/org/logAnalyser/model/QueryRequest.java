package org.logAnalyser.model;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryRequest {

    private  String indexName;
    private List<Map<String, String>> filterBy;

}
