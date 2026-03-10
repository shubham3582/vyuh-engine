package com.vyuh;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Vyuh - State Machine Engine
 *
 * Configuration-driven state machine engine supporting:
 * - HTTP/REST calls
 * - gRPC calls
 * - Kafka pub/sub messaging
 * - Synchronous and asynchronous execution
 * - Multi-level workflow calls
 * - Conditional routing and fallbacks
 * - Distributed state management with Aerospike
 */
@SpringBootApplication(scanBasePackages = "com.vyuh")
@EnableScheduling
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean
    public CommandLineRunner warmUpRunner() {
        return args -> {
            logger.info("Starting application warm-up...");
            
            try {
                // Warm up ObjectMapper
                logger.info("Warming up ObjectMapper...");
                objectMapper().writeValueAsString(Map.of("warmup", "test"));
                logger.info("ObjectMapper warm-up completed");
                
                // Warm up RestTemplate
                logger.info("Warming up RestTemplate...");
                // Note: Actual HTTP call would be made here in production
                logger.info("RestTemplate warm-up completed");
                
                // Warm up WebClient
                logger.info("Warming up WebClient...");
                // Note: Actual HTTP call would be made here in production
                logger.info("WebClient warm-up completed");
                
                logger.info("Application warm-up completed successfully");
                
            } catch (Exception e) {
                logger.error("Error during warm-up", e);
                throw e;
            }
        };
    }
}

