package com.vyuh.orchestration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Loads orchestration configuration from YAML or JSON files
 */
@Component
public class ConfigurationLoader {
    
    private static final Logger logger = Logger.getLogger(ConfigurationLoader.class.getName());
    
    private final ObjectMapper jsonObjectMapper;
    private final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    
    public ConfigurationLoader(ObjectMapper jsonObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
    }
    
    /**
     * Load configuration from JSON file
     */
    public OrchestrationConfig loadFromJson(String filePath) throws IOException {
        logger.info("Loading orchestration config from JSON: " + filePath);
        return jsonObjectMapper.readValue(new File(filePath), OrchestrationConfig.class);
    }
    
    /**
     * Load configuration from YAML file
     */
    public OrchestrationConfig loadFromYaml(String filePath) throws IOException {
        logger.info("Loading orchestration config from YAML: " + filePath);
        return yamlObjectMapper.readValue(new File(filePath), OrchestrationConfig.class);
    }
    
    /**
     * Load configuration from string content
     */
    public OrchestrationConfig loadFromYamlString(String content) throws IOException {
        logger.info("Loading orchestration config from YAML string");
        return yamlObjectMapper.readValue(content, OrchestrationConfig.class);
    }
    
    /**
     * Load configuration from JSON string
     */
    public OrchestrationConfig loadFromJsonString(String content) throws IOException {
        logger.info("Loading orchestration config from JSON string");
        return jsonObjectMapper.readValue(content, OrchestrationConfig.class);
    }
    
    /**
     * Auto-detect and load configuration from file or classpath
     */
    public OrchestrationConfig load(String filePath) throws IOException {
        String content;
        
        // Try loading from classpath first
        try {
            Resource resource = new ClassPathResource(filePath);
            if (resource.exists()) {
                logger.info("Loading configuration from classpath: " + filePath);
                content = Files.readString(resource.getFile().toPath());
            } else {
                // Fall back to file system
                logger.info("Loading configuration from file system: " + filePath);
                Path path = Path.of(filePath);
                content = Files.readString(path);
            }
        } catch (IOException e) {
            logger.info("Failed to load from classpath, trying file system: " + filePath);
            // Fall back to file system
            Path path = Path.of(filePath);
            content = Files.readString(path);
        }
        
        if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            return loadFromYamlString(content);
        } else if (filePath.endsWith(".json")) {
            return loadFromJsonString(content);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }
}
