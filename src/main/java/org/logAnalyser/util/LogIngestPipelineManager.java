package org.logAnalyser.util;

import com.google.gson.JsonObject;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.net.URISyntaxException;

public class LogIngestPipelineManager {

    private final RestClient restClient;

    public LogIngestPipelineManager() throws URISyntaxException {
        this.restClient = ElasticsearchClientConfig.buildRestClient();
    }
    public Response createOrUpdatePipeline(String pipelineId, JsonObject pipelineConfig) throws IOException {
        String endpoint = "/_logstash/pipeline/" + pipelineId;
        Request request = new Request("PUT", endpoint);
        request.setJsonEntity(pipelineConfig.toString());
        return restClient.performRequest(request);
    }

    public Response deletePipeline(String pipelineId) throws IOException {
        String deleteUrl = "/_logstash/pipeline/" + pipelineId;
        return restClient.performRequest(new Request("DELETE",deleteUrl));
    }

    public void close() throws IOException {
        restClient.close();
    }
}


