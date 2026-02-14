#!/bin/bash

# YAWL Build Script with A2A and MCP Integration
# Compiles all Java sources and runs tests

set -e  # Exit on error

echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Building YAWL with A2A and MCP Integration               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Create classes directory if it doesn't exist
mkdir -p classes

# Step 1: Compile integration classes
echo "→ Compiling integration classes..."
javac -d classes \
  src/org/yawlfoundation/yawl/integration/a2a/*.java \
  src/org/yawlfoundation/yawl/integration/mcp/*.java

echo "✓ Integration classes compiled successfully"
echo ""

# Step 2: Compile test classes
echo "→ Compiling test classes..."
javac -cp classes -d classes \
  test/org/yawlfoundation/yawl/integration/IntegrationTest.java

echo "✓ Test classes compiled successfully"
echo ""

# Step 3: Run tests
echo "→ Running integration tests..."
echo ""
java -cp classes org.yawlfoundation.yawl.integration.IntegrationTest

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║  Build Completed Successfully! ✓                          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Available commands:"
echo "  ./run-a2a-server.sh    - Start A2A server on port 8080"
echo "  ./run-mcp-server.sh    - Start MCP server on port 3000"
echo "  ./run-tests.sh         - Run integration tests"
echo ""
