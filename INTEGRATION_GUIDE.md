# YAWL Integration Guide: A2A and MCP SDKs

This guide explains how to build, test, and run YAWL with A2A (Agent-to-Agent) and MCP (Model Context Protocol) SDK integrations.

## Overview

YAWL has been extended to support:

1. **A2A (Agent-to-Agent) Protocol** - Enables YAWL to communicate with other agentic systems
2. **MCP (Model Context Protocol)** - Allows AI models to interact with YAWL workflows as tools

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      YAWL Core Engine                       │
├─────────────────────────────────────────────────────────────┤
│  Integration Layer                                          │
│  ┌─────────────────────┐  ┌──────────────────────────────┐ │
│  │  A2A Integration    │  │  MCP Integration             │ │
│  │  ┌───────────────┐  │  │  ┌────────────────────────┐ │ │
│  │  │ A2A Server    │  │  │  │ MCP Server             │ │ │
│  │  │ (Port 8080)   │  │  │  │ (Port 3000)            │ │ │
│  │  ├───────────────┤  │  │  ├────────────────────────┤ │ │
│  │  │ A2A Client    │  │  │  │ MCP Client             │ │ │
│  │  └───────────────┘  │  │  └────────────────────────┘ │ │
│  └─────────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6+ (for building with external dependencies)
- Git

### Build the Project

```bash
# Compile integration classes
javac -d classes \
  src/org/yawlfoundation/yawl/integration/a2a/*.java \
  src/org/yawlfoundation/yawl/integration/mcp/*.java

# Compile tests
javac -cp classes -d classes \
  test/org/yawlfoundation/yawl/integration/IntegrationTest.java
```

### Run Tests

```bash
# Run integration test suite
java -cp classes org.yawlfoundation.yawl.integration.IntegrationTest
```

Expected output:
```
╔════════════════════════════════════════════════════════════╗
║  YAWL Integration Test Suite                              ║
║  Testing A2A and MCP SDK Integration                      ║
╚════════════════════════════════════════════════════════════╝

=== Testing A2A Server ===
✓ A2A Server started successfully
✓ A2A Server stopped successfully

=== Testing A2A Client ===
✓ A2A Client connected successfully
✓ Capability invoked
✓ A2A Client disconnected successfully

=== Testing MCP Server ===
✓ MCP Server started successfully
✓ MCP Server stopped successfully

=== Testing MCP Client ===
✓ MCP Client connected successfully
✓ Found 4 tools
✓ Tool called
✓ Resource fetched
✓ MCP Client disconnected successfully

╔════════════════════════════════════════════════════════════╗
║  All Integration Tests Passed Successfully! ✓             ║
╚════════════════════════════════════════════════════════════╝
```

## Running Individual Components

### A2A Server

Start the A2A server to expose YAWL workflows via A2A protocol:

```bash
java -cp classes org.yawlfoundation.yawl.integration.a2a.YawlA2AServer
```

The server will start on port 8080 and accept agent requests via:
- JSON-RPC 2.0
- gRPC
- HTTP+JSON/REST

### A2A Client

Connect to external A2A agents:

```bash
java -cp classes org.yawlfoundation.yawl.integration.a2a.YawlA2AClient http://agent-url:8080
```

### MCP Server

Start the MCP server to expose YAWL workflows to AI models:

```bash
java -cp classes org.yawlfoundation.yawl.integration.mcp.YawlMcpServer
```

The server will start on port 3000 and expose:

**Tools:**
- `startWorkflow` - Start a new workflow instance
- `getWorkflowStatus` - Get workflow status
- `listWorkflows` - List available workflows
- `executeTask` - Execute a workflow task

**Resources:**
- `yawl://workflows` - Available workflow specs
- `yawl://cases` - Running workflow cases
- `yawl://tasks` - Available tasks

### MCP Client

Connect to external MCP servers:

```bash
java -cp classes org.yawlfoundation.yawl.integration.mcp.YawlMcpClient http://mcp-server:3000
```

## Integration with External SDKs

### Adding A2A Java SDK

1. Clone and build the A2A Java SDK:
```bash
git clone https://github.com/a2aproject/a2a-java.git
cd a2a-java
mvn clean install
```

2. Update `pom.xml` to uncomment A2A dependencies:
```xml
<dependency>
    <groupId>org.a2aproject</groupId>
    <artifactId>a2a-java-client</artifactId>
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>org.a2aproject</groupId>
    <artifactId>a2a-java-server</artifactId>
    <version>0.1.0</version>
</dependency>
```

