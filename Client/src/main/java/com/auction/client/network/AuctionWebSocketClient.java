package com.auction.client.network;

import com.auction.client.service.BidUpdateListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client WebSocket chuyên dụng cho phòng đấu giá (BiddingRoom).
 *
 * <p><b>Luồng hoạt động:</b>
 * <ol>
 *   <li>BiddingRoomController gọi {@link #connect(int, BidUpdateListener)} khi vào phòng.</li>
 *   <li>Server broadcast JSON → client parse → gọi callback trên JavaFX thread.</li>
 *   <li>BiddingRoomController gọi {@link #disconnect()} khi rời phòng.</li>
 * </ol>
 *
 * <p><b>Định dạng JSON server gửi về:</b>
 * <pre>
 * // NEW_BID (từ AuctionManager.broadcastUpdate)
 * {"type":"NEW_BID","data":{"auctionId":1,"newPrice":500000,"bidderUsername":"user"},"timestamp":"..."}
 *
 * // PRICE_UPDATE (từ BidService.processBid → broadcastPriceUpdate)
 * {"type":"PRICE_UPDATE","auctionId":1,"newPrice":500000.00}
 *
 * // AUCTION_ENDED
 * {"type":"AUCTION_ENDED","data":{"auctionId":1,"status":"FINISHED",...},"timestamp":"..."}
 * </pre>
 */
public class AuctionWebSocketClient {

  private static final String WS_URL = "ws://localhost:8081";

  // Delay và số lần thử lại kết nối khi mất mạng
  private static final long RECONNECT_DELAY_SECONDS = 5;
  private static final int MAX_RECONNECT_ATTEMPTS = 3;

  private WebSocketClient wsClient;
  private BidUpdateListener listener;
  private int watchedAuctionId = -1;

  private final AtomicBoolean intentionalClose = new AtomicBoolean(false);
  private final ScheduledExecutorService reconnectScheduler =
    Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "ws-reconnect-thread");
      t.setDaemon(true);
      return t;
    });
  private ScheduledFuture<?> reconnectTask;
  private int reconnectAttempts = 0;

  // ===================================================================
  // PUBLIC API
  // ===================================================================

  /**
   * Kết nối tới WebSocket server và đăng ký lắng nghe phiên đấu giá.
   *
   * @param auctionId ID phiên cần theo dõi (dùng để lọc event không liên quan)
   * @param listener  Callback nhận sự kiện realtime
   */
  public void connect(int auctionId, BidUpdateListener listener) {
    this.watchedAuctionId = auctionId;
    this.listener = listener;
    this.intentionalClose.set(false);
    this.reconnectAttempts = 0;
    createAndConnect();
  }

  /**
   * Ngắt kết nối chủ động (gọi khi rời phòng đấu giá).
   */
  public void disconnect() {
    intentionalClose.set(true);
    cancelReconnect();
    if (wsClient != null) {
      wsClient.close();
      wsClient = null;
    }
    reconnectScheduler.shutdownNow();
  }

  /** Kiểm tra kết nối còn mở không. */
  public boolean isConnected() {
    return wsClient != null && wsClient.isOpen();
  }

  // ===================================================================
  // INTERNAL — tạo và duy trì kết nối
  // ===================================================================

  private void createAndConnect() {
    try {
      wsClient = new WebSocketClient(new URI(WS_URL)) {

        @Override
        public void onOpen(ServerHandshake handshake) {
          reconnectAttempts = 0;
          System.out.println("[WS] Kết nối thành công tới " + WS_URL);
          if (listener != null) {
            Platform.runLater(() -> listener.onConnected());
          }
        }

        @Override
        public void onMessage(String message) {
          handleServerMessage(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
          System.out.println("[WS] Đóng kết nối (code=" + code + ", reason=" + reason + ")");
          if (listener != null) {
            Platform.runLater(() -> listener.onDisconnected(reason));
          }
          // Chỉ thử kết nối lại nếu không phải đóng chủ động
          if (!intentionalClose.get()) {
            scheduleReconnect();
          }
        }

        @Override
        public void onError(Exception ex) {
          System.err.println("[WS] Lỗi: " + ex.getMessage());
        }
      };

      wsClient.connect();

    } catch (URISyntaxException e) {
      System.err.println("[WS] URI không hợp lệ: " + e.getMessage());
    }
  }

  private void scheduleReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      System.err.println("[WS] Đã thử " + MAX_RECONNECT_ATTEMPTS + " lần, dừng kết nối lại.");
      return;
    }
    cancelReconnect();
    reconnectAttempts++;
    System.out.println("[WS] Sẽ thử kết nối lại lần " + reconnectAttempts
      + " sau " + RECONNECT_DELAY_SECONDS + "s...");
    reconnectTask = reconnectScheduler.schedule(() -> {
      if (!intentionalClose.get()) {
        createAndConnect();
      }
    }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
  }

  private void cancelReconnect() {
    if (reconnectTask != null && !reconnectTask.isDone()) {
      reconnectTask.cancel(false);
    }
  }

  // ===================================================================
  // PARSE JSON — xử lý từng loại message
  // ===================================================================

  private void handleServerMessage(String rawJson) {
    try {
      JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
      String type = root.has("type") ? root.get("type").getAsString() : "";

      switch (type) {
        case "NEW_BID":
          handleNewBid(root);
          break;

        case "PRICE_UPDATE":
          // Format đơn giản từ broadcastPriceUpdate(): {"type":"PRICE_UPDATE","auctionId":X,"newPrice":Y}
          handlePriceUpdate(root);
          break;

        case "AUCTION_ENDED":
          handleAuctionEnded(root);
          break;

        default:
          // Bỏ qua các loại không liên quan (NEW_ITEM, ITEM_UPDATED...)
          break;
      }

    } catch (Exception e) {
      System.err.println("[WS] Lỗi parse JSON: " + e.getMessage() + " | raw=" + rawJson);
    }
  }

  /**
   * Xử lý sự kiện NEW_BID.
   * Server gửi: {"type":"NEW_BID","data":{"auctionId":1,"newPrice":500000,"bidderUsername":"user"}}
   */
  private void handleNewBid(JsonObject root) {
    if (!root.has("data") || root.get("data").isJsonNull()) return;

    JsonObject data = root.getAsJsonObject("data");
    int auctionId   = getInt(data, "auctionId", -1);
    double newPrice = getDouble(data, "newPrice", 0);
    String username = getString(data, "bidderUsername", "Ẩn danh");

    // Chỉ xử lý nếu đúng phiên đang xem
    if (watchedAuctionId == -1 || auctionId == watchedAuctionId) {
      notifyNewBid(auctionId, newPrice, username);
    }
  }

  /**
   * Xử lý sự kiện PRICE_UPDATE (format gọn từ broadcastPriceUpdate).
   * Server gửi: {"type":"PRICE_UPDATE","auctionId":1,"newPrice":500000}
   */
  private void handlePriceUpdate(JsonObject root) {
    int auctionId   = getInt(root, "auctionId", -1);
    double newPrice = getDouble(root, "newPrice", 0);

    if (watchedAuctionId == -1 || auctionId == watchedAuctionId) {
      notifyNewBid(auctionId, newPrice, null);
    }
  }

  /**
   * Xử lý sự kiện AUCTION_ENDED.
   * Server gửi: {"type":"AUCTION_ENDED","data":{...AuctionDataDTO...}}
   */
  private void handleAuctionEnded(JsonObject root) {
    if (!root.has("data") || root.get("data").isJsonNull()) return;

    JsonObject data   = root.getAsJsonObject("data");
    int auctionId     = getInt(data, "auctionId", -1);
    // AuctionDataDTO dùng field "auction_id" (SerializedName)
    if (auctionId == -1) auctionId = getInt(data, "auction_id", -1);

    String status = "FINISHED";
    if (data.has("status")) {
      JsonElement statusEl = data.get("status");
      status = statusEl.isJsonNull() ? "FINISHED" : statusEl.getAsString();
    }

    if (watchedAuctionId == -1 || auctionId == watchedAuctionId) {
      final int finalAuctionId = auctionId;
      final String finalStatus = status;
      if (listener != null) {
        Platform.runLater(() -> listener.onAuctionEnded(finalAuctionId, finalStatus));
      }
    }
  }

  private void notifyNewBid(int auctionId, double price, String username) {
    if (listener != null) {
      Platform.runLater(() -> listener.onNewBid(auctionId, price, username));
    }
  }

  // ===================================================================
  // HELPER — an toàn khi đọc JSON (tránh NPE)
  // ===================================================================

  private int getInt(JsonObject obj, String key, int defaultVal) {
    try {
      return (obj.has(key) && !obj.get(key).isJsonNull())
        ? obj.get(key).getAsInt() : defaultVal;
    } catch (Exception e) { return defaultVal; }
  }

  private double getDouble(JsonObject obj, String key, double defaultVal) {
    try {
      return (obj.has(key) && !obj.get(key).isJsonNull())
        ? obj.get(key).getAsDouble() : defaultVal;
    } catch (Exception e) { return defaultVal; }
  }

  private String getString(JsonObject obj, String key, String defaultVal) {
    try {
      return (obj.has(key) && !obj.get(key).isJsonNull())
        ? obj.get(key).getAsString() : defaultVal;
    } catch (Exception e) { return defaultVal; }
  }
}