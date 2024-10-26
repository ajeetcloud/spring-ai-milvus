package com.example.springaimilvus.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProductController {

    private final EmbeddingModel embeddingModel;

    public ProductController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }


    @GetMapping("/hello")
    public String getResults() {

        System.out.println(embeddingModel);
        float[] embeddings = embeddingModel.embed("hello");
        System.out.println(embeddings.length);
        return String.valueOf(embeddings.length);
    }

}
