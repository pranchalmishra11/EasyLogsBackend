package org.logAnalyser.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.logAnalyser.model.ConfWriteModel;
import org.logAnalyser.model.MatchAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Getter
@Service
public class GenerateConfigService {

    private static final List<String> AttributeSet = Arrays.asList("timestamp","log_level","pid","thread",
            "class","message");


    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


    @Value("${elasticSearch.cluster.url}")
    private String elasticSearchUrl;

    @Value("${elasticSearch.cluster.userName}")
    private String userName;

    @Value("${elasticSearch.cluster.password}")
    private String password;

    public String writeConfigString(ConfWriteModel writeModel){
        StringBuilder configBuilder = new StringBuilder();
        buildInputConfiguration(configBuilder,writeModel);
        addFilterConfiguration(configBuilder,writeModel);
        buildOutputConfiguration(configBuilder,writeModel);
        return configBuilder.toString();

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
                .append("    match => { \"message\" => \"").append(generateMatchParsing(writeModel))
                .append(" }\n").append(" }\n");
         addDateParsing(currentWrittenContext,writeModel);
         buildMutateFeatures(currentWrittenContext,writeModel);
         currentWrittenContext.append(" }\n");
        }

        private void addDateParsing(StringBuilder currentWrittenContext, ConfWriteModel writeModel) {
            if ("timestamp".equalsIgnoreCase(checkIfTimeStampPresent(writeModel.getMatchAttributes()))) {
                currentWrittenContext.append("  date {\n").
                        append("    match => [\"").append("timestamp").append("\"").append(", ").append("\"").append(TIMESTAMP_FORMAT).append("\"]\n").
                        append("    timezone => ").append("\"").append("UTC").append("\"").
                        append("    target => ").append("\"").append("timestamp").append("\"")
                        .append("  }\n");
            }
        }

    private String checkIfTimeStampPresent(List<MatchAttributes> matchAttributes) {
        if(null!=matchAttributes && !matchAttributes.isEmpty()){
            for(MatchAttributes attributes: matchAttributes){
                if("TIMESTAMP_ISO8601".equalsIgnoreCase(attributes.getType())){
                    return "timestamp";
                }
            }
        }
        return "";
    }


    private StringBuilder generateMatchParsing(ConfWriteModel confWriteModel){
            StringBuilder matchContext =  new StringBuilder();
            List<MatchAttributes> matchAttributes = confWriteModel.getMatchAttributes();
            matchAttributes.sort(Comparator.comparingInt(MatchAttributes::getPosition));

            matchAttributes.forEach(attributeObject->{
                if(attributeObject.getPosition()>1 && null!=attributeObject.getPreDelimiter() &&
                        !attributeObject.getPreDelimiter().isEmpty()){
                    String trimDelimiter = attributeObject.getPreDelimiter().trim();
                    if(trimDelimiter.isEmpty()){
                            matchContext.append("%{SPACE}+");
                        }
                    else{
                        matchContext.append("%{SPACE}*").append(trimDelimiter).append("%{SPACE}*");
                    }
                }
                if(null!=attributeObject.getPrefix()){

                    matchContext.append(transformSpecialCharacters(attributeObject.getPrefix()));
                }
                matchContext.append("%{").append(attributeObject.getType()).append(":").
                        append("TIMESTAMP_ISO8601".equalsIgnoreCase(attributeObject.getType())?"timestamp":attributeObject.getField()).append("}");
                if(null!=attributeObject.getSuffix()){
                    matchContext.append(transformSpecialCharacters(attributeObject.getSuffix()));
                }
            });
            matchContext.append("\"");

            return matchContext;
        }

        private void buildMutateFeatures(StringBuilder currentWrittenContext,ConfWriteModel writeModel){
            currentWrittenContext.append("  mutate {\n")
                    .append("    add_field => { \"microservice\" => \"%{type}\" }\n");
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
                    .append("    user => \"").append(getUserName()).append("\"\n")
                    .append("    password => \"").append(getPassword()).append("\"\n")
                    .append("  }\n")
                    .append("}\n");

        }

    private String transformSpecialCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (!Character.isLetterOrDigit(ch)) {
                result.append('\\');
            }
            result.append(ch);
        }

        return result.toString();
    }


}
