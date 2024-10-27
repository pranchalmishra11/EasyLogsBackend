package org.logAnalyser.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.logAnalyser.model.ConfWriteModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@Service
public class PushLogService {

    private static final Logger logger = LoggerFactory.getLogger(PushLogService.class);

    @Value("${queue.type}")
    private String queueType;

    @Value("${logStash.url}")
    private String logstashUrl;

    @Autowired
    GenerateConfigService generateConfigService;

    @Autowired
    LogIngestPipelineManager pipelineManager;


    public int runLogStashProcess(ConfWriteModel writeModel) throws IOException, URISyntaxException {
        JsonObject pipelineBody = createRequestBody(writeModel);
        Response response = pipelineManager.createOrUpdatePipeline(writeModel.getPipelineId(), pipelineBody);
        int statusCode = response.getStatusLine().getStatusCode();
        if(statusCode==201 || statusCode==200){
            return 0;
        }
        return 1;
    }

    private JsonObject createRequestBody(ConfWriteModel confWriteModel){
        JsonObject pipelineBody = new JsonObject();
        pipelineBody.addProperty("description", confWriteModel.getDescription());
        pipelineBody.addProperty("last_modified", confWriteModel.getLast_modified());
        JsonObject metadata = new JsonObject();
        metadata.addProperty("type", "logstash_pipeline");
        metadata.addProperty("version", "1");
        pipelineBody.add("pipeline_metadata", metadata);
        pipelineBody.addProperty("username", confWriteModel.getPipelineOwner());
        pipelineBody.addProperty("pipeline", generateConfigService.writeConfigString(confWriteModel));
        JsonObject settings = new JsonObject();
        settings.addProperty("pipeline.workers", confWriteModel.getWorkers());
        settings.addProperty("pipeline.batch.size", confWriteModel.getBatchSize());
        settings.addProperty("pipeline.batch.delay", confWriteModel.getBatchDelay());
        settings.addProperty("queue.type", queueType);
        pipelineBody.add("pipeline_settings", settings);
        return pipelineBody;
    }

    public int haltLogIngestion(String pipelineId) throws IOException {
        try {
            Response response = pipelineManager.deletePipeline(pipelineId);
            if (response.getStatusLine().getStatusCode() == 200) {
                return 0;
            }
        }catch (ResponseException re){
            return 2;
        }
        return 1;

    }

    public int getIngestionStatus(String pipelineId){
        JsonObject eventsObject =  callLogStash(pipelineId);
        if(!eventsObject.entrySet().isEmpty()) {
            long eventsIn = eventsObject.get("in").getAsLong();
            long eventsOut = eventsObject.get("out").getAsLong();
            long eventsFiltered = eventsObject.get("filtered").getAsLong();
            if (eventsIn > 0 && (eventsOut > 0 || eventsFiltered > 0)) {
                return 0;
            } else {
                return 1;
            }
        }
        return 2;
    }

    private JsonObject callLogStash(String pipelineId){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try{
        ResponseEntity<String> response = restTemplate.exchange(logstashUrl+pipelineId+"?pretty", HttpMethod.GET, entity, String.class);
        int code = response.getStatusCode().value();
        if(code==200){
                JsonObject resultObject = JsonParser.parseString(Objects.requireNonNull(response.getBody())).
                        getAsJsonObject();
                JsonObject pipelinesObject = resultObject.getAsJsonObject("pipelines");
                JsonObject pipelineObject = pipelinesObject.getAsJsonObject(pipelineId);
                if(pipelineObject.get("events").isJsonObject()) {
                    return pipelineObject.getAsJsonObject("events");
                }else{
                    return new JsonObject();
                }
            }
        }catch(RestClientException restClientException){
            return new JsonObject();
        }
       return new JsonObject();
    }

}
