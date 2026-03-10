package com.vyuh.orchestration;

import com.vyuh.orchestration.config.ConfigurationLoader;
import com.vyuh.orchestration.config.OrchestrationConfig;
import com.vyuh.orchestration.engine.OrchestrationEngine;
import com.vyuh.orchestration.execution.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for OrchestrationEngine
 */
@SpringBootTest
public class OrchestrationEngineTest {
    
    @Autowired
    private OrchestrationEngine orchestrationEngine;
    
    @Autowired
    private ConfigurationLoader configurationLoader;
    
    private OrchestrationConfig testConfig;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Load example configuration
        testConfig = configurationLoader.load("examples/payment-workflow.yaml");
    }
    
    @Test
    public void testSyncWorkflowExecution() {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("amount", 100.0);
        inputData.put("currency", "USD");
        inputData.put("customer_id", "CUST123");
        inputData.put("order_id", "ORD456");
        
        ExecutionContext context = orchestrationEngine.executeSync(testConfig, inputData);
        
        assertNotNull(context);
        assertNotNull(context.getExecutionId());
        System.out.println("Workflow execution ID: " + context.getExecutionId());
        System.out.println("Workflow status: " + context.getStatus());
    }
    
    @Test
    public void testYamlConfigurationLoading() throws IOException {
        OrchestrationConfig config = configurationLoader.load("examples/payment-workflow.yaml");
        
        assertNotNull(config);
        assertNotNull(config.getWorkflowName());
        assertNotNull(config.getServices());
        assertNotNull(config.getSteps());
        
        System.out.println("Loaded workflow: " + config.getWorkflowName());
        System.out.println("Number of services: " + config.getServices().size());
        System.out.println("Number of steps: " + config.getSteps().size());
    }
}
