package com.example.DiallockAI.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient firecrawlWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://api.firecrawl.dev")
            .defaultHeader("Authorization", "Bearer fc-ff419d39b47044ad98ff1f83fb33eae0")
            .build();
    }
}
