package com.vyuh.orchestration.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Defines a single step in the orchestration workflow
 */
public class WorkflowStep {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("serviceName")
    private String serviceName;
    
    @JsonProperty("inputMapping")
    private Map<String, String> inputMapping;
    
    @JsonProperty("outputMapping")
    private Map<String, String> outputMapping;
    
    @JsonProperty("nextSteps")
    private List<String> nextSteps;
    
    @JsonProperty("parallel")
    private Boolean parallel;
    
    @JsonProperty("retryPolicy")
    private Map<String, Object> retryPolicy;
    
    @JsonProperty("fallback")
    private String fallback;
    
    @JsonProperty("timeout")
    private Long timeout;
    
    @JsonProperty("async")
    private Boolean async;

    // Constructors
    public WorkflowStep() {}
    
    public WorkflowStep(String id, String serviceName, Map<String, String> inputMapping,
                       Map<String, String> outputMapping, List<String> nextSteps,
                       Boolean parallel, Map<String, Object> retryPolicy, String fallback,
                       Long timeout, Boolean async) {
        this.id = id;
        this.serviceName = serviceName;
        this.inputMapping = inputMapping;
        this.outputMapping = outputMapping;
        this.nextSteps = nextSteps;
        this.parallel = parallel;
        this.retryPolicy = retryPolicy;
        this.fallback = fallback;
        this.timeout = timeout;
        this.async = async;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public Map<String, String> getInputMapping() { return inputMapping; }
    public void setInputMapping(Map<String, String> inputMapping) { this.inputMapping = inputMapping; }

    public Map<String, String> getOutputMapping() { return outputMapping; }
    public void setOutputMapping(Map<String, String> outputMapping) { this.outputMapping = outputMapping; }

    public List<String> getNextSteps() { return nextSteps; }
    public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }

    public Boolean getParallel() { return parallel; }
    public void setParallel(Boolean parallel) { this.parallel = parallel; }

    public Map<String, Object> getRetryPolicy() { return retryPolicy; }
    public void setRetryPolicy(Map<String, Object> retryPolicy) { this.retryPolicy = retryPolicy; }

    public String getFallback() { return fallback; }
    public void setFallback(String fallback) { this.fallback = fallback; }

    public Long getTimeout() { return timeout; }
    public void setTimeout(Long timeout) { this.timeout = timeout; }

    public Boolean getAsync() { return async; }
    public void setAsync(Boolean async) { this.async = async; }
}

