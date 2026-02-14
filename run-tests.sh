#!/bin/bash

# Run YAWL Integration Tests

echo "Running YAWL Integration Tests..."
echo ""

java -cp classes org.yawlfoundation.yawl.integration.IntegrationTest
