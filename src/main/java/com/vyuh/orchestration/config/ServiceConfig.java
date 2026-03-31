package com.vyuh.orchestration.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Configuration for a microservice endpoint
 */
public class ServiceConfig {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("protocol")
    private String protocol; // HTTP, GRPC, KAFKA
    
    @JsonProperty("url")
    private String url; // For HTTP and gRPC
    
    @JsonProperty("topic")
    private String topic; // For Kafka
    
    @JsonProperty("method")
    private String method; // For HTTP: GET, POST, PUT, DELETE
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("timeout")
    private Long timeout; // milliseconds
    
    @JsonProperty("retries")
    private Integer retries;
    
    @JsonProperty("headers")
    private Map<String, String> headers;
    
    @JsonProperty("async")
    private Boolean async;
    
    @JsonProperty("kafkaProperties")
    private Map<String, String> kafkaProperties;

    @JsonProperty("openapiSpec")
    private String openapiSpec;

    @JsonProperty("operationId")
    private String operationId;

    @JsonProperty("protoFile")
    private String protoFile;

    @JsonProperty("protoService")
    private String protoService;

    @JsonProperty("protoMethod")
    private String protoMethod;

    // Constructors
    public ServiceConfig() {}

    public ServiceConfig(String name, String protocol, String url, String path, 
                        String method, Long timeout, Integer retries, 
                        Map<String, String> headers, Boolean async,
                        String topic, Map<String, String> kafkaProperties) {
        this.name = name;
        this.protocol = protocol;
        this.url = url;
        this.path = path;
        this.method = method;
        this.timeout = timeout;
        this.retries = retries;
        this.headers = headers;
        this.async = async;
        this.topic = topic;
        this.kafkaProperties = kafkaProperties;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Long getTimeout() { return timeout; }
    public void setTimeout(Long timeout) { this.timeout = timeout; }

    public Integer getRetries() { return retries; }
    public void setRetries(Integer retries) { this.retries = retries; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public Boolean getAsync() { return async; }
    public void setAsync(Boolean async) { this.async = async; }

    public Map<String, String> getKafkaProperties() { return kafkaProperties; }
    public void setKafkaProperties(Map<String, String> kafkaProperties) { this.kafkaProperties = kafkaProperties; }

    public String getOpenapiSpec() { return openapiSpec; }
    public void setOpenapiSpec(String openapiSpec) { this.openapiSpec = openapiSpec; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    public String getProtoFile() { return protoFile; }
    public void setProtoFile(String protoFile) { this.protoFile = protoFile; }

    public String getProtoService() { return protoService; }
    public void setProtoService(String protoService) { this.protoService = protoService; }

    public String getProtoMethod() { return protoMethod; }
    public void setProtoMethod(String protoMethod) { this.protoMethod = protoMethod; }
}

