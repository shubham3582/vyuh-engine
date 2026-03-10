#!/bin/bash

# Vyuh Engine Build Script
# Builds the application and Docker image

set -e

echo "Building Vyuh Engine..."

# Build the Maven project
echo "Building JAR with Maven..."
cd "$(dirname "$0")"
mvn clean package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t vyuh-engine:latest .

echo "Build complete! Image: vyuh-engine:latest"

# Optional: Push to registry (uncomment and set REGISTRY)
# REGISTRY="your-registry.com"
# docker tag vyuh-engine:latest $REGISTRY/vyuh-engine:latest
# docker push $REGISTRY/vyuh-engine:latest
# echo "Pushed to $REGISTRY/vyuh-engine:latest"