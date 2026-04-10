package com.auction.server.websocket;

import com.auction.server.models.AuctionManager;
import com.auction.server.models.AuctionUpdate;
import com.auction.server.models.AuctionUpdateListener;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArraySet;

public class AuctionWebSocketServer extends WebSocketServer implements AuctionUpdateListener {

    private static final int PORT = 8081;
    private final CopyOnWriteArraySet<WebSocket> clients = new CopyOnWriteArraySet<>();

    public AuctionWebSocketServer() {
        super(new InetSocketAddress(PORT));
        // Register as listener for auction updates
        AuctionManager.getInstance().addUpdateListener(this);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        System.out.println("WebSocket connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);
        // Handle client messages if needed
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + PORT);
    }

    @Override
    public void onAuctionUpdate(AuctionUpdate update) {
        // Broadcast update to all connected clients
        String jsonUpdate = String.format(
            "{\"auctionId\":%d,\"currentHighestBid\":%.2f,\"totalBids\":%d,\"endTime\":\"%s\"}",
            update.getAuctionId(),
            update.getCurrentHighestBid(),
            update.getTotalBids(),
            update.getEndTime().toString()
        );

        for (WebSocket client : clients) {
            if (client.isOpen()) {
                client.send(jsonUpdate);
            }
        }
    }

    public void startServer() {
        this.start();
    }

    public void stopServer() {
        try {
            this.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}