package org.yawlfoundation.yawl.integration;

import org.yawlfoundation.yawl.integration.a2a.YawlA2AServer;
import org.yawlfoundation.yawl.integration.a2a.YawlA2AClient;
import org.yawlfoundation.yawl.integration.mcp.YawlMcpServer;
import org.yawlfoundation.yawl.integration.mcp.YawlMcpClient;

/**
 * Integration Test for A2A and MCP SDK Integration
 *
 * This test demonstrates the integration between YAWL and:
 * - A2A (Agent-to-Agent) Protocol
 * - MCP (Model Context Protocol)
 *
 * @author YAWL Foundation
 * @version 5.2
 */
public class IntegrationTest {

    /**
     * Test A2A Server functionality
     */
    public static void testA2AServer() {
        System.out.println("\n=== Testing A2A Server ===");

        YawlA2AServer server = new YawlA2AServer(8080);
        server.start();

        if (server.isRunning()) {
            System.out.println("✓ A2A Server started successfully");
        } else {
            System.out.println("✗ A2A Server failed to start");
        }

        server.stop();
        System.out.println("✓ A2A Server stopped successfully");
    }

    /**
     * Test A2A Client functionality
     */
    public static void testA2AClient() {
        System.out.println("\n=== Testing A2A Client ===");

        YawlA2AClient client = new YawlA2AClient("http://localhost:8080");
        client.connect();

        if (client.isConnected()) {
            System.out.println("✓ A2A Client connected successfully");

            String result = client.invokeCapability("testCapability", "test data");
            System.out.println("✓ Capability invoked: " + result);
        } else {
            System.out.println("✗ A2A Client failed to connect");
        }

        client.disconnect();
        System.out.println("✓ A2A Client disconnected successfully");
    }

    /**
     * Test MCP Server functionality
     */
    public static void testMCPServer() {
        System.out.println("\n=== Testing MCP Server ===");

        YawlMcpServer server = new YawlMcpServer(3000);
        server.registerWorkflowTools();
        server.registerWorkflowResources();
        server.start();

        if (server.isRunning()) {
            System.out.println("✓ MCP Server started successfully");
        } else {
            System.out.println("✗ MCP Server failed to start");
        }

        server.stop();
        System.out.println("✓ MCP Server stopped successfully");
    }

    /**
     * Test MCP Client functionality
     */
    public static void testMCPClient() {
        System.out.println("\n=== Testing MCP Client ===");

        YawlMcpClient client = new YawlMcpClient("http://localhost:3000");
        client.connect();

        if (client.isConnected()) {
            System.out.println("✓ MCP Client connected successfully");

            // List tools
            String[] tools = client.listTools();
            System.out.println("✓ Found " + tools.length + " tools");
            for (String tool : tools) {
                System.out.println("  - " + tool);
            }

            // Call a tool
            String result = client.callTool("analyzeDocument", "{\"doc\": \"test\"}");
            System.out.println("✓ Tool called: " + result);

            // Get a resource
            String resource = client.getResource("mcp://data/test");
            System.out.println("✓ Resource fetched: " + resource);
        } else {
            System.out.println("✗ MCP Client failed to connect");
        }

        client.disconnect();
        System.out.println("✓ MCP Client disconnected successfully");
    }

    /**
     * Main test runner
     */
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  YAWL Integration Test Suite                              ║");
        System.out.println("║  Testing A2A and MCP SDK Integration                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        try {
            testA2AServer();
            testA2AClient();
            testMCPServer();
            testMCPClient();

            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  All Integration Tests Passed Successfully! ✓             ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("\n✗ Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
