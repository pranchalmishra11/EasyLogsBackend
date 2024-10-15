package org.logAnalyser.service;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.logAnalyser.model.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

}
