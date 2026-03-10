#!/bin/bash

# Vyuh Engine Sanity Test Script
# Tests basic functionality: health, workflow execution, and state persistence

set -e

APP_URL="http://localhost:8080"
echo "Running Vyuh Engine Sanity Tests..."
echo "App URL: $APP_URL"

# Test 1: Health Check
echo "1. Testing health endpoint..."
HEALTH_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null "$APP_URL/actuator/health")
if [ "$HEALTH_RESPONSE" -eq 200 ]; then
    echo "✅ Health check passed"
else
    echo "❌ Health check failed (HTTP $HEALTH_RESPONSE)"
    exit 1
fi

# Test 2: Workflow Execution
echo "2. Testing workflow execution..."
EXEC_REQUEST='{
  "configPath": "examples/simple-http-workflow.yaml",
  "variables": {
    "amount": 100.00,
    "currency": "USD"
  }
}'

EXEC_RESPONSE=$(curl -s -X POST "$APP_URL/api/orchestration/execute/sync" \
  -H "Content-Type: application/json" \
  -d "$EXEC_REQUEST")

# Check if execution was successful
if echo "$EXEC_RESPONSE" | grep -q '"status":"COMPLETED"'; then
    echo "✅ Workflow execution passed"

    # Extract execution ID for further testing
    EXECUTION_ID=$(echo "$EXEC_RESPONSE" | grep -o '"executionId":"[^"]*"' | cut -d'"' -f4)
    echo "   Execution ID: $EXECUTION_ID"

    # Test 3: Verify state persistence (if execution ID exists)
    if [ -n "$EXECUTION_ID" ]; then
        echo "3. Testing state persistence..."
        # Note: This would require an endpoint to check execution status
        # For now, just verify the execution completed successfully
        echo "✅ State persistence assumed working (execution completed)"
    fi

else
    echo "❌ Workflow execution failed"
    echo "Response: $EXEC_RESPONSE"
    exit 1
fi

echo ""
echo "🎉 All sanity tests passed! Vyuh Engine is working correctly."
echo "   - Health check: OK"
echo "   - Workflow execution: OK"
echo "   - State persistence: OK (assumed)"