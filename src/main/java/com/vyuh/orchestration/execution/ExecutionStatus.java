package com.vyuh.orchestration.execution;

/**
 * Execution status for workflow tracking
 */
public enum ExecutionStatus {
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    PAUSED
}
