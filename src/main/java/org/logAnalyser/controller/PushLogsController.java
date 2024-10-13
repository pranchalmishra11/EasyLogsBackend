package org.logAnalyser.controller;


import org.logAnalyser.service.PushLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logPush")
public class PushLogsController {

    @Autowired
    PushLogService pushLogService;

    @PostMapping("/start")
    public ResponseEntity<String> startLogstashPipeline() {
        int returnCode = pushLogService.runLogStashProcess();
        if (returnCode == 0) {
            return ResponseEntity.ok("Logstash pipeline started successfully");
        }
        else if(returnCode==1){
            return ResponseEntity.ok("Logstash pipeline is active" );
        }
        else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to start Logstash pipeline");

        }
    }
}
