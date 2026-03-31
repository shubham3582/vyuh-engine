package com.vyuh.orchestration.api;

import com.vyuh.orchestration.config.OrchestrationConfig;
import com.vyuh.orchestration.config.WorkflowConfigHolder;
import com.vyuh.orchestration.engine.OrchestrationEngine;
import com.vyuh.orchestration.engine.WorkflowRecoveryService;
import com.vyuh.orchestration.execution.ExecutionContext;
import com.vyuh.orchestration.state.DistributedStateStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * REST API for orchestration engine with distributed state management.
 */
@RestController
@RequestMapping("/api/orchestration")
public class OrchestrationController {

    private static final Logger logger = Logger.getLogger(OrchestrationController.class.getName());

    private final OrchestrationEngine orchestrationEngine;
    private final WorkflowConfigHolder workflowConfigHolder;
    private final WorkflowRecoveryService recoveryService;
    private final DistributedStateStore stateStore;

    @Autowired
    public OrchestrationController(OrchestrationEngine orchestrationEngine,
                                   WorkflowConfigHolder workflowConfigHolder,
                                   WorkflowRecoveryService recoveryService,
                                   DistributedStateStore stateStore) {
        this.orchestrationEngine = orchestrationEngine;
        this.workflowConfigHolder = workflowConfigHolder;
        this.recoveryService = recoveryService;
        this.stateStore = stateStore;
    }
    
    /**
     * Execute workflow synchronously
     */
    @PostMapping("/execute/sync")
    public ResponseEntity<ExecutionContext> executeSyncWorkflow(
            @RequestBody WorkflowExecutionRequest request) {
        try {
            if (request == null) {
                logger.severe("Missing workflow execution request");
                return ResponseEntity.badRequest().build();
            }

            OrchestrationConfig config = workflowConfigHolder.getWorkflowConfig();
            ExecutionContext context = orchestrationEngine.executeSync(config, request.getVariables());
            return ResponseEntity.ok(context);
        } catch (Exception e) {
            logger.severe("Workflow execution failed: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Execute workflow asynchronously
     */
    @PostMapping("/execute/async")
    public Mono<ResponseEntity<ExecutionContext>> executeAsyncWorkflow(
            @RequestBody WorkflowExecutionRequest request) {
        try {
            if (request == null) {
                logger.severe("Missing workflow execution request");
                return Mono.just(ResponseEntity.badRequest().build());
            }

            OrchestrationConfig config = workflowConfigHolder.getWorkflowConfig();
            return orchestrationEngine.executeAsync(config, request.getVariables())
                    .map(ResponseEntity::ok)
                    .onErrorResume(e -> {
                        logger.severe("Workflow execution failed: " + e.getMessage());
                        return Mono.just(ResponseEntity.internalServerError().build());
                    });
        } catch (Exception e) {
            logger.severe("Workflow execution failed: " + e.getMessage());
            return Mono.just(ResponseEntity.internalServerError().build());
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Orchestration Engine"));
    }

    /**
     * Resume a workflow execution
     */
    @PostMapping("/resume/{executionId}")
    public ResponseEntity<ExecutionContext> resumeWorkflow(@PathVariable String executionId) {
        try {
            Optional<ExecutionContext> contextOpt = recoveryService.resumeWorkflow(executionId);
            if (contextOpt.isPresent()) {
                return ResponseEntity.ok(contextOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Failed to resume workflow " + executionId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get execution context by ID
     */
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<ExecutionContext> getExecution(@PathVariable String executionId) {
        try {
            Optional<ExecutionContext> contextOpt = stateStore.load(executionId);
            if (contextOpt.isPresent()) {
                return ResponseEntity.ok(contextOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Failed to load execution " + executionId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all execution IDs for a workflow
     */
    @GetMapping("/executions/{workflowName}")
    public ResponseEntity<List<String>> getWorkflowExecutions(@PathVariable String workflowName) {
        try {
            List<String> executionIds = stateStore.getAllExecutionIds(workflowName);
            return ResponseEntity.ok(executionIds);
        } catch (Exception e) {
            logger.severe("Failed to get executions for workflow " + workflowName + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if execution exists
     */
    @RequestMapping(value = "/execution/{executionId}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> executionExists(@PathVariable String executionId) {
        try {
            if (stateStore.exists(executionId)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.severe("Failed to check existence of execution " + executionId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
