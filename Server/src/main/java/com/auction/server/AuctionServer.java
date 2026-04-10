package com.auction.server;

import com.auction.server.websocket.AuctionWebSocketServer;

public class AuctionServer {

    private static AuctionWebSocketServer webSocketServer;

    public static void main(String[] args) {
        System.out.println("=== AUCTION SERVER STARTING ===");

        // Start WebSocket server for realtime updates
        webSocketServer = new AuctionWebSocketServer();
        webSocketServer.startServer();

        // Keep server running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        } finally {
            if (webSocketServer != null) {
                webSocketServer.stopServer();
            }
        }
    }

    public static void shutdown() {
        if (webSocketServer != null) {
            webSocketServer.stopServer();
        }
        System.exit(0);
    }
}