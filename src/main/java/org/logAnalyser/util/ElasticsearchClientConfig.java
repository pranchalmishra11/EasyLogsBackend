package org.logAnalyser.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.transport.TransportUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration

public class ElasticsearchClientConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() throws IOException {
        // Configure the REST client to connect to the Elasticsearch cluster
        RestClient restClient = RestClient.builder(
                        new HttpHost("localhost", 9200, "http"))  // Replace with your Elasticsearch node
                .build();

        // Create the Elasticsearch Java API client using the REST transport
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
