#!/bin/bash

# Test the Simple HTTP Workflow

set -e

echo "🚀 Testing Simple HTTP Workflow"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
APP_HOST="localhost"
APP_PORT="${1:-8080}"
APP_URL="http://$APP_HOST:$APP_PORT"
WORKFLOW_CONFIG="examples/simple-http-workflow.yaml"

echo -e "${BLUE}Configuration:${NC}"
echo "  App URL: $APP_URL"
echo "  Workflow: $WORKFLOW_CONFIG"
echo ""

# Step 1: Check if app is running
echo -e "${YELLOW}1️⃣  Checking if app is running...${NC}"
if ! curl -s "$APP_URL/api/orchestration/health" > /dev/null 2>&1; then
  echo -e "${YELLOW}❌ App not running on port $APP_PORT${NC}"
  echo ""
  echo -e "${BLUE}Start the app in another terminal:${NC}"
  echo "  ./start.sh --port $APP_PORT"
  echo ""
  exit 1
fi
echo -e "${GREEN}✅ App is running${NC}"
echo ""

# Step 2: Get health status
echo -e "${YELLOW}2️⃣  Checking health status...${NC}"
HEALTH=$(curl -s "$APP_URL/api/orchestration/health")
echo "   Response: $HEALTH"
echo -e "${GREEN}✅ Health check passed${NC}"
echo ""

# Step 3: Execute workflow
echo -e "${YELLOW}3️⃣  Executing workflow...${NC}"
echo "   Request:"
echo '   {
     "configPath": "'$WORKFLOW_CONFIG'",
     "variables": {
       "amount": 100,
       "currency": "USD"
     }
   }'
echo ""

RESPONSE=$(curl -s -X POST "$APP_URL/api/orchestration/execute/sync" \
  -H "Content-Type: application/json" \
  -d '{
    "configPath": "'$WORKFLOW_CONFIG'",
    "variables": {
      "amount": 100,
      "currency": "USD"
    }
  }')

echo "   Response:"
if command -v jq &> /dev/null; then
  echo "$RESPONSE" | jq . | sed 's/^/     /'
else
  echo "$RESPONSE" | python3 -m json.tool 2>/dev/null | sed 's/^/     /' || echo "$RESPONSE" | sed 's/^/     /'
fi
echo ""

# Step 4: Check if workflow completed successfully
if echo "$RESPONSE" | grep -q '"status":"COMPLETED"'; then
  echo -e "${GREEN}✅ Workflow executed successfully!${NC}"
elif echo "$RESPONSE" | grep -q '"status":"FAILED"'; then
  echo -e "${YELLOW}⚠️  Workflow completed but with errors${NC}"
else
  echo -e "${YELLOW}ℹ️  Workflow status check incomplete - see full response above${NC}"
fi
echo ""

# Step 5: Test with different amounts
echo -e "${YELLOW}4️⃣  Testing with different amounts...${NC}"
for AMOUNT in 50 250 1000; do
  echo "   Amount: $AMOUNT"
  RESPONSE=$(curl -s -X POST "$APP_URL/api/orchestration/execute/sync" \
    -H "Content-Type: application/json" \
    -d '{
      "configPath": "'$WORKFLOW_CONFIG'",
      "variables": {
        "amount": '$AMOUNT',
        "currency": "USD"
      }
    }')
  
  if echo "$RESPONSE" | grep -q '"status":"COMPLETED"'; then
    echo -e "   ${GREEN}✅${NC}"
  else
    echo -e "   ${YELLOW}⚠️${NC}"
  fi
done
echo ""

echo -e "${GREEN}🎉 Testing complete!${NC}"
echo ""
echo -e "${BLUE}Summary:${NC}"
echo "  • Workflow: $WORKFLOW_CONFIG"
echo "  • Endpoint: $APP_URL/api/orchestration/execute/sync"
echo "  • Method: POST"
echo "  • Protocol: HTTP"
echo ""
echo -e "${BLUE}Next steps:${NC}"
echo "  • Try other workflows in examples/"
echo "  • Start Kafka: docker-compose up -d kafka"
echo "  • Test payment-workflow.yaml with Kafka enabled"
echo ""
