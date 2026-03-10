package com.vyuh.orchestration.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Complete workflow configuration for orchestration
 */
public class OrchestrationConfig {
    
    @JsonProperty("workflowName")
    private String workflowName;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("services")
    private Map<String, ServiceConfig> services;
    
    @JsonProperty("steps")
    private Map<String, WorkflowStep> steps;
    
    @JsonProperty("startStep")
    private String startStep;
    
    @JsonProperty("globalTimeout")
    private Long globalTimeout;
    
    @JsonProperty("globalRetries")
    private Integer globalRetries;
    
    @JsonProperty("errorHandler")
    private Map<String, String> errorHandler;
    
    @JsonProperty("context")
    private Map<String, Object> context;

    // Constructors
    public OrchestrationConfig() {}

    public OrchestrationConfig(String workflowName, String version, 
                              Map<String, ServiceConfig> services,
                              Map<String, WorkflowStep> steps, String startStep,
                              Long globalTimeout, Integer globalRetries,
                              Map<String, String> errorHandler,
                              Map<String, Object> context) {
        this.workflowName = workflowName;
        this.version = version;
        this.services = services;
        this.steps = steps;
        this.startStep = startStep;
        this.globalTimeout = globalTimeout;
        this.globalRetries = globalRetries;
        this.errorHandler = errorHandler;
        this.context = context;
    }

    // Getters and Setters
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Map<String, ServiceConfig> getServices() { return services; }
    public void setServices(Map<String, ServiceConfig> services) { this.services = services; }

    public Map<String, WorkflowStep> getSteps() { return steps; }
    public void setSteps(Map<String, WorkflowStep> steps) { this.steps = steps; }

    public String getStartStep() { return startStep; }
    public void setStartStep(String startStep) { this.startStep = startStep; }

    public Long getGlobalTimeout() { return globalTimeout; }
    public void setGlobalTimeout(Long globalTimeout) { this.globalTimeout = globalTimeout; }

    public Integer getGlobalRetries() { return globalRetries; }
    public void setGlobalRetries(Integer globalRetries) { this.globalRetries = globalRetries; }

    public Map<String, String> getErrorHandler() { return errorHandler; }
    public void setErrorHandler(Map<String, String> errorHandler) { this.errorHandler = errorHandler; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
}
