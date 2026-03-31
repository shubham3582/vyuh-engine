package com.vyuh.orchestration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * Holds the single workflow configuration loaded into memory at startup.
 * Only one workflow is allowed per instance.
 */
@Component
public class WorkflowConfigHolder {

    private static final Logger logger = Logger.getLogger(WorkflowConfigHolder.class.getName());

    private final String configPath;
    private final ConfigurationLoader configurationLoader;
    private OrchestrationConfig workflowConfig;

    public WorkflowConfigHolder(@Value("${vyuh.workflow.config-path}") String configPath,
                                ConfigurationLoader configurationLoader) {
        this.configPath = configPath;
        this.configurationLoader = configurationLoader;
    }

    @PostConstruct
    public void init() throws Exception {
        logger.info("Loading workflow configuration from: " + configPath);
        this.workflowConfig = configurationLoader.load(configPath);

        if (this.workflowConfig == null) {
            throw new IllegalStateException("Workflow configuration could not be loaded from: " + configPath);
        }

        if (this.workflowConfig.getWorkflowName() == null || this.workflowConfig.getWorkflowName().isBlank()) {
            throw new IllegalStateException("Loaded workflow configuration must define workflowName");
        }

        if (this.workflowConfig.getSteps() == null || this.workflowConfig.getSteps().isEmpty()) {
            throw new IllegalStateException("Loaded workflow configuration must contain at least one step");
        }

        if (this.workflowConfig.getServices() == null || this.workflowConfig.getServices().isEmpty()) {
            throw new IllegalStateException("Loaded workflow configuration must contain service definitions");
        }

        logger.info("Workflow configuration loaded successfully: " + this.workflowConfig.getWorkflowName());
    }

    public OrchestrationConfig getWorkflowConfig() {
        return workflowConfig;
    }

    public String getConfigPath() {
        return configPath;
    }

    public String getWorkflowName() {
        return workflowConfig.getWorkflowName();
    }
}
