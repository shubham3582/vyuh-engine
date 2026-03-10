package com.vyuh.orchestration.execution;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Execution context that carries state through the workflow
 * Now supports distributed storage with Aerospike
 */
public class ExecutionContext {

    @JsonProperty("executionId")
    private String executionId;

    @JsonProperty("workflowName")
    private String workflowName;

    @JsonProperty("currentStep")
    private String currentStep;

    @JsonProperty("startTime")
    private LocalDateTime startTime;

    @JsonProperty("endTime")
    private LocalDateTime endTime;

    @JsonProperty("variables")
    private Map<String, Object> variables = new HashMap<>();

    @JsonProperty("stepStack")
    private Stack<String> stepStack = new Stack<>();

    @JsonProperty("stepResults")
    private Map<String, Object> stepResults = new HashMap<>();

    @JsonProperty("status")
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("instanceId")
    private String instanceId; // Track which Vyuh instance owns this execution

    @JsonProperty("lastUpdated")
    private LocalDateTime lastUpdated;

    // Constructors
    public ExecutionContext() {}

    public ExecutionContext(String executionId, String workflowName) {
        this.executionId = executionId;
        this.workflowName = workflowName;
        this.startTime = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = ExecutionStatus.RUNNING;
    }

    public ExecutionContext(String executionId, String workflowName, String currentStep,
                           LocalDateTime startTime, LocalDateTime endTime,
                           Map<String, Object> variables, Stack<String> stepStack,
                           Map<String, Object> stepResults, ExecutionStatus status,
                           String errorMessage, String errorCode, String instanceId,
                           LocalDateTime lastUpdated) {
        this.executionId = executionId;
        this.workflowName = workflowName;
        this.currentStep = currentStep;
        this.startTime = startTime;
        this.endTime = endTime;
        this.variables = variables;
        this.stepStack = stepStack;
        this.stepResults = stepResults;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.instanceId = instanceId;
        this.lastUpdated = lastUpdated;
    }

    /**
     * Mark context as updated (for distributed state sync)
     */
    public void markUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    public void setVariable(String key, Object value) {
        this.variables.put(key, value);
        markUpdated();
    }
    
    public Object getVariable(String key) {
        return this.variables.get(key);
    }
    
    public void recordStepResult(String stepId, Object result) {
        this.stepResults.put(stepId, result);
    }
    
    public Object getStepResult(String stepId) {
        return this.stepResults.get(stepId);
    }

    // Getters and Setters
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }

    public Stack<String> getStepStack() { return stepStack; }
    public void setStepStack(Stack<String> stepStack) { this.stepStack = stepStack; }

    public Map<String, Object> getStepResults() { return stepResults; }
    public void setStepResults(Map<String, Object> stepResults) { this.stepResults = stepResults; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}

