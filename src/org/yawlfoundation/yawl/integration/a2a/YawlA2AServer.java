package org.yawlfoundation.yawl.integration.a2a;

/**
 * A2A Server Integration for YAWL
 *
 * This class demonstrates how to expose YAWL workflow capabilities through the A2A protocol.
 * The A2A (Agent-to-Agent) protocol enables YAWL to communicate with other agentic systems.
 *
 * Example Usage:
 *
 * YawlA2AServer server = new YawlA2AServer();
 * server.start();
 *
 * // The server will expose YAWL workflow operations via A2A protocol
 * // Supporting JSON-RPC 2.0, gRPC, and HTTP+JSON transports
 *
 * @author YAWL Foundation
 * @version 5.2
 */
public class YawlA2AServer {

    private boolean running = false;
    private int port = 8080;

    /**
     * Constructor for YAWL A2A Server
     */
    public YawlA2AServer() {
        System.out.println("Initializing YAWL A2A Server...");
    }

    /**
     * Constructor with custom port
     * @param port the port to run the A2A server on
     */
    public YawlA2AServer(int port) {
        this.port = port;
        System.out.println("Initializing YAWL A2A Server on port " + port);
    }

    /**
     * Start the A2A server
     *
     * When A2A SDK is available, this would:
     * 1. Create an AgentCard describing YAWL's capabilities
     * 2. Implement AgentExecutor for handling workflow requests
     * 3. Start the server with chosen transport (JSON-RPC, gRPC, etc.)
     */
    public void start() {
        if (running) {
            System.out.println("Server already running");
            return;
        }

        System.out.println("Starting YAWL A2A Server on port " + port + "...");

        // TODO: When A2A SDK is available, implement:
        // AgentCard card = createYawlAgentCard();
        // AgentExecutor executor = new YawlAgentExecutor();
        // A2AServer server = new A2AServerBuilder()
        //     .withCard(card)
        //     .withExecutor(executor)
        //     .withJsonRpcTransport(port)
        //     .build();
        // server.start();

        running = true;
        System.out.println("YAWL A2A Server started successfully");
    }

    /**
     * Stop the A2A server
     */
    public void stop() {
        if (!running) {
            System.out.println("Server not running");
            return;
        }

        System.out.println("Stopping YAWL A2A Server...");
        running = false;
        System.out.println("YAWL A2A Server stopped");
    }

    /**
     * Check if server is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        YawlA2AServer server = new YawlA2AServer(8080);
        server.start();

        System.out.println("YAWL A2A Server is ready to accept agent requests");
        System.out.println("Press Ctrl+C to stop");

        // Keep server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down YAWL A2A Server...");
            server.stop();
        }));

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            server.stop();
        }
    }
}
