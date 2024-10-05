package org.logAnalyser.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.logAnalyser.model.ConfFileResponse;
import org.logAnalyser.model.ConfWriteModel;
import org.logAnalyser.service.GenerateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logAnalyser")
public class ConfWriteController {

    @Autowired
    GenerateConfigService generateConfigService;

    @PostMapping("/generate-config")
    public ResponseEntity<ConfFileResponse> generateLogstashConfig(@RequestBody ConfWriteModel request) {
        ConfFileResponse fileResponse = generateConfigService.writeConfigFile(request);
        return new ResponseEntity<ConfFileResponse>(fileResponse, HttpStatus.OK);
    }
}
