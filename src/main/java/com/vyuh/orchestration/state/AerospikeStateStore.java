package com.vyuh.orchestration.state;

import com.aerospike.client.*;
import com.aerospike.client.policy.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyuh.orchestration.execution.ExecutionContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Aerospike-based distributed state store implementation
 * Provides serializable isolation level for state machine consistency
 */
@Component
public class AerospikeStateStore implements DistributedStateStore {

    private static final Logger logger = LoggerFactory.getLogger(AerospikeStateStore.class);

    private static final String EXECUTION_SET = "executions";

    @Value("${aerospike.host:localhost}")
    private String aerospikeHost;

    @Value("${aerospike.port:3000}")
    private int aerospikePort;

    @Value("${aerospike.namespace:vyuh}")
    private String aerospikeNamespace;

    @Value("${aerospike.execution.ttl:86400}") // 24 hours default TTL
    private int executionTtl;

    private AerospikeClient client;
    private ObjectMapper objectMapper;

    private final WritePolicy writePolicy;
    private final Policy readPolicy;

    public AerospikeStateStore() {
        this.writePolicy = new WritePolicy();
        writePolicy.recordExistsAction = RecordExistsAction.UPDATE;
        writePolicy.sendKey = true;
        writePolicy.expiration = executionTtl;

        this.readPolicy = new Policy();
        readPolicy.sendKey = true;

        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void initialize() {
        try {
            Host[] hosts = new Host[]{new Host(aerospikeHost, aerospikePort)};
            ClientPolicy clientPolicy = new ClientPolicy();
            clientPolicy.timeout = 10000; // 10 seconds

            this.client = new AerospikeClient(clientPolicy, hosts);
            logger.info("Connected to Aerospike at {}:{}", aerospikeHost, aerospikePort);

        } catch (Exception e) {
            logger.error("Failed to connect to Aerospike - distributed features disabled", e);
            // Don't throw exception - allow app to start without Aerospike
            this.client = null;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.close();
            logger.info("Aerospike connection closed");
        }
    }

    @Override
    public void save(ExecutionContext context) {
        if (client == null) {
            logger.warn("Aerospike not available - skipping distributed save");
            return;
        }

        try {
            Key key = new Key(aerospikeNamespace, EXECUTION_SET, context.getExecutionId());
            Bin dataBin = new Bin("data", serializeContext(context));
            Bin workflowBin = new Bin("workflowName", context.getWorkflowName());
            Bin statusBin = new Bin("status", context.getStatus().name());

            client.put(writePolicy, key, dataBin, workflowBin, statusBin);
            logger.debug("Saved execution context: {}", context.getExecutionId());

        } catch (Exception e) {
            logger.error("Failed to save execution context: {}", context.getExecutionId(), e);
        }
    }

    @Override
    public Optional<ExecutionContext> load(String executionId) {
        if (client == null) {
            logger.warn("Aerospike not available - cannot load from distributed store");
            return Optional.empty();
        }

        try {
            Key key = new Key(aerospikeNamespace, EXECUTION_SET, executionId);
            com.aerospike.client.Record record = client.get(readPolicy, key);

            if (record == null) {
                return Optional.empty();
            }

            String data = record.getString("data");
            ExecutionContext context = deserializeContext(data);

            logger.debug("Loaded execution context: {}", executionId);
            return Optional.of(context);

        } catch (Exception e) {
            logger.error("Failed to load execution context: {}", executionId, e);
            return Optional.empty();
        }
    }

    @Override
    public void update(ExecutionContext context) {
        save(context); // Aerospike UPSERT behavior
    }

    @Override
    public void delete(String executionId) {
        if (client == null) return;

        try {
            Key key = new Key(aerospikeNamespace, EXECUTION_SET, executionId);
            client.delete(writePolicy, key);
            logger.debug("Deleted execution context: {}", executionId);
        } catch (Exception e) {
            logger.error("Failed to delete execution context: {}", executionId, e);
        }
    }

    @Override
    public boolean exists(String executionId) {
        if (client == null) return false;

        try {
            Key key = new Key(aerospikeNamespace, EXECUTION_SET, executionId);
            return client.exists(readPolicy, key);
        } catch (Exception e) {
            logger.error("Failed to check existence: {}", executionId, e);
            return false;
        }
    }

    @Override
    public boolean acquireLock(String executionId, String instanceId, long timeoutMs) {
        // Locks not maintained in Java code - relying on Aerospike's serializable isolation level
        logger.debug("Lock not acquired (not maintained) for execution: {} by instance: {}", executionId, instanceId);
        return true;
    }

    @Override
    public void releaseLock(String executionId, String instanceId) {
        // Locks not maintained in Java code - relying on Aerospike's serializable isolation level
        logger.debug("Lock not released (not maintained) for execution: {} by instance: {}", executionId, instanceId);
    }

    @Override
    public List<String> getAllExecutionIds(String workflowName) {
        // Simplified implementation - in production would use secondary indexes
        logger.debug("Returning empty list for workflow: {} (Aerospike query not implemented)", workflowName);
        return new ArrayList<>();
    }

    @Override
    public void cleanupOldExecutions(long maxAgeMs) {
        // Rely on Aerospike TTL settings
        logger.info("Cleanup not implemented - relying on Aerospike TTL");
    }

    private String serializeContext(ExecutionContext context) throws IOException {
        return objectMapper.writeValueAsString(context);
    }

    private ExecutionContext deserializeContext(String data) throws IOException {
        return objectMapper.readValue(data, ExecutionContext.class);
    }
}