package com.example.DiallockAI.Services;


import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.DiallockAI.Models.FireCrawlRequest;
import com.example.DiallockAI.Models.FireCrawlResponse;
import com.example.DiallockAI.Models.JobResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;


@Service
public class FireCrawl {
	private final WebClient webClient;
    private final ObjectMapper objectMapper; 

    
    public FireCrawl(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.firecrawl.dev").build();
        this.objectMapper = objectMapper;
    }
    public Mono<String> llmUrl(String url) {
        Map<String, Object> body = new HashMap<>();
        body.put("url", url);
        body.put("maxUrls", 2);
        body.put("showFullText", true);

        return webClient.post()
                .uri("/v1/llmstxt")
                .header("Authorization", "Bearer fc-ff419d39b47044ad98ff1f83fb33eae0")// Don't repeat the domain, just path.
                .header("Content-Type", "application/json")
                .bodyValue(body)      // Sends body correctly
                .retrieve()           // Starts retrieving the response
                .bodyToMono(FireCrawlResponse.class)
                .flatMap(response -> {
                    if (response.getData() == null) {
                        return Mono.error(new RuntimeException("No data returned"));
                    }
                    return Mono.just(response.getData().get(0).getMarkdown());

                });
    }
    


    public Mono<String> crawlUrl(String url) {
        Map<String, Object> body = new HashMap<>();
        body.put("url", url);
        body.put("maxDepth", 2); // You can adjust depth if you want

        return webClient.post()
                .uri("/v1/crawl")
                .header("Authorization", "Bearer fc-ff419d39b47044ad98ff1f83fb33eae0")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String id = (String) response.get("id");
                    if (id == null) {
                        return Mono.error(new RuntimeException("No ID returned from crawl"));
                    }
                    return pollCrawlResult(id,10); // Now poll using the returned ID
                });
    }

    private Mono<String> pollCrawlResult(String id, int attemptsLeft) {
        return webClient.get()
                .uri("/v1/crawl/" + id)
                .header("Authorization", "Bearer fc-ff419d39b47044ad98ff1f83fb33eae0")
                .retrieve()
                .bodyToMono(FireCrawlResponse.class)
                .flatMap(response -> {
                    List<FireCrawlResponse.CrawlData> dataList = response.getData();
                    if (dataList != null && !dataList.isEmpty()) {
                        return Mono.justOrEmpty(dataList.get(0).getMarkdown());
                    } else if (attemptsLeft > 0) {
                        return Mono.delay(Duration.ofSeconds(2))
                                .flatMap(ignored -> pollCrawlResult(id, attemptsLeft - 1));
                    } else {
                        return Mono.error(new RuntimeException("No data returned after maximum retries"));
                    }
                });
    }




   

    public Mono<String> scrapeUrl(String url) {
        return webClient.post()
            .uri("/v0/scrape")
            .header("Authorization", "Bearer fc-ff419d39b47044ad98ff1f83fb33eae0")
            .bodyValue(Map.of(
                "url", url,
                "options", Map.of("formats", List.of("markdown"))
            ))
            .retrieve()
            .bodyToMono(FireCrawlResponse.class)
            .flatMap(response -> {
                if (response.getData() == null) {
                    return Mono.error(new RuntimeException("No data returned"));
                }
                return Mono.just(response.getData().get(0).getMarkdown());

            });
    }
    
   }