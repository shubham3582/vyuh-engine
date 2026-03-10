# Vyuh - State Machine Engine

A powerful, configuration-driven state machine engine for orchestrating complex workflows across microservices. Vyuh supports multiple communication protocols (HTTP, gRPC, Kafka) with both synchronous and asynchronous execution patterns.

## Features

✅ **Multi-Protocol Support**
- HTTP/REST calls
- gRPC for high-performance RPC
- Kafka pub/sub messaging for event-driven workflows

✅ **Flexible Execution Models**
- Synchronous execution with blocking calls
- Asynchronous execution with reactive streams
- Parallel step execution
- Sequential and conditional routing

✅ **Advanced Workflow Capabilities**
- Multi-level workflow composition (nested service calls)
- Input/output mapping between steps
- Retry policies and fallback handlers
- Global and step-specific timeouts
- Error handling with custom error routes

✅ **Configuration-Driven**
- YAML and JSON configuration support
- Runtime workflow definition without code changes
- Service registry and endpoint management
- Context variables and state management

✅ **Security & Performance**
- **CVE Patched**: All critical and high-severity security vulnerabilities fixed
- **Application Warm-up**: Pre-initializes components for optimal startup performance
- **Updated Dependencies**: Latest secure versions of all libraries

## Architecture

### Core Components

1. **OrchestrationEngine** - Main orchestration coordinator
   - Manages workflow execution (sync/async)
   - Handles step transitions
   - Manages execution context

2. **ProtocolHandlers** - Protocol-specific implementations
   - `HttpProtocolHandler` - REST API calls
   - `GrpcProtocolHandler` - gRPC service calls
   - `KafkaProtocolHandler` - Kafka messaging

3. **Configuration Models**
   - `OrchestrationConfig` - Complete workflow configuration
   - `WorkflowStep` - Individual step definition
   - `ServiceConfig` - Service endpoint configuration

4. **Execution Context**
   - Tracks workflow execution state
   - Manages variables and step results
   - Records execution path and timing

## Configuration Structure

### Example YAML Workflow

```yaml
workflowName: payment-processing-workflow
version: "1.0"

services:
  validation-service:
    protocol: HTTP
    url: http://validation-service:8081
    path: /api/validate
    method: POST
    timeout: 5000

  payment-service:
    protocol: GRPC
    url: payment-service:50051
    path: /payment.PaymentService/ProcessPayment
    method: POST
    timeout: 10000

steps:
  validate-payment:
    id: validate-payment
    serviceName: validation-service
    inputMapping:
      paymentAmount: amount
      paymentCurrency: currency
    outputMapping:
      validationResult: result
    nextSteps:
      - process-payment

startStep: validate-payment
globalTimeout: 60000
```

## Security & Performance

### CVE Security Patches
All critical and high-severity Common Vulnerabilities and Exposures (CVEs) have been addressed:

- **Jackson Core**: Updated to 2.16.2 (fixes GHSA-72hv-8253-57qq)
- **Google Protobuf**: Updated to 3.25.5 (fixes CVE-2024-7254)
- **Netty**: Updated to 4.1.117.Final (fixes CVE-2025-55163 - MadeYouReset)
- **Apache Tomcat**: Updated to 10.1.50 (fixes multiple CVEs including CVE-2025-24813)
- **Json-smart**: Updated to 2.5.2 (fixes CVE-2024-57699)
- **AssertJ**: Updated to 3.27.7 (fixes CVE-2026-24400)
- **LZ4 Java**: Updated to 1.8.1 (fixes CVE-2025-12183, CVE-2025-66566)

### Application Warm-up
The application includes automatic warm-up functionality that pre-initializes key components during startup:

- **ObjectMapper**: Pre-compiles JSON serialization
- **RestTemplate**: Initializes synchronous HTTP client
- **WebClient**: Initializes reactive HTTP client

This ensures optimal performance from the first request and reduces cold-start latency.

## API Endpoints

### Execute Workflow Synchronously

```bash
POST /api/orchestration/execute/sync?configPath=examples/payment-workflow.yaml

Request Body:
{
  "amount": 100.0,
  "currency": "USD",
  "customer_id": "CUST123"
}

Response:
{
  "executionId": "uuid",
  "workflowName": "payment-processing-workflow",
  "status": "COMPLETED",
  "variables": {...},
  "stepResults": {...}
}
```

