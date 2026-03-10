package com.vyuh.orchestration.state;

import com.vyuh.orchestration.execution.ExecutionContext;
import java.util.Optional;

/**
 * Distributed state store interface for workflow execution contexts
 * Provides persistence and sharing across multiple Vyuh instances
 */
public interface DistributedStateStore {

    /**
     * Save execution context to distributed store
     */
    void save(ExecutionContext context);

    /**
     * Load execution context from distributed store
     */
    Optional<ExecutionContext> load(String executionId);

    /**
     * Update execution context in distributed store
     */
    void update(ExecutionContext context);

    /**
     * Delete execution context from distributed store
     */
    void delete(String executionId);

    /**
     * Check if execution context exists
     */
    boolean exists(String executionId);

    /**
     * Acquire distributed lock for execution context
     * Returns true if lock acquired, false if already locked
     */
    boolean acquireLock(String executionId, String instanceId, long timeoutMs);

    /**
     * Release distributed lock for execution context
     */
    void releaseLock(String executionId, String instanceId);

    /**
     * Get all execution IDs for a workflow (for monitoring/admin)
     */
    java.util.List<String> getAllExecutionIds(String workflowName);

    /**
     * Clean up old completed/failed executions
     */
    void cleanupOldExecutions(long maxAgeMs);
}