3. Implement the TODOs in:
   - `src/org/yawlfoundation/yawl/integration/a2a/YawlA2AServer.java`
   - `src/org/yawlfoundation/yawl/integration/a2a/YawlA2AClient.java`

### Adding MCP Java SDK

1. Clone and build the MCP Java SDK:
```bash
git clone https://github.com/modelcontextprotocol/java-sdk.git
cd java-sdk
./mvnw clean install -DskipTests
```

2. Update `pom.xml` to uncomment MCP dependencies:
```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

3. Implement the TODOs in:
   - `src/org/yawlfoundation/yawl/integration/mcp/YawlMcpServer.java`
   - `src/org/yawlfoundation/yawl/integration/mcp/YawlMcpClient.java`

## API Examples

### A2A Server Example

```java
// Create and configure A2A server
YawlA2AServer server = new YawlA2AServer(8080);
server.start();

// Server exposes YAWL capabilities via A2A protocol
// Other agents can now invoke YAWL workflows
```

### A2A Client Example

```java
// Connect to external agent
YawlA2AClient client = new YawlA2AClient("http://agent.example.com:8080");
client.connect();

// Invoke agent capability from YAWL workflow
String result = client.invokeCapability("processDocument", documentData);

client.disconnect();
```

### MCP Server Example

```java
// Create MCP server
YawlMcpServer server = new YawlMcpServer(3000);

// Register YAWL workflow operations as MCP tools
server.registerWorkflowTools();

// Register YAWL resources
server.registerWorkflowResources();

// Start server - AI models can now use YAWL workflows
server.start();
```

### MCP Client Example

```java
// Connect to MCP server
YawlMcpClient client = new YawlMcpClient("http://mcp-server:3000");
client.connect();

// List available tools
String[] tools = client.listTools();

// Call an AI tool from YAWL workflow
String result = client.callTool("analyzeDocument", parameters);

// Access MCP resources
String data = client.getResource("mcp://data/workflows");

client.disconnect();
```

## Use Cases

### A2A Integration Use Cases

1. **Multi-Agent Workflows** - Coordinate multiple AI agents in complex workflows
2. **Service Composition** - Combine YAWL workflows with external agent services
3. **Agent Orchestration** - Use YAWL as orchestrator for agent-based systems

### MCP Integration Use Cases

1. **AI-Enhanced Workflows** - Add AI capabilities to workflow tasks
2. **Intelligent Process Automation** - Use LLMs for decision-making in workflows
3. **Context-Aware Processing** - Provide workflow context to AI models
4. **Dynamic Workflow Adaptation** - AI models can start and monitor workflows

## Configuration

### A2A Server Configuration

Default settings:
- Port: 8080
- Supported transports: JSON-RPC 2.0, gRPC, HTTP+JSON

Customize in constructor:
```java
YawlA2AServer server = new YawlA2AServer(customPort);
```

### MCP Server Configuration

Default settings:
- Port: 3000
- Supports: Tools, Resources, Reactive Streams

Customize in constructor:
```java
YawlMcpServer server = new YawlMcpServer(customPort);
```

## Troubleshooting

### Build Issues

If you encounter build errors:

1. Ensure Java 21+ is installed:
   ```bash
   java -version
   ```

2. Check Maven is available:
   ```bash
   mvn -version
   ```

3. Verify all source files are present:
   ```bash
   find src/org/yawlfoundation/yawl/integration -name "*.java"
   ```

### Runtime Issues

If servers fail to start:

1. Check if ports are available:
   ```bash
   # A2A server port
   netstat -an | grep 8080

   # MCP server port
   netstat -an | grep 3000
   ```

2. Verify classes are compiled:
   ```bash
   find classes -name "*.class"
   ```

## Next Steps

1. **Implement Full SDK Integration** - Complete the TODO sections in integration classes
2. **Add Workflow Bindings** - Connect integration to actual YAWL engine
3. **Create Custom Tools** - Define YAWL-specific MCP tools
4. **Develop Agent Capabilities** - Implement A2A agent cards for YAWL
5. **Add Security** - Implement authentication and authorization
6. **Performance Testing** - Load test with multiple agents/models

## Resources

- [A2A Project](https://github.com/a2aproject/a2a-java)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [YAWL Foundation](https://yawlfoundation.github.io)

## Contributing

Contributions welcome! Please see `CONTRIBUTING.md` for guidelines.

## License

This integration follows the YAWL license. See `license.txt` for details.
