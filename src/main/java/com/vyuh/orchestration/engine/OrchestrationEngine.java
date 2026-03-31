package com.vyuh.orchestration.engine;

import com.vyuh.orchestration.config.OrchestrationConfig;
import com.vyuh.orchestration.config.ServiceConfig;
import com.vyuh.orchestration.config.WorkflowStep;
import com.vyuh.orchestration.execution.ExecutionContext;
import com.vyuh.orchestration.execution.ExecutionStatus;
import com.vyuh.orchestration.execution.ServiceCallResponse;
import com.vyuh.orchestration.handler.ProtocolHandler;
import com.vyuh.orchestration.state.DistributedStateStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Main orchestration engine for executing multi-level microservice workflows
 * Now supports distributed state management with Aerospike
 */
@Service
public class OrchestrationEngine {

    private static final Logger logger = Logger.getLogger(OrchestrationEngine.class.getName());

    private final List<ProtocolHandler> protocolHandlers;
    private final DistributedStateStore stateStore;

    @Value("${vyuh.instance.id:${random.uuid}}")
    private String instanceId;

    @Autowired
    public OrchestrationEngine(List<ProtocolHandler> protocolHandlers,
                              DistributedStateStore stateStore) {
        this.protocolHandlers = protocolHandlers;
        this.stateStore = stateStore;
    }
    
    /**
     * Execute workflow synchronously with distributed state management
     */
    public ExecutionContext executeSync(OrchestrationConfig config, Map<String, Object> inputData) {
        ExecutionContext context = new ExecutionContext(
                UUID.randomUUID().toString(),
                config.getWorkflowName()
        );

        // Set instance ownership for distributed coordination
        context.setInstanceId(instanceId);

        // Initialize context with input data
        if (inputData != null) {
            inputData.forEach(context::setVariable);
        }

        // Initialize with workflow context
        if (config.getContext() != null) {
            config.getContext().forEach(context::setVariable);
        }

        // Save initial state to distributed store
        try {
            stateStore.save(context);
            boolean loop = true;
            long globalTimeout = config.getGlobalTimeout() != null ? config.getGlobalTimeout() : Long.MAX_VALUE;
            long startTime = System.currentTimeMillis();
            
            String currentStepId = config.getStartStep();
            context.setCurrentStep(currentStepId);
            
            while (loop && (System.currentTimeMillis() - startTime) < globalTimeout) {
                if (currentStepId == null) {
                    loop = false;
                    context.setStatus(ExecutionStatus.COMPLETED);
                    stateStore.update(context); // Save completion state
                    break;
                }

                WorkflowStep step = config.getSteps().get(currentStepId);
                if (step == null) {
                    throw new IllegalArgumentException("Step not found: " + currentStepId);
                }

                logger.info("Executing step: " + currentStepId + " in workflow: " + config.getWorkflowName());

                // Update current step in distributed state
                context.setCurrentStep(currentStepId);
                stateStore.update(context);

                ServiceCallResponse response = executeSyncStep(config, step, context);
                context.recordStepResult(currentStepId, response);

                if (!response.isSuccess()) {
                    handleStepFailure(config, step, context);
                    stateStore.update(context); // Save failure state
                    loop = false;
                } else {
                    // Apply output mapping
                    applyOutputMapping(step, response, context);
                    
                    // Move to next step
                    if (step.getNextSteps() != null && !step.getNextSteps().isEmpty()) {
                        if (step.getParallel() != null && step.getParallel()) {
                            executeSyncParallelSteps(config, step.getNextSteps(), context);
                        }
                        currentStepId = step.getNextSteps().get(0);
                    } else {
                        loop = false;
                        context.setStatus(ExecutionStatus.COMPLETED);
                    }
                }
            }
            
            if ((System.currentTimeMillis() - startTime) >= globalTimeout) {
                context.setStatus(ExecutionStatus.FAILED);
                context.setErrorCode("TIMEOUT");
                context.setErrorMessage("Workflow execution exceeded global timeout");
                stateStore.update(context);
            }

        } catch (Exception e) {
            logger.severe("Workflow execution failed: " + e.getMessage());
            context.setStatus(ExecutionStatus.FAILED);
            context.setErrorCode("EXECUTION_ERROR");
            context.setErrorMessage(e.getMessage());
            stateStore.update(context);
        }

        context.setEndTime(LocalDateTime.now());
        return context;
    }
    
    /**
     * Execute workflow asynchronously
     */
    public Mono<ExecutionContext> executeAsync(OrchestrationConfig config, Map<String, Object> inputData) {
        ExecutionContext context = new ExecutionContext(
                UUID.randomUUID().toString(),
                config.getWorkflowName()
        );
        
        // Initialize context
        if (inputData != null) {
            inputData.forEach(context::setVariable);
        }
        if (config.getContext() != null) {
            config.getContext().forEach(context::setVariable);
        }
        
        return Mono.fromCallable(() -> executeAsyncWorkflow(config, context))
                .onErrorResume(error -> {
                    logger.severe("Async workflow execution failed: " + error.getMessage());
                    context.setStatus(ExecutionStatus.FAILED);
                    context.setErrorCode("EXECUTION_ERROR");
                    context.setErrorMessage(error.getMessage());
                    context.setEndTime(LocalDateTime.now());
                    return Mono.just(context);
                });
    }
    
