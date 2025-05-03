package com.example.DiallockAI.Models;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FireCrawlResponse {
    private String status;
    private int total;
    private int completed;
    private int creditsUsed;
    private String expiresAt;
    private String next;
    private List<CrawlData> data;

    @Data
    public static class CrawlData {
        private String markdown;
        private String html;
        private Map<String, Object> metadata;
    }
}
