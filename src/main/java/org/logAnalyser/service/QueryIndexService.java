package org.logAnalyser.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.logAnalyser.model.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        try {
            // Build the boolean query to handle multiple filters
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // Iterate over the filterBy array and build a must clause for each filter
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

            // Execute the search request
            SearchResponse<Map> response = elasticsearchClientConfig.search(s -> s
                            .index(indexName)
                            .query(q -> q.bool(boolQuery.build()))
                            .size(1000),  // Adjust size if you expect more results
                    Map.class);

            // Collect log messages from the search hits
            List<String> logs = response.hits().hits().stream()
                    .map(hit -> hit.source().get("logMessage").toString()) // Assuming logs are stored in "logMessage" field
                    .collect(Collectors.toList());

            return logs;

        } catch (IOException e) {
            return new ArrayList<String>();
        }

    }

}