### Execute Workflow Asynchronously

```bash
POST /api/orchestration/execute/async?configPath=examples/payment-workflow.yaml

Request Body:
{
  "amount": 100.0,
  "currency": "USD"
}
```

### Health Check

```bash
GET /api/orchestration/health
```

## Usage Examples

### 1. Synchronous Workflow Execution

```java
@Autowired
private OrchestrationEngine orchestrationEngine;

@Autowired
private ConfigurationLoader configLoader;

// Load configuration
OrchestrationConfig config = configLoader.load("workflows/payment.yaml");

// Prepare input data
Map<String, Object> input = new HashMap<>();
input.put("amount", 100.0);
input.put("currency", "USD");

// Execute synchronously
ExecutionContext context = orchestrationEngine.executeSync(config, input);

// Check results
if (context.getStatus() == ExecutionStatus.COMPLETED) {
    Object paymentResult = context.getStepResult("process-payment");
}
```

### 2. Asynchronous Workflow Execution

```java
// Execute asynchronously
orchestrationEngine.executeAsync(config, input)
    .subscribe(
        context -> System.out.println("Workflow completed: " + context.getStatus()),
        error -> System.err.println("Workflow failed: " + error.getMessage())
    );
```

### 3. Multi-Level Workflow (Chained Service Calls)

```yaml
steps:
  step1:
    serviceName: service-a
    nextSteps: [step2, step3]  # Call both step2 and step3
    parallel: true

  step2:
    serviceName: service-b
    nextSteps: [step4]

  step3:
    serviceName: service-c
    nextSteps: [step4]

  step4:
    serviceName: service-d
    nextSteps: []
```

### 4. Error Handling and Fallbacks

```yaml
steps:
  process-payment:
    serviceName: payment-service
    fallback: handle-payment-failure
    timeout: 10000

  handle-payment-failure:
    serviceName: notification-service
    nextSteps: []
    
globalErrorHandler:
  TIMEOUT: handle-payment-failure
  PAYMENT_FAILED: handle-payment-failure
```

## Building and Running

### Prerequisites
- Java 21+
- Maven 3.9.12+
- Spring Boot 3.2.12+
- Kafka (optional, for Kafka support)

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Run Tests

```bash
mvn test
```

## Configuration Properties

### Global Timeout
- `globalTimeout` - Maximum time for entire workflow execution (milliseconds)

### Retry Policy
- `globalRetries` - Default retry count for failed steps
- `step.retryPolicy` - Per-step retry configuration

### Context Variables
- `context` - Initial variables available to all steps
- `inputMapping` - Maps external input to step parameters
- `outputMapping` - Extracts step output to context variables

## Error Codes

- `TIMEOUT` - Workflow exceeded timeout
- `EXECUTION_ERROR` - General execution error
- `NOT_FOUND` - Service or step not found
- `UNSUPPORTED_PROTOCOL` - Protocol not supported

## Performance Considerations

1. **Async Execution** - Use for I/O-bound operations and independent steps
2. **Parallel Steps** - Execute independent steps concurrently
3. **Connection Pooling** - HTTP handlers use connection pools
4. **Reactive Streams** - WebClient for non-blocking async HTTP calls

## Extensibility

### Add Custom Protocol Handler

```java
@Component
public class CustomProtocolHandler implements ProtocolHandler {
    
    @Override
    public ServiceCallResponse executeSync(...) {
        // Implementation
    }
    
    @Override
    public Mono<ServiceCallResponse> executeAsync(...) {
        // Implementation
    }
    
    @Override
    public boolean supports(String protocol) {
        return "CUSTOM".equalsIgnoreCase(protocol);
    }
    
    @Override
    public String getProtocolName() {
        return "CUSTOM";
    }
}
```

## Monitoring and Logging

The engine logs workflow execution at DEBUG level:

```
com.payments.orchestration.engine - Executing step: validate-payment
com.payments.orchestration.handler - Executing HTTP POST request to http://validation-service:8081/api/validate
```

## Future Enhancements

- [ ] Workflow versioning and rollback
- [ ] Circuit breaker pattern for service calls
- [ ] Dead letter queue for failed messages
- [ ] Workflow visualization and dashboard
- [ ] Dynamic step scheduling
- [ ] Rate limiting and throttling
- [ ] Distributed tracing support

## License

MIT

## Contributing

Contributions welcome! Please submit PRs with tests and documentation.
