package org.logAnalyser.controller;


import jakarta.validation.Valid;
import org.logAnalyser.model.ConfWriteModel;
import org.logAnalyser.service.PushLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/logPush")
public class PushLogsController {

    @Autowired
    PushLogService pushLogService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startLogstashPipeline(@Valid @RequestBody ConfWriteModel writeModel) throws IOException, URISyntaxException {
        Map<String, Object> responseMap = new HashMap<>();
        int returnCode = pushLogService.runLogStashProcess(writeModel);
        if (returnCode == 0) {
            responseMap.put("status","Logstash pipeline started successfully");
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        }
        else{
            responseMap.put("status","Failed to start Logstash pipeline");
            return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/stop/{pipelineId}")
    public ResponseEntity<Map<String, Object>> stopLogstashPipeline(@PathVariable String pipelineId) throws URISyntaxException,IOException{
        Map<String, Object> responseMap = new HashMap<>();
        int returnCode = pushLogService.haltLogIngestion(pipelineId);
        if (returnCode == 0) {
            responseMap.put("status","Logstash pipeline stopped successfully");
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        }
        else if(returnCode==2){
            responseMap.put("status","Logstash pipeline not found");
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        }
        else{
            responseMap.put("status","Failed to stop Logstash pipeline");
            return new ResponseEntity<>(responseMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/status/{pipelineId}")
    public ResponseEntity<Map<String, Object>> getPipelineStatus(@PathVariable String pipelineId){
        Map<String, Object> responseMap = new HashMap<>();
        int response = pushLogService.getIngestionStatus(pipelineId);
        if (response==0)
        {
            responseMap.put("status", "Pipeline is actively processing logs.");
        }
        else if(response==1)
        {
            responseMap.put("status", "Pipeline is not processing any logs currently.");
        }
        else
        {
            responseMap.put("status","pipeline is terminated");
        }
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }
}

