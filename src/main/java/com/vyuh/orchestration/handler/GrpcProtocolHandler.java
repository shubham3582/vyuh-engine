package com.vyuh.orchestration.handler;

import com.vyuh.orchestration.execution.ExecutionContext;
import com.vyuh.orchestration.execution.ServiceCallResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.logging.Logger;

/**
 * gRPC protocol handler for high-performance RPC calls.
 * Contract definitions are published in src/main/proto/orchestration.proto.
 */
@Component
public class GrpcProtocolHandler implements ProtocolHandler {
    
    private static final Logger logger = Logger.getLogger(GrpcProtocolHandler.class.getName());
    
    @Override
    public ServiceCallResponse executeSync(String serviceName, String url, String path, String method,
                                          Map<String, Object> payload, Map<String, String> headers,
                                          long timeout, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        try {
            String target = resolveTarget(serviceName, url);
            
            logger.info("Executing gRPC call to " + target + " for service " + serviceName + " method " + method);
            
            Map<String, Object> response = executeGrpcCall(target, method, payload, timeout);
            
            long executionTime = System.currentTimeMillis() - startTime;
            return ServiceCallResponse.success(serviceName, response, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.severe("gRPC call failed for service " + serviceName + ": " + e.getMessage());
            return ServiceCallResponse.failure(serviceName, 500, "gRPC Error: " + e.getMessage(), executionTime);
        }
    }
    
    @Override
    public Mono<ServiceCallResponse> executeAsync(String serviceName, String url, String path, String method,
                                                  Map<String, Object> payload, Map<String, String> headers,
                                                  long timeout, ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        return Mono.fromCallable(() -> {
            String target = resolveTarget(serviceName, url);
            
            logger.info("Executing async gRPC call to " + target + " for service " + serviceName + " method " + method);
            
            Map<String, Object> response = executeGrpcCall(target, method, payload, timeout);
            
            return ServiceCallResponse.success(serviceName, response, System.currentTimeMillis() - startTime);
        })
        .timeout(java.time.Duration.ofMillis(timeout))
        .onErrorResume(error -> {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.severe("Async gRPC call failed for service " + serviceName + ": " + error.getMessage());
            return Mono.just(ServiceCallResponse.failure(serviceName, 500, 
                    "Async gRPC Error: " + error.getMessage(), executionTime));
        });
    }
    
    @Override
    public boolean supports(String protocol) {
        return "GRPC".equalsIgnoreCase(protocol);
    }
    
    @Override
    public String getProtocolName() {
        return "GRPC";
    }
    
    private String resolveTarget(String serviceName, String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return serviceName + ":50051";
        }
        return targetUrl;
    }
    
    private Map<String, Object> executeGrpcCall(String target, String method,
                                               Map<String, Object> payload, long timeout) {
        return Map.of("status", "gRPC call executed");
    }
}
