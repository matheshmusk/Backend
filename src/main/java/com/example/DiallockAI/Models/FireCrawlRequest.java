package com.example.DiallockAI.Models;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class FireCrawlRequest {
    private String url;
    private int limit;
    private Map<String, Object> scrapeOptions;

    public FireCrawlRequest(String url, int limit) {
        this.url = url;
        this.limit = limit;
        this.scrapeOptions = Map.of("formats", List.of("markdown"));
    }
}