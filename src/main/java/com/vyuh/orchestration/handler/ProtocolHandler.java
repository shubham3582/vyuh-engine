package com.vyuh.orchestration.handler;

import com.vyuh.orchestration.execution.ExecutionContext;
import com.vyuh.orchestration.execution.ServiceCallResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Interface for protocol-specific service handlers
 */
public interface ProtocolHandler {
    
    /**
     * Execute a service call synchronously
     */
    ServiceCallResponse executeSync(String serviceName, String url, String path, String method, 
                                   Map<String, Object> payload, Map<String, String> headers,
                                   long timeout, ExecutionContext context);
    
    /**
     * Execute a service call asynchronously
     */
    Mono<ServiceCallResponse> executeAsync(String serviceName, String url, String path, String method,
                                          Map<String, Object> payload, Map<String, String> headers,
                                          long timeout, ExecutionContext context);
    
    /**
     * Check if this handler supports the given protocol
     */
    boolean supports(String protocol);
    
    /**
     * Get the protocol name
     */
    String getProtocolName();
}
