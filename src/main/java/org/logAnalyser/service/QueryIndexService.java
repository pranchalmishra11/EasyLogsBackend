package org.logAnalyser.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.json.JsonData;
import org.logAnalyser.model.ErrorStatsRequest;
import org.logAnalyser.model.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Service
public class QueryIndexService {

    private final ElasticsearchClient elasticsearchClientConfig;

    @Autowired
    public QueryIndexService(ElasticsearchClient elasticsearchClientConfig) {
        this.elasticsearchClientConfig = elasticsearchClientConfig;
    }


    public List<String> fetchResolvedData(QueryRequest queryRequest){
        String indexName = queryRequest.getIndexName();
        List<Map<String,String>> filterByList = queryRequest.getFilterBy();
        List<String> resultList = new ArrayList<>();
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            for (Map<String, String> filter : filterByList) {
                String fieldName = filter.get("fieldName");
                String value = filter.get("value");
                if (fieldName != null && value != null) {
                    boolQuery.must(m -> m
                            .match(t -> t
                                    .field(fieldName)
                                    .query(value)));
                }
            }
            SearchResponse<Map> response = elasticsearchClientConfig.search(s -> s
                            .index(indexName)
                            .query(q -> q.bool(boolQuery.build()))
                            .size(1000),  // Adjust size if you expect more results
                    Map.class);

            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source();
                if (source != null) {
                    if (source.containsKey("event") && ((Map<String, Object>) source.get("event")).containsKey("original")) {
                        Map<String, Object> event = (Map<String, Object>) source.get("event");
                        String originalLog = (String) event.get("original");
                        resultList.add(originalLog);
                    }
                }
            }
            return resultList;

        } catch (IOException e) {
            return new ArrayList<String>();
        }

    }

    public Map<String,Object> fetchErrorStats(ErrorStatsRequest errorStatsRequest){
        Map<String,Object> errorStats =  new HashMap<>();
        String indexName = errorStatsRequest.getIndexName();
        List<String> microservices = errorStatsRequest.getMicroservices();
        try {
            if (!indexExists(indexName)) {
                return Collections.singletonMap("error", "Index '" + indexName + "' not found.");
            }
            for(String microservice:microservices){
                buildStatsMap(errorStats,indexName,microservice);
            }
        }catch(IOException ioe){
            return Collections.singletonMap("error",ioe.getMessage());
        }
        return errorStats;
    }


    private boolean indexExists(String indexName) throws IOException {
        GetIndexRequest request = GetIndexRequest.of(i -> i.index(indexName));
        try {
            elasticsearchClientConfig.indices().get(request);
            return true;
        } catch (ElasticsearchException e) {
            if (e.status() == 404) {
                return false;
            }
            throw e;
        }
    }


    private long countTotalLogs(String microserviceName, String indexName) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s.index(indexName).query(q -> q.term(t -> t
                                .field("microserviceName")
                                .value(microserviceName))));
        SearchResponse<JsonData> response = elasticsearchClientConfig.search(request, JsonData.class);
        return response.hits().total().value();
    }
    private long countErrorLogs(String microserviceName, String indexName) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s.index(indexName).query(q -> q.bool(b -> b
                                .must(m -> m.term(t -> t.field("microserviceName").value(microserviceName)
                                        )).must(m -> m.term(t -> t.field("log_level").value("ERROR"))))));
        SearchResponse<JsonData> response = elasticsearchClientConfig.search(request, JsonData.class);
        return response.hits().total().value();
    }

    private void buildStatsMap(Map<String,Object> errorStats, String indexName, String microserviceName) throws IOException {
        Map<String, Object> stats = new HashMap<>();
        long totalLogs = countTotalLogs(microserviceName, indexName);
        if (totalLogs == 0) {
            errorStats.put(microserviceName, "No logs found for microservice '" + microserviceName + "'");
        }
        long errorLogs = countErrorLogs(microserviceName, indexName);
        double errorRate = (double) errorLogs / totalLogs * 100;
        stats.put("totalLogs", totalLogs);
        stats.put("errorLogs", errorLogs);
        stats.put("errorRate", errorRate);
        errorStats.put(microserviceName,stats);
    }
}
