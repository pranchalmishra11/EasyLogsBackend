package org.logAnalyser.service;


import com.google.gson.JsonObject;
import org.logAnalyser.model.ConfWriteModel;
import org.elasticsearch.client.Response;
import org.logAnalyser.util.LogIngestPipelineManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

@Service
public class PushLogService {

    private static final Logger logger = LoggerFactory.getLogger(PushLogService.class);

    @Value("${queue.type}")
    private String queueType;

    @Autowired
    GenerateConfigService generateConfigService;


    public int runLogStashProcess(ConfWriteModel writeModel) throws IOException, URISyntaxException {
        JsonObject pipelineBody = createRequestBody(writeModel);
        LogIngestPipelineManager pipelineManager =  new LogIngestPipelineManager();
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
        pipelineBody.addProperty("last_modified", confWriteModel.getLast_modified().toString());
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

    public int haltLogIngestion(String pipelineId) throws URISyntaxException, IOException {
        LogIngestPipelineManager pipelineManager =  new LogIngestPipelineManager();
        Response response = pipelineManager.deletePipeline(pipelineId);
        if(response.getStatusLine().getStatusCode()==200){
            return 0;
        }

        return 1;

    }

}
