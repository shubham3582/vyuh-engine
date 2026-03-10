#!/bin/bash

# Test the simple HTTP workflow with proper error handling

set -e

cd /Users/shubham/payments-engine/vyuh-engine

# Build
echo "Building Vyuh Engine..."
JAVA_HOME=/opt/homebrew/opt/openjdk mvn clean install -q -DskipTests
echo "✅ Build complete"

# Start app
echo ""
echo "Starting Vyuh Engine..."
JAVA_HOME=/opt/homebrew/opt/openjdk java -jar target/vyuh-engine-1.0-SNAPSHOT.jar --server.port=8080 &
APP_PID=$!
sleep 6

# Test health
echo "Testing health..."
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
echo ""
echo "✅ Health check passed"

# Execute workflow
echo ""
echo "Executing workflow..."
curl -s -X POST http://localhost:8080/api/orchestration/execute/sync \
  -H "Content-Type: application/json" \
  -d '{
    "configPath": "examples/simple-http-workflow.yaml",
    "variables": {
      "amount": 100.00,
      "currency": "USD"
    }
  }' | python3 -m json.tool

# Cleanup
echo ""
echo "Cleaning up..."
kill $APP_PID 2>/dev/null || true
sleep 1
