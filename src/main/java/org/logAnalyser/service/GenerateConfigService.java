package org.logAnalyser.service;

import org.logAnalyser.model.ConfFileResponse;
import org.logAnalyser.model.ConfWriteModel;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class GenerateConfigService {

    private static final String logstashConfigDir = "C:/EasyLogAnalyser/output"; // Path to Logstash config directory

    public ConfFileResponse writeConfigFile(ConfWriteModel writeModel){
        StringBuilder configBuilder = new StringBuilder();
        ConfFileResponse fileResponse =  new ConfFileResponse();
        buildInputConfiguration(configBuilder,writeModel);
        addFilterConfiguration(configBuilder);
        buildOutputConfiguration(configBuilder);
        // Write the configuration file to the specified directory
        String configFilePath = Paths.get(logstashConfigDir,"logstashSample.conf").toString();
        try (FileWriter fileWriter = new FileWriter(configFilePath)) {
            fileWriter.write(configBuilder.toString());
        } catch (IOException e) {
            fileResponse.setGeneratedFilePath(null);
            fileResponse.setMessage("Failure");
        }
        fileResponse.setGeneratedFilePath(configFilePath);
        fileResponse.setMessage("Success");
        return fileResponse;

    }

    private void buildInputConfiguration(StringBuilder currentWrittenContext, ConfWriteModel writeModel){
        currentWrittenContext.append("input {\n");
        writeModel.getMicroservices().forEach(microservice -> {
            currentWrittenContext.append("  file {\n")
                    .append("    path => \"").append(microservice.getLogPath()).append("\"\n")
                    .append("    start_position => \"beginning\"\n")
                    .append("    sincedb_path => \"NUL\"\n")
                    .append("     type => \"").append(microservice.getName()).append("\"\n")
                    .append("  }\n");
        });
        currentWrittenContext.append("}\n\n");

    }

    private void addFilterConfiguration(StringBuilder currentWrittenContext){
        currentWrittenContext.append("filter {\n")
                .append("  grok {\n")
                .append("    match => { \"message\" => \"%{TIMESTAMP_ISO8601:timestamp} +%{LOGLEVEL:log_level} +%{NUMBER:pid} --- \\[%{DATA:thread}\\] %{DATA:class} +: %{GREEDYDATA:message}\" }\n")
                .append("  }\n")
                .append("  mutate {\n")
                .append("      add_field => { \"microservice\" => \"%{type}\" }\n")
                .append("}\n\n");
        }

        private void buildOutputConfiguration(StringBuilder currentWrittenContext){
            currentWrittenContext.append("output {\n")
                    .append("  elasticsearch {\n")
                    .append("    hosts => [\"http://localhost:9200\"]\n")
                    .append("    index => \"microservice-logs-%{+YYYY.MM.dd}\"\n")
                    .append("  }\n")
                    .append("}\n");

        }


}
