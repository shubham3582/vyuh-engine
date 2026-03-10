package com.vyuh.orchestration.execution;

import java.time.LocalDateTime;

/**
 * Response from a microservice call
 */
public class ServiceCallResponse {
    
    private String serviceId;
    private int statusCode;
    private String statusMessage;
    private Object payload;
    private String contentType;
    private long executionTimeMs;
    private LocalDateTime timestamp;
    private String correlationId;
    private boolean success;
    
    // Constructors
    public ServiceCallResponse() {}
    
    public ServiceCallResponse(String serviceId, int statusCode, String statusMessage,
                              Object payload, String contentType, long executionTimeMs,
                              LocalDateTime timestamp, String correlationId, boolean success) {
        this.serviceId = serviceId;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.payload = payload;
        this.contentType = contentType;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.success = success;
    }
    
    public static ServiceCallResponse success(String serviceId, Object payload, long executionTimeMs) {
        ServiceCallResponse response = new ServiceCallResponse();
        response.setServiceId(serviceId);
        response.setPayload(payload);
        response.setStatusCode(200);
        response.setStatusMessage("SUCCESS");
        response.setSuccess(true);
        response.setExecutionTimeMs(executionTimeMs);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    public static ServiceCallResponse failure(String serviceId, int statusCode, String message, long executionTimeMs) {
        ServiceCallResponse response = new ServiceCallResponse();
        response.setServiceId(serviceId);
        response.setStatusCode(statusCode);
        response.setStatusMessage(message);
        response.setSuccess(false);
        response.setExecutionTimeMs(executionTimeMs);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}

