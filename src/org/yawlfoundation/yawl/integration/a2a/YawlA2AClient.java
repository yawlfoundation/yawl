package org.yawlfoundation.yawl.integration.a2a;

/**
 * A2A Client Integration for YAWL
 *
 * This class demonstrates how YAWL can connect to other agents using the A2A protocol.
 * Enables YAWL workflows to invoke capabilities from external A2A-compliant agents.
 *
 * Example Usage:
 *
 * YawlA2AClient client = new YawlA2AClient("http://external-agent.example.com:8080");
 * client.connect();
 * String result = client.invokeCapability("processDocument", documentData);
 *
 * @author YAWL Foundation
 * @version 5.2
 */
public class YawlA2AClient {

    private String agentUrl;
    private boolean connected = false;

    /**
     * Constructor for YAWL A2A Client
     * @param agentUrl URL of the remote A2A agent
     */
    public YawlA2AClient(String agentUrl) {
        this.agentUrl = agentUrl;
        System.out.println("Initializing YAWL A2A Client for agent at: " + agentUrl);
    }

    /**
     * Connect to the remote A2A agent
     *
     * When A2A SDK is available, this would:
     * 1. Fetch the agent's AgentCard to discover capabilities
     * 2. Configure the transport (JSON-RPC, gRPC, etc.)
     * 3. Establish the connection
     */
    public void connect() {
        if (connected) {
            System.out.println("Already connected to agent");
            return;
        }

        System.out.println("Connecting to A2A agent at: " + agentUrl);

        // TODO: When A2A SDK is available, implement:
        // AgentCard card = fetchAgentCard(agentUrl);
        // A2AClient client = new A2AClientBuilder()
        //     .withAgentUrl(agentUrl)
        //     .withJsonRpcTransport()
        //     .build();
        // client.connect();

        connected = true;
        System.out.println("Successfully connected to A2A agent");
    }

    /**
     * Disconnect from the remote agent
     */
    public void disconnect() {
        if (!connected) {
            System.out.println("Not connected to any agent");
            return;
        }

        System.out.println("Disconnecting from A2A agent...");
        connected = false;
        System.out.println("Disconnected");
    }

    /**
     * Invoke a capability on the remote agent
     * @param capabilityName name of the capability to invoke
     * @param data data to send to the capability
     * @return result from the capability
     */
    public String invokeCapability(String capabilityName, String data) {
        if (!connected) {
            throw new IllegalStateException("Not connected to agent");
        }

        System.out.println("Invoking capability: " + capabilityName);

        // TODO: When A2A SDK is available, implement:
        // Request request = new Request.Builder()
        //     .capability(capabilityName)
        //     .data(data)
        //     .build();
        // Response response = client.execute(request);
        // return response.getResult();

        return "Mock result from " + capabilityName;
    }

    /**
     * Check if connected to agent
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        String agentUrl = args.length > 0 ? args[0] : "http://localhost:8080";

        YawlA2AClient client = new YawlA2AClient(agentUrl);
        client.connect();

        // Example: Invoke a capability
        String result = client.invokeCapability("testCapability", "sample data");
        System.out.println("Result: " + result);

        client.disconnect();
    }
}
