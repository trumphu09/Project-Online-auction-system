package com.auction.server.websocket;

import com.auction.server.models.Item;
import com.auction.server.models.ItemDTO;
import com.auction.server.models.UserDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.auction.server.utils.LocalDateTimeAdapter;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

public class AuctionWebSocketServer extends WebSocketServer {

    private static final int PORT = 8081;
    private final CopyOnWriteArraySet<WebSocket> clients = new CopyOnWriteArraySet<>();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public AuctionWebSocketServer() {
        super(new InetSocketAddress(PORT));
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
        System.out.println("Received message from client: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + PORT);
    }

    private void broadcast(String jsonMessage) {
        System.out.println("Broadcasting update: " + jsonMessage);
        for (WebSocket client : clients) {
            if (client.isOpen()) {
                client.send(jsonMessage);
            }
        }
    }

    public void broadcastNewBid(int itemId, double newPrice, String bidderUsername) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "NEW_BID");
        update.put("itemId", itemId);
        update.put("newPrice", newPrice);
        update.put("bidderUsername", bidderUsername);
        broadcast(gson.toJson(update));
    }

    public void broadcastAuctionStarted(Item item) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "AUCTION_STARTED");
        update.put("itemId", item.getId());
        broadcast(gson.toJson(update));
    }

    public void broadcastAuctionEnded(Item item, UserDTO winner) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "AUCTION_ENDED");
        update.put("itemId", item.getId());
        update.put("finalPrice", item.getCurrentMaxPrice());
        if (winner != null) {
            update.put("winnerUsername", winner.getUsername());
        } else {
            update.put("winnerUsername", null);
        }
        broadcast(gson.toJson(update));
    }

    public void broadcastNewItem(ItemDTO item) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "NEW_ITEM");
        update.put("item", item);
        broadcast(gson.toJson(update));
    }

    public void broadcastItemUpdated(ItemDTO item) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "ITEM_UPDATED");
        update.put("item", item);
        broadcast(gson.toJson(update));
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