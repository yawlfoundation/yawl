#!/bin/bash

# Run YAWL A2A Server

echo "Starting YAWL A2A Server..."
echo "Press Ctrl+C to stop"
echo ""

java -cp classes org.yawlfoundation.yawl.integration.a2a.YawlA2AServer
