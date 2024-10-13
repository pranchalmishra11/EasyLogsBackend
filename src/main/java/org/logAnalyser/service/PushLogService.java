package org.logAnalyser.service;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class PushLogService {

    @Value("${logstash.dir.path}")
    private String logstashDirPath;

    @Value("${logstash.conf.file.path}")
    private String configPath;

    private static final Logger logger = LoggerFactory.getLogger(PushLogService.class);
    private Process logstashProcess;

    public int runLogStashProcess(){
        if (logstashProcess != null && logstashProcess.isAlive()) {
            logger.info("Logstash is already running.");
            return 1;
        }
        String logstashCommand = logstashDirPath + " -f " + configPath;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(logstashCommand.split(" "));
            processBuilder.redirectErrorStream(true);
            logstashProcess = processBuilder.start();
            // Capture Logstash output in a separate thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(logstashProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info(line);
                    }
                } catch (IOException e) {
                    logger.error("Error reading Logstash process output", e);
                }
            });
            logger.info("Logstash process started with config file: {}", configPath);
            return 0;


        }catch (IOException e) {
            logger.error("Error starting Logstash", e);
            return 2;
        }
    }

}
