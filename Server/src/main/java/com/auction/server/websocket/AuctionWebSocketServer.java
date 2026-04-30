package com.auction.server.websocket;

import com.auction.server.models.AuctionManager;
import com.auction.server.models.AuctionUpdate;
import com.auction.server.models.AuctionUpdateListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.auction.server.utils.LocalDateTimeAdapter;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArraySet;

public class AuctionWebSocketServer extends WebSocketServer implements AuctionUpdateListener {

    private static final int PORT = 8081;
    private final CopyOnWriteArraySet<WebSocket> clients = new CopyOnWriteArraySet<>();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public AuctionWebSocketServer() {
        super(new InetSocketAddress(PORT));
        // Đăng ký làm "người lắng nghe" cho AuctionManager
        AuctionManager.getInstance().addUpdateListener(this);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        System.out.println("✓ [WEBSOCKET] New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        System.out.println("✗ [WEBSOCKET] Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Hiện tại không xử lý message từ client
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("✓ [WEBSOCKET] Server started on port " + PORT);
    }

    /**
     * Đây là phương thức được AuctionManager gọi khi có sự kiện mới.
     * Vai trò của nó là biến sự kiện đó thành JSON và phát đi.
     */
    @Override
    public void onAuctionUpdate(AuctionUpdate update) {
        String jsonUpdate = gson.toJson(update);
        System.out.println("Broadcasting update: " + jsonUpdate);
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