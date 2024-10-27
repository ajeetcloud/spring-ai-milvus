package com.example.springaimilvus.controller;


import com.example.springaimilvus.model.ProductRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.param.dml.InsertParam;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import java.util.List;

@RestController
public class ProductController {

    private final EmbeddingModel embeddingModel;

    private final MilvusServiceClient milvusServiceClient;

    private final Gson gson = new Gson();

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
    public ResponseEntity<String> insert(@RequestBody ProductRequest request) {

        JsonArray vectors = gson.toJsonTree(embeddingModel.embed(request.getDescription())).getAsJsonArray();
        List<JsonObject> data = new ArrayList<>();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("product_id", request.getProductId());
        jsonObject.add("description_vector", vectors);
        jsonObject.addProperty("description", request.getDescription());
        jsonObject.addProperty("category", request.getCategory());
        jsonObject.addProperty("brand", request.getBrand());
        jsonObject.addProperty("price", request.getPrice());

        data.add(jsonObject);

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName("product_embeddings")
                .withRows(data)
                .build();

        MutationResult result = milvusServiceClient.insert(insertParam).getData();
        if (result.getInsertCnt() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(String.valueOf(jsonObject));
    }
}
