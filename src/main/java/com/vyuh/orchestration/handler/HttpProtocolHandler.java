package com.vyuh.orchestration.handler;

import com.vyuh.orchestration.execution.ExecutionContext;
import com.vyuh.orchestration.execution.ServiceCallResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HTTP protocol handler for REST API calls
 */
@Component
public class HttpProtocolHandler implements ProtocolHandler {
    
    private static final Logger logger = Logger.getLogger(HttpProtocolHandler.class.getName());
    
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    
    @Autowired
    public HttpProtocolHandler(RestTemplate restTemplate, WebClient webClient) {
        this.restTemplate = restTemplate;
        this.webClient = webClient;
    }
    
    @Override
    public ServiceCallResponse executeSync(String serviceName, String path, String method,
                                          Map<String, Object> payload, Map<String, String> headers,
                                          long timeout, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        try {
            String url = buildUrl(serviceName, path);
            
            logger.info("Executing HTTP " + method + " request to " + url + " for service " + serviceName);
            
            Map<String, Object> response = executeHttpRequest(url, method, payload, headers, timeout);
            
            long executionTime = System.currentTimeMillis() - startTime;
            return ServiceCallResponse.success(serviceName, response, executionTime);
            
        } catch (RestClientException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.severe("HTTP request failed for service " + serviceName + ": " + e.getMessage());
            return ServiceCallResponse.failure(serviceName, 500, "HTTP Error: " + e.getMessage(), executionTime);
        }
    }
    
    @Override
    public Mono<ServiceCallResponse> executeAsync(String serviceName, String path, String method,
                                                  Map<String, Object> payload, Map<String, String> headers,
                                                  long timeout, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        String url = buildUrl(serviceName, path);
        
        logger.info("Executing async HTTP " + method + " request to " + url + " for service " + serviceName);
        
        return webClient.method(org.springframework.http.HttpMethod.valueOf(method.toUpperCase()))
                .uri(url)
                .bodyValue(payload)
                .headers(httpHeaders -> {
                    if (headers != null) {
                        headers.forEach(httpHeaders::set);
                    }
                })
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(Duration.ofMillis(timeout))
                .map(response -> ServiceCallResponse.success(serviceName, response, 
                        System.currentTimeMillis() - startTime))
                .onErrorResume(error -> {
                    long executionTime = System.currentTimeMillis() - startTime;
                    logger.severe("Async HTTP request failed for service " + serviceName + ": " + error.getMessage());
                    return Mono.just(ServiceCallResponse.failure(serviceName, 500, 
                            "Async HTTP Error: " + error.getMessage(), executionTime));
                });
    }
    
    @Override
    public boolean supports(String protocol) {
        return "HTTP".equalsIgnoreCase(protocol) || "REST".equalsIgnoreCase(protocol);
    }
    
    @Override
    public String getProtocolName() {
        return "HTTP";
    }
    
    private String buildUrl(String serviceName, String path) {
        return String.format("http://%s%s", serviceName, path);
    }
    
    private Map<String, Object> executeHttpRequest(String url, String method, 
                                                   Map<String, Object> payload, 
                                                   Map<String, String> headers, 
                                                   long timeout) {
        return new HashMap<>();
    }
}
