package com.vyuh.orchestration.api;

import java.util.Map;

/**
 * Request object for workflow execution
 */
public class WorkflowExecutionRequest {
    private String configPath;
    private Map<String, Object> variables;

    public WorkflowExecutionRequest() {
    }

    public WorkflowExecutionRequest(String configPath, Map<String, Object> variables) {
        this.configPath = configPath;
        this.variables = variables;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
