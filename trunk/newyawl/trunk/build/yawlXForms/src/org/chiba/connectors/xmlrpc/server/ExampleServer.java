package org.chiba.connectors.xmlrpc.server;

import org.apache.xmlrpc.WebServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.chiba.connectors.xmlrpc.server.DefaultHandler;

import java.util.Hashtable;

public class ExampleServer {
    private WebServer server;

    public static void main(String[] args) {
        try {
            ExampleServer rpc = new ExampleServer();
            rpc.setServer(InetAddress.getByName("127.0.0.1"), 8088);
            rpc.addHandler("$default", new DefaultHandler());
            rpc.listen();
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: localhost");
        }
    }

    public void setServer(InetAddress address, int port) {
        server = new WebServer(port, address);
    }

    public void listen() {
        server.start();
    }

    public void addHandler(String prefix, Object o) {
        server.addHandler(prefix, o);
    }
}
