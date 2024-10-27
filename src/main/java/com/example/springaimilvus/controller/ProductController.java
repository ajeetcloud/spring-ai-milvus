package com.example.springaimilvus.controller;


import com.example.springaimilvus.model.ProductRequest;
import com.example.springaimilvus.model.SearchRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.highlevel.dml.SearchSimpleParam;
import io.milvus.param.highlevel.dml.response.SearchResponse;
import io.milvus.response.QueryResultsWrapper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @PostMapping("/search")
    public List<QueryResultsWrapper.RowRecord> getSrcResults(@RequestBody SearchRequest searchRequest) {

        float[] vectors = embeddingModel.embed(searchRequest.getSearchQuery());
        List<Float> floatList = IntStream.range(0, vectors.length)
                .mapToObj(i -> vectors[i])
                .toList();

        SearchSimpleParam searchParam = SearchSimpleParam.newBuilder()
                .withCollectionName("product_embeddings")
                .withVectors(floatList)
                .withLimit(searchRequest.getLimit())
                .withOutputFields(List.of("description"))
                .build();

        SearchResponse src= milvusServiceClient.search(searchParam).getData();
        List<QueryResultsWrapper.RowRecord> rowRecords = src.getRowRecords(0);
        return rowRecords;
    }

    @PostMapping("/searchAdvanced")
    public String getSrcResultsAdvanced(@RequestBody SearchRequest searchRequest) {

        float[] vectors = embeddingModel.embed(searchRequest.getSearchQuery());

        List<Float> floatList = IntStream.range(0, vectors.length)
                .mapToObj(i -> vectors[i])
                .toList();

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName("product_embeddings")
                .withVectorFieldName("description_vector")
                .withFloatVectors(Collections.singletonList(floatList))
                .withTopK(Math.toIntExact(searchRequest.getLimit()))
                .withOutFields(List.of("description"))
                .build();

        SearchResults src = milvusServiceClient.search(searchParam).getData();
        src.getResults().getAllFields();
        return src.getResults().toString();
    }
}
