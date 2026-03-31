package com.vyuh.orchestration.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Request object for workflow execution.
 * The workflow is loaded in memory at startup, so only variables are required.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowExecutionRequest {
    private Map<String, Object> variables;

    public WorkflowExecutionRequest() {
    }

    public WorkflowExecutionRequest(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