    private ExecutionContext executeAsyncWorkflow(OrchestrationConfig config, ExecutionContext context) {
        String currentStepId = config.getStartStep();
        context.setCurrentStep(currentStepId);
        
        try {
            long globalTimeout = config.getGlobalTimeout() != null ? config.getGlobalTimeout() : Long.MAX_VALUE;
            long startTime = System.currentTimeMillis();
            
            while (currentStepId != null && (System.currentTimeMillis() - startTime) < globalTimeout) {
                WorkflowStep step = config.getSteps().get(currentStepId);
                if (step == null) {
                    throw new IllegalArgumentException("Step not found: " + currentStepId);
                }
                
                logger.info("Executing async step: " + currentStepId + " in workflow: " + config.getWorkflowName());
                
                Mono<ServiceCallResponse> asyncResponse = executeAsyncStep(config, step, context);
                asyncResponse.block();
                
                if (step.getNextSteps() != null && !step.getNextSteps().isEmpty()) {
                    currentStepId = step.getNextSteps().get(0);
                } else {
                    currentStepId = null;
                }
            }
            
            context.setStatus(ExecutionStatus.COMPLETED);
        } catch (Exception e) {
            logger.severe("Async workflow execution failed: " + e.getMessage());
            context.setStatus(ExecutionStatus.FAILED);
            context.setErrorCode("EXECUTION_ERROR");
            context.setErrorMessage(e.getMessage());
        }
        
        context.setEndTime(LocalDateTime.now());
        return context;
    }
    
    private ServiceCallResponse executeSyncStep(OrchestrationConfig config, WorkflowStep step, ExecutionContext context) {
        ServiceConfig serviceConfig = config.getServices().get(step.getServiceName());
        if (serviceConfig == null) {
            return ServiceCallResponse.failure(step.getServiceName(), 404, 
                    "Service configuration not found", 0);
        }
        
        Map<String, Object> payload = buildPayload(step, context);
        
        long timeout = step.getTimeout() != null ? step.getTimeout() : 
                      (serviceConfig.getTimeout() != null ? serviceConfig.getTimeout() : 30000);
        
        ProtocolHandler handler = findProtocolHandler(serviceConfig.getProtocol());
        if (handler == null) {
            return ServiceCallResponse.failure(step.getServiceName(), 400, 
                    "Unsupported protocol: " + serviceConfig.getProtocol(), 0);
        }
        
        return handler.executeSync(step.getServiceName(), serviceConfig.getUrl(), serviceConfig.getPath(), 
                serviceConfig.getMethod(), payload, serviceConfig.getHeaders(), timeout, context);
    }
    
    private Mono<ServiceCallResponse> executeAsyncStep(OrchestrationConfig config, WorkflowStep step, ExecutionContext context) {
        ServiceConfig serviceConfig = config.getServices().get(step.getServiceName());
        if (serviceConfig == null) {
            return Mono.just(ServiceCallResponse.failure(step.getServiceName(), 404, 
                    "Service configuration not found", 0));
        }
        
        Map<String, Object> payload = buildPayload(step, context);
        
        long timeout = step.getTimeout() != null ? step.getTimeout() : 
                      (serviceConfig.getTimeout() != null ? serviceConfig.getTimeout() : 30000);
        
        ProtocolHandler handler = findProtocolHandler(serviceConfig.getProtocol());
        if (handler == null) {
            return Mono.just(ServiceCallResponse.failure(step.getServiceName(), 400, 
                    "Unsupported protocol: " + serviceConfig.getProtocol(), 0));
        }
        
        return handler.executeAsync(step.getServiceName(), serviceConfig.getUrl(), serviceConfig.getPath(),
                serviceConfig.getMethod(), payload, serviceConfig.getHeaders(), timeout, context);
    }
    
    private void executeSyncParallelSteps(OrchestrationConfig config, List<String> stepIds, ExecutionContext context) {
        for (String stepId : stepIds) {
            WorkflowStep step = config.getSteps().get(stepId);
            if (step != null) {
                ServiceCallResponse response = executeSyncStep(config, step, context);
                context.recordStepResult(stepId, response);
                applyOutputMapping(step, response, context);
            }
        }
    }
    
    private void applyOutputMapping(WorkflowStep step, ServiceCallResponse response, ExecutionContext context) {
        if (step.getOutputMapping() != null && response.isSuccess()) {
            step.getOutputMapping().forEach((key, value) -> {
                Object mappedValue = extractValueFromResponse(response.getPayload(), value);
                context.setVariable(key, mappedValue);
            });
        }
    }
    
    private Map<String, Object> buildPayload(WorkflowStep step, ExecutionContext context) {
        Map<String, Object> payload = new HashMap<>();
        
        if (step.getInputMapping() != null) {
            step.getInputMapping().forEach((key, contextVar) -> {
                Object value = context.getVariable(contextVar);
                payload.put(key, value);
            });
        }
        
        return payload;
    }
    
    private Object extractValueFromResponse(Object response, String path) {
        if (response instanceof Map) {
            String[] parts = path.split("\\.");
            Object current = response;
            for (String part : parts) {
                if (current instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) current;
                    current = map.get(part);
                } else {
                    return null;
                }
            }
            return current;
        }
        return response;
    }
    
    private void handleStepFailure(OrchestrationConfig config, WorkflowStep step, ExecutionContext context) {
        if (step.getFallback() != null) {
            logger.info("Executing fallback step: " + step.getFallback() + " for failed step: " + step.getId());
            WorkflowStep fallbackStep = config.getSteps().get(step.getFallback());
            if (fallbackStep != null) {
                ServiceCallResponse response = executeSyncStep(config, fallbackStep, context);
                context.recordStepResult(step.getFallback(), response);
            }
        }
    }
    
    private ProtocolHandler findProtocolHandler(String protocol) {
        return protocolHandlers.stream()
                .filter(handler -> handler.supports(protocol))
                .findFirst()
                .orElse(null);
    }
}
