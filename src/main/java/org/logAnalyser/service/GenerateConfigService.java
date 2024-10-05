package org.logAnalyser.service;

import lombok.Getter;
import org.logAnalyser.model.ConfFileResponse;
import org.logAnalyser.model.ConfWriteModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Getter
@Service
public class GenerateConfigService {

    private static final String logstashConfigDir = "C:/EasyLogAnalyser/output"; // Path to Logstash config directory

    private static final List<String> AttributeSet = Arrays.asList("timestamp","log_level","pid","thread",
            "class","message");


    @Value("${elasticSearch.cluster.url}")
    private String elasticSearchUrl;

    public ConfFileResponse writeConfigFile(ConfWriteModel writeModel){
        StringBuilder configBuilder = new StringBuilder();
        ConfFileResponse fileResponse =  new ConfFileResponse();
        buildInputConfiguration(configBuilder,writeModel);
        addFilterConfiguration(configBuilder,writeModel);
        buildOutputConfiguration(configBuilder,writeModel);
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

    private void addFilterConfiguration(StringBuilder currentWrittenContext, ConfWriteModel writeModel){
        currentWrittenContext.append("filter {\n")
                .append("  grok {\n")
                .append("    match => { \"message\" => \"%{TIMESTAMP_ISO8601:timestamp} +%{LOGLEVEL:log_level} +%{NUMBER:pid} --- \\[%{DATA:thread}\\] %{DATA:class} +: %{GREEDYDATA:message}\" }\n")
                .append("  }\n");
         buildMutateFeatures(currentWrittenContext,writeModel);
         currentWrittenContext.append(" }\n");
        }

        private void buildMutateFeatures(StringBuilder currentWrittenContext,ConfWriteModel writeModel){
            currentWrittenContext.append("  mutate {\n")
                    .append("    add_field => { \"microservice\" => \"%{type}\" }\n");
                    //.append("}\n\n");
            if(null!= writeModel.getFilterableAttributes() && !writeModel.getFilterableAttributes().isEmpty()){
                List<String> removableFields = AttributeSet.stream().
                        filter(attribute->!writeModel.getFilterableAttributes().contains(attribute)).toList();
                currentWrittenContext.append("    remove_field => [");
                for(int idx=0;idx<removableFields.size();idx++){
                    if(idx==removableFields.size()-1){
                        currentWrittenContext.append("\"").append(removableFields.get(idx)).append("\"");
                    }else{
                        currentWrittenContext.append("\"").append(removableFields.get(idx)).append("\"").append(",");
                    }
                }
                currentWrittenContext.append("]\n");
            }
            currentWrittenContext.append("  }\n");

        }

        private void buildOutputConfiguration(StringBuilder currentWrittenContext,ConfWriteModel writeModel){
            currentWrittenContext.append("output {\n")
                    .append("  elasticsearch {\n")
                    .append("    hosts => [\"").append(getElasticSearchUrl()).append("\"]\n")
                    .append("    index => \"").append(writeModel.getIndexName()).append("\"\n")
                    .append("  }\n")
                    .append("}\n");

        }


}
