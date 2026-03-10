package com.vyuh.orchestration.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyuh.orchestration.execution.ExecutionContext;
import com.vyuh.orchestration.execution.ServiceCallResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Kafka protocol handler for pub/sub messaging
 */
@Component
public class KafkaProtocolHandler implements ProtocolHandler {
    
    private static final Logger logger = Logger.getLogger(KafkaProtocolHandler.class.getName());
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private final Map<String, CompletableFuture<ServiceCallResponse>> responseMap = new ConcurrentHashMap<>();
    
    @Autowired
    public KafkaProtocolHandler(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public ServiceCallResponse executeSync(String serviceName, String path, String method,
                                          Map<String, Object> payload, Map<String, String> headers,
                                          long timeout, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        try {
            String topic = resolveTopic(serviceName, path);
            String correlationId = UUID.randomUUID().toString();
            
            logger.info("Publishing message to Kafka topic " + topic + " for service " + serviceName);
            
            CompletableFuture<ServiceCallResponse> responsePromise = new CompletableFuture<>();
            responseMap.put(correlationId, responsePromise);
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            Message<String> message = MessageBuilder.withPayload(payloadJson)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader("correlationId", correlationId)
                    .setHeader("serviceName", serviceName)
                    .setHeader("contentType", "application/json")
                    .build();
            
            kafkaTemplate.send(message);
            
            ServiceCallResponse response = responsePromise.get(timeout, TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;
            response.setExecutionTimeMs(executionTime);
            return response;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.severe("Kafka publish failed for service " + serviceName + ": " + e.getMessage());
            return ServiceCallResponse.failure(serviceName, 500, "Kafka Error: " + e.getMessage(), executionTime);
        }
    }
    
    @Override
    public Mono<ServiceCallResponse> executeAsync(String serviceName, String path, String method,
                                                  Map<String, Object> payload, Map<String, String> headers,
                                                  long timeout, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        return Mono.fromCallable(() -> {
            String topic = resolveTopic(serviceName, path);
            String correlationId = UUID.randomUUID().toString();
            
            logger.info("Publishing async message to Kafka topic " + topic + " for service " + serviceName);
            
            String payloadJson = objectMapper.writeValueAsString(payload);
            Message<String> message = MessageBuilder.withPayload(payloadJson)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader("correlationId", correlationId)
                    .setHeader("serviceName", serviceName)
                    .build();
            
            kafkaTemplate.send(message);
            
            return ServiceCallResponse.success(serviceName, 
                    "Message published with correlationId: " + correlationId, 
                    System.currentTimeMillis() - startTime);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(error -> {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.severe("Async Kafka publish failed for service " + serviceName + ": " + error.getMessage());
            return Mono.just(ServiceCallResponse.failure(serviceName, 500, 
                    "Async Kafka Error: " + error.getMessage(), executionTime));
        });
    }
    
    @Override
    public boolean supports(String protocol) {
        return "KAFKA".equalsIgnoreCase(protocol);
    }
    
    @Override
    public String getProtocolName() {
        return "KAFKA";
    }
    
    private String resolveTopic(String serviceName, String path) {
        return serviceName + "-" + path;
    }
    
    public void handleResponse(String correlationId, ServiceCallResponse response) {
        CompletableFuture<ServiceCallResponse> promise = responseMap.remove(correlationId);
        if (promise != null) {
            promise.complete(response);
        }
    }
}
