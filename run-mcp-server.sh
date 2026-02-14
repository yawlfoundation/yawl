#!/bin/bash

# Run YAWL MCP Server

echo "Starting YAWL MCP Server..."
echo "Press Ctrl+C to stop"
echo ""

java -cp classes org.yawlfoundation.yawl.integration.mcp.YawlMcpServer
