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

@RestController
@RequestMapping("/logPush")
public class PushLogsController {

    @Autowired
    PushLogService pushLogService;

    @PostMapping("/start")
    public ResponseEntity<String> startLogstashPipeline(@Valid @RequestBody ConfWriteModel writeModel) throws IOException, URISyntaxException {
        int returnCode = pushLogService.runLogStashProcess(writeModel);
        if (returnCode == 0) {
            return ResponseEntity.ok("Logstash pipeline started successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start Logstash pipeline");

        }
    }

    @PostMapping("/stop/{pipelineId}")
    public ResponseEntity<String> stopLogstashPipeline(@PathVariable String pipelineId) throws URISyntaxException,IOException{
        int returnCode = pushLogService.haltLogIngestion(pipelineId);
        if (returnCode == 0) {
            return ResponseEntity.ok("Logstash pipeline stopped successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to stop Logstash pipeline");

        }
    }
}
