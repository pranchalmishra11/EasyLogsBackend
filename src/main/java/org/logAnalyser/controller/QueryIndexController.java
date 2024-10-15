package org.logAnalyser.controller;


import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.logAnalyser.model.QueryRequest;
import org.logAnalyser.service.QueryIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/queryLogs")
public class QueryIndexController {
    
    @Autowired
    QueryIndexService queryIndexService;

    @PostMapping("/search")
    public ResponseEntity<List<String>> search(@RequestBody QueryRequest queryRequest){
        List<String> response = queryIndexService.fetchResolvedData(queryRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
