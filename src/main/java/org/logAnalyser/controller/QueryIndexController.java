package org.logAnalyser.controller;

import jakarta.validation.Valid;
import org.logAnalyser.model.ErrorStatsRequest;
import org.logAnalyser.model.QueryRequest;
import org.logAnalyser.service.QueryIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/error-stats")
    public ResponseEntity<Map<String, Object>> getErrorStats(@Valid @RequestBody ErrorStatsRequest errorStatsRequest){
            Map<String, Object> response = queryIndexService.fetchErrorStats(errorStatsRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);

    }

}
