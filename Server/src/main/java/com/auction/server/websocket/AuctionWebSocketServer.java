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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AuctionWebSocketServer extends WebSocketServer implements AuctionUpdateListener {
    
    private static final int PORT = 8081;
    private final CopyOnWriteArraySet<WebSocket> clients = new CopyOnWriteArraySet<>();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
            
    // THÊM: Một luồng chuyên trách việc gửi tin nhắn ra ngoài mạng
    private final ExecutorService broadcastPool = Executors.newSingleThreadExecutor();

    public AuctionWebSocketServer() {
        super(new InetSocketAddress(PORT));
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
        System.err.println("✗ [WEBSOCKET] Lỗi: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("✓ [WEBSOCKET] Server started on port " + PORT);
    }

    @Override
    public void onAuctionUpdate(AuctionUpdate update) {
        // Chuyển object sang JSON ở luồng hiện tại cho nhanh
        String jsonUpdate = gson.toJson(update);
        
        // THÊM: Quăng việc gửi tin nhắn cho luồng broadcast xử lý, không bắt Timer đứng đợi
        broadcastPool.submit(() -> {
            System.out.println("Broadcasting update: " + jsonUpdate);
            for (WebSocket client : clients) {
                if (client.isOpen()) {
                    try {
                        client.send(jsonUpdate);
                    } catch (Exception e) {
                        System.err.println("Lỗi khi gửi tới client: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void startServer() {
        this.start();
    }

    public void stopServer() {
        try {
            // Nhớ đóng cả Thread Pool khi tắt Server nhé
            if (!broadcastPool.isShutdown()) {
                broadcastPool.shutdown();
            }
            this.stop();
        } catch (InterruptedException e) {
            System.err.println("Lỗi khi đóng WebSocket Server: " + e.getMessage());
        }
    }

    // Hàm này dùng để bắn thông báo giá mới cho toàn bộ Client đang xem Live
    public void broadcastPriceUpdate(int auctionId, double newPrice) {
        String message = String.format("{\"type\": \"PRICE_UPDATE\", \"auctionId\": %d, \"newPrice\": %.2f}", auctionId, newPrice);
        
        // Duyệt qua tập hợp clients của ông
        for (org.java_websocket.WebSocket client : clients) { 
            if (client != null && client.isOpen()) {
                try {
                    client.send(message); // Thư viện này gửi tin nhắn cực kỳ ngắn gọn
                } catch (Exception e) {
                    System.err.println("Lỗi gửi WebSocket tới 1 client: " + e.getMessage());
                }
            }
        }
    }
}