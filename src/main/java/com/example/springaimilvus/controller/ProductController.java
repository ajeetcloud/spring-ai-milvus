package com.example.springaimilvus.controller;

import io.milvus.client.MilvusServiceClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProductController {

    private final EmbeddingModel embeddingModel;

    private final MilvusServiceClient milvusServiceClient;

    public ProductController(EmbeddingModel embeddingModel, MilvusServiceClient milvusServiceClient) {
        this.embeddingModel = embeddingModel;
        this.milvusServiceClient = milvusServiceClient;
    }


    @GetMapping("/hello")
    public String getResults() {

        System.out.println(embeddingModel);
        float[] embeddings = embeddingModel.embed("hello");
        System.out.println(embeddings.length);
        return String.valueOf(embeddings.length);
    }

    @PostMapping("/insert")
    public String insert() {

        return "";
    }

}
