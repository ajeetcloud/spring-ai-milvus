package com.example.springaimilvus.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorConfig {

    @Value("${zilliz.username}")
    private String username;

    @Value("${zilliz.password}")
    private String password;

    @Value("${zilliz.endpoint}")
    private String endpoint;

    @Bean
    public MilvusServiceClient milvusServiceClient() {

        return new MilvusServiceClient(ConnectParam.newBuilder()
                .withAuthorization(username, password)
                .withUri(endpoint)
                .build());
    }
}
