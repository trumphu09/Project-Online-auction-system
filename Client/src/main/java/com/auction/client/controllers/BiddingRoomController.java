package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.auction.client.network.AuctionWebSocketClient;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.auction.client.service.BidUpdateListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import com.google.gson.JsonObject;

/**
 * Controller phòng đấu giá — hỗ trợ cập nhật realtime qua WebSocket.
 *
 * <p><b>Luồng chính:</b>
 * <ol>
 *   <li>{@code ProductViewController} gọi {@link #setData(ItemDTO)} sau khi load FXML.</li>
 *   <li>{@code setData()} điền thông tin sản phẩm và gọi {@link #connectWebSocket()}.</li>
 *   <li>Khi server broadcast giá mới → {@link #onNewBid} cập nhật UI tức thì.</li>
 *   <li>Khi phiên kết thúc → {@link #onAuctionEnded} khoá nút đặt giá.</li>
 *   <li>Countdown timer tự đếm từ endTime về 0, cập nhật {@code lblTimer} mỗi giây.</li>
 *   <li>Khi rời phòng → {@link #cleanup()} ngắt WebSocket và dừng timer.</li>
 * </ol>
 */
public class BiddingRoomController extends BaseController implements BidUpdateListener {

  // ─── FXML nodes ──────────────────────────────────────────────────────────
  @FXML private ImageView imgProduct;
  @FXML private Label lblDescription, lblSeller, lblStartPrice, lblStepPrice;
  @FXML private Label lblStartTime, lblEndTime;
  @FXML private Label lblTimer, lblCurrentPrice, lblHighestBidder;
  @FXML private Label lblCurrentUser, lblUserBalance, lblCategory;
  @FXML private LineChart<String, Number> chartHistory;
  @FXML private TextField txtBidAmount;
  @FXML private Button btnBid;
  @FXML private Label lblSellerRating, lblSellerSaleCount, lblBidNote;
  // Thêm 2 biến này vào danh sách khai báo FXML nodes ở đầu file:
  @FXML private Button btnPay;
  private UserDTO currentUser; // Lưu thông tin người dùng hiện tại

  // ─── Hằng số ─────────────────────────────────────────────────────────────
  private static final String SERVER_BASE_URL = "http://localhost:8080/api";
  private static final DateTimeFormatter DT_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter DISPLAY_FORMATTER =
    DateTimeFormatter.ofPattern("HH:mm:ss");

  // ─── Trạng thái ──────────────────────────────────────────────────────────
  private ItemDTO currentItem;
  private double displayedCurrentPrice = 0;
  private boolean auctionEnded = false;

  /** Đếm số lần nhận giá mới — dùng làm nhãn trục X cho biểu đồ. */
  private final AtomicInteger bidCount = new AtomicInteger(0);

  // ─── WebSocket ───────────────────────────────────────────────────────────
  private final AuctionWebSocketClient wsClient = new AuctionWebSocketClient();

  // ─── Countdown timer ─────────────────────────────────────────────────────
  private ScheduledExecutorService timerScheduler;
  private ScheduledFuture<?> timerTask;
  private LocalDateTime auctionEndTime;

  // ─── Series biểu đồ ──────────────────────────────────────────────────────
  private XYChart.Series<String, Number> chartSeries;

  // =========================================================================
  // KHỞI TẠO
  // =========================================================================

  @FXML
  public void initialize() {
    try {
      // Biểu đồ lịch sử giá
      if (chartHistory != null) {
        chartSeries = new XYChart.Series<>();
        chartSeries.setName("Lịch sử giá");
        chartHistory.getData().add(chartSeries);
        chartHistory.setAnimated(false); // Tắt animation để update nhanh
      }

      // Đồng hồ đếm ngược: style đỏ to
      if (lblTimer != null) {
        lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: red; -fx-font-weight: bold;");
        lblTimer.setText("--:--:--");
      }

      loadCurrentUserInfo();

    } catch (Exception e) {
      System.err.println("[BiddingRoom] Lỗi initialize(): " + e.getMessage());
      e.printStackTrace();
    }
  }

  // =========================================================================
  // NHẬN DỮ LIỆU SẢN PHẨM (gọi từ ProductViewController)
  // =========================================================================

  /**
   * Điền thông tin sản phẩm lên UI và kết nối WebSocket.
   * Phải gọi sau khi FXML đã được load xong.
   */
  public void setData(ItemDTO item) {
    if (item == null) {
      showAlert("Lỗi", "Không có dữ liệu sản phẩm để hiển thị!");
      return;
    }

    this.currentItem = item;

    // ── Thông tin cơ bản ──────────────────────────────────────────────
    setText(lblDescription, safeText(item.getDescription(), "Không có mô tả"));
    setText(lblStartPrice,  formatVnd(item.getStartingPrice()));
    setText(lblStepPrice,   formatVnd(item.getPriceStep()));
    setText(lblStartTime,   "Bắt đầu: " + safeText(item.getStartTime(), "N/A"));
    setText(lblEndTime,     "Kết thúc: " + safeText(item.getEndTime(), "N/A"));
    setText(lblCategory,    "Loại: " + safeText(item.getCategory(), "N/A"));
    // ── Người bán ─────────────────────────────────────────────
    int sellerId = item.getSellerId();

    AuctionFacade.getInstance().getUserById(
            sellerId,
            new ApiCallback<UserDTO>() {

        @Override
        public void onSuccess(UserDTO seller) {

            Platform.runLater(() -> {

                if (seller != null) {

                    String sellerName =
                            seller.getFullName() != null &&
                            !seller.getFullName().isBlank()
                                    ? seller.getFullName()
                                    : seller.getUsername();

                    setText(lblSeller, "Người bán: " + sellerName);

                    // rating
                    if (lblSellerRating != null) {
                        lblSellerRating.setText(
                                String.format("⭐ %.1f", seller.getTotalRating())
                        );
                    }

                    // sale count
                    if (lblSellerSaleCount != null) {
                        lblSellerSaleCount.setText(
                                "Đã bán: " + seller.getSaleCount()
                        );
                    }

                } else {

                    setText(lblSeller, "Người bán: Không xác định");
                }
            });
        }

        @Override
        public void onError(String errorMessage) {

            Platform.runLater(() -> {

                setText(lblSeller, "Người bán: Không tải được");

                if (lblSellerRating != null) {
                    lblSellerRating.setText("");
                }

                if (lblSellerSaleCount != null) {
                    lblSellerSaleCount.setText("");
                }

                System.out.println("Lỗi load seller: " + errorMessage);
            });
        }
    });

    
    // ── Giá hiện tại ─────────────────────────────────────────────────
    displayedCurrentPrice = item.getCurrentMaxPrice() > 0
      ? item.getCurrentMaxPrice()
      : item.getStartingPrice();
    setText(lblCurrentPrice, formatVnd(displayedCurrentPrice));

    if (item.getHighestBidderId() > 0) {
      setText(lblHighestBidder, "Bidder #" + item.getHighestBidderId());
    } else {
      setText(lblHighestBidder, "Chưa có người ra giá");
    }

    // ── Điểm khởi đầu cho biểu đồ ────────────────────────────────────
    addChartPoint("Khởi điểm", displayedCurrentPrice);

    // ── Hình ảnh ─────────────────────────────────────────────────────
    loadProductImage(item.getImagePath());

    // ── Countdown timer ───────────────────────────────────────────────
    parseAndStartTimer(item.getEndTime());

    // ── Kết nối WebSocket ─────────────────────────────────────────────
    connectWebSocket();
  }

  // =========================================================================
  // WEBSOCKET — kết nối / ngắt
  // =========================================================================

  private void connectWebSocket() {
    if (currentItem == null || currentItem.getAuctionId() <= 0) {
      System.err.println("[BiddingRoom] Không thể kết nối WS: auctionId không hợp lệ.");
      return;
    }
    wsClient.connect(currentItem.getAuctionId(), this);
  }

  // =========================================================================
  // BidUpdateListener — nhận sự kiện từ WebSocket
  // =========================================================================

  @Override
  public void onConnected() {
    // Chạy trên JavaFX thread (Platform.runLater đã làm trong AuctionWebSocketClient)
    System.out.println("[BiddingRoom] WebSocket đã kết nối!");
    if (lblBidNote != null) {
      lblBidNote.setText("🟢 Kết nối realtime — Giá phải ≥ Giá hiện tại + Bước giá");
      lblBidNote.setStyle("-fx-font-size: 11; -fx-text-fill: #27ae60; -fx-font-style: italic;");
    }
  }

  @Override
  public void onDisconnected(String reason) {
    System.out.println("[BiddingRoom] WebSocket ngắt: " + reason);
    if (lblBidNote != null && !auctionEnded) {
      lblBidNote.setText("🔴 Mất kết nối realtime — Đang thử lại...");
      lblBidNote.setStyle("-fx-font-size: 11; -fx-text-fill: #e74c3c; -fx-font-style: italic;");
    }
  }

  /**
   * Nhận giá mới broadcast từ server — cập nhật UI tức thì.
   * Đã chạy trên JavaFX Application Thread.
   */
  @Override
  public void onNewBid(int auctionId, double newPrice, String bidderUsername) {
    if (newPrice <= displayedCurrentPrice) return; // Bỏ qua nếu giá cũ hơn

    displayedCurrentPrice = newPrice;

    // ── Giá hiện tại ─────────────────────────────────────────────────
    setText(lblCurrentPrice, formatVnd(newPrice));

    // ── Hiển thị người đặt ────────────────────────────────────────────
    if (bidderUsername != null && !bidderUsername.isBlank()) {
      setText(lblHighestBidder, bidderUsername);
    }

    // ── Cập nhật biểu đồ ─────────────────────────────────────────────
    String label = DISPLAY_FORMATTER.format(LocalDateTime.now());
    addChartPoint(label, newPrice);

    // ── Cập nhật số dư user (giá trị này đã thay đổi sau khi đặt cọc) ─
    loadCurrentUserInfo();
  }

  /**
   * Phiên đấu giá kết thúc — khoá nút đặt giá.
   * Đã chạy trên JavaFX Application Thread.
   */
  @Override
  public void onAuctionEnded(int auctionId, String status) {
    auctionEnded = true;
    if (currentItem != null) currentItem.setStatus("FINISHED"); // Cập nhật state
    stopCountdownTimer();

    if (btnBid != null) {
      btnBid.setDisable(true);
      btnBid.setText("ĐÃ ĐÓNG ĐẤU GIÁ");
      btnBid.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; "
        + "-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 13;");
    }

    if (txtBidAmount != null) txtBidAmount.setDisable(true);
    if (lblTimer   != null) {
      lblTimer.setText("KẾT THÚC");
      lblTimer.setStyle("-fx-font-size: 28px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
    }

    // --- BẠN CẦN THÊM DÒNG NÀY VÀO ĐÂY ---
    // Gọi hàm kiểm tra để tự động hiện nút "XÁC NHẬN THANH TOÁN" nếu user này là người thắng
    Platform.runLater(() -> {
        checkPaymentButtonVisibility();
    });
    // ------------------------------------

    boolean isCanceled = "CANCELED".equalsIgnoreCase(status);
    String msg = isCanceled
      ? "Phiên đấu giá đã bị HỦY (không có người mua)."
      : "Phiên đấu giá đã KẾT THÚC! Vui lòng xác nhận thanh toán nếu bạn là người chiến thắng."; // Sửa lại câu thông báo một chút cho hợp lý
    showAlert("Thông báo phiên đấu giá", msg);
  }

  // =========================================================================
  // COUNTDOWN TIMER
  // =========================================================================

  private void parseAndStartTimer(String endTimeStr) {
    if (endTimeStr == null || endTimeStr.isBlank()) return;

    try {
      // Server có thể trả về "2024-01-15 20:00:00.0" → cắt milliseconds
      String normalized = endTimeStr.trim();
      if (normalized.contains(".")) {
        normalized = normalized.substring(0, normalized.indexOf("."));
      }
      auctionEndTime = LocalDateTime.parse(normalized, DT_FORMATTER);
      startCountdownTimer();
    } catch (Exception e) {
      System.err.println("[BiddingRoom] Không parse được endTime: " + endTimeStr);
    }
  }

  private void startCountdownTimer() {
    timerScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "auction-countdown");
      t.setDaemon(true);
      return t;
    });

    timerTask = timerScheduler.scheduleAtFixedRate(() -> {
      if (auctionEndTime == null || auctionEnded) return;

      LocalDateTime now = LocalDateTime.now();
      long totalSeconds = java.time.Duration.between(now, auctionEndTime).getSeconds();

      if (totalSeconds <= 0) {
        Platform.runLater(() -> {
          setText(lblTimer, "00:00:00");
          if (lblTimer != null) {
            lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
          }
        });
        stopCountdownTimer();
        return;
      }

      long hours   = totalSeconds / 3600;
      long minutes = (totalSeconds % 3600) / 60;
      long seconds = totalSeconds % 60;
      String formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

      // Đổi màu timer khi sắp hết giờ (dưới 5 phút)
      final String timerStyle;
      if (totalSeconds <= 300) {
        timerStyle = "-fx-font-size: 40px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;";
      } else {
        timerStyle = "-fx-font-size: 40px; -fx-text-fill: #27ae60; -fx-font-weight: bold;";
      }

      Platform.runLater(() -> {
        setText(lblTimer, formatted);
        if (lblTimer != null) lblTimer.setStyle(timerStyle);
      });

    }, 0, 1, TimeUnit.SECONDS);
  }

  private void stopCountdownTimer() {
    if (timerTask != null && !timerTask.isDone()) {
      timerTask.cancel(false);
    }
    if (timerScheduler != null && !timerScheduler.isShutdown()) {
      timerScheduler.shutdownNow();
    }
  }

  // =========================================================================
  // THAO TÁC NGƯỜI DÙNG
  // =========================================================================

  @FXML
  private void handleBidAction() {

    // THÊM ĐOẠN NÀY LÊN ĐẦU HÀM
    if (!"RUNNING".equalsIgnoreCase(currentItem.getStatus())) {
        showAlert("Thông báo", "Phiên đấu giá này hiện chưa mở hoặc đã kết thúc. Bạn không thể đặt giá lúc này!");
        return;
    }

    if (auctionEnded) {
      showAlert("Thông báo", "Phiên đấu giá này đã kết thúc!");
      return;
    }
    if (currentItem == null) {
      showAlert("Lỗi", "Chưa có sản phẩm đấu giá!");
      return;
    }

    String amountStr = (txtBidAmount != null && txtBidAmount.getText() != null)
      ? txtBidAmount.getText().trim() : "";
    if (amountStr.isEmpty()) {
      showAlert("Thông báo", "Vui lòng nhập số tiền muốn đấu giá!");
      return;
    }

    try {
      double bidAmount = Double.parseDouble(amountStr);
      double minRequired = displayedCurrentPrice + Math.max(0, currentItem.getPriceStep());

      if (bidAmount < minRequired) {
        showAlert("Lỗi",
          "Giá đấu phải ít nhất bằng giá hiện tại + bước giá.\n"
            + "Mức tối thiểu: " + formatVnd(minRequired));
        return;
      }

      int auctionId = currentItem.getAuctionId();
      if (auctionId <= 0) {
        showAlert("Lỗi", "Sản phẩm này chưa có auctionId hợp lệ!");
        return;
      }

      // Khoá nút tránh double-click
      if (btnBid != null) btnBid.setDisable(true);

      AuctionFacade.getInstance().placeBid(auctionId, bidAmount,
        new ApiCallback<com.google.gson.JsonObject>() {

          @Override
          public void onSuccess(com.google.gson.JsonObject result) {
            Platform.runLater(() -> {
              // Giá của mình được ghi nhận; realtime update sẽ tới qua WS
              showAlert("Thành công",
                "Đã đặt giá " + formatVnd(bidAmount) + " thành công!");
              if (txtBidAmount != null) txtBidAmount.clear();
              if (btnBid != null) btnBid.setDisable(false);
            });
          }

          @Override
          public void onError(String errorMessage) {
            Platform.runLater(() -> {
              showAlert("Từ chối giá", errorMessage);
              if (btnBid != null) btnBid.setDisable(false);
            });
          }
        });

    } catch (NumberFormatException e) {
      showAlert("Lỗi", "Số tiền phải là con số hợp lệ!");
    } catch (Exception e) {
      e.printStackTrace();
      showAlert("Lỗi hệ thống", "Không thể đặt giá: " + e.getMessage());
    }
  }

  @FXML
  private void handleBackToHome(ActionEvent event) {
    cleanup();
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/view/Product.fxml"));
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(new Scene(root, 800, 500));
      stage.setTitle("Chợ Đấu Giá - Người mua");
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      showAlert("Lỗi hệ thống", "Không thể quay lại trang chủ: " + e.getMessage());
    }
  }

  @FXML
  private void handlePayment() {
    // 1. Khởi tạo Alert xác nhận
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Xác nhận thanh toán");
    alert.setHeaderText(null);
    alert.setContentText("Bạn có chắc chắn muốn chuyển " + formatVnd(currentItem.getCurrentMaxPrice()) + " cho người bán?");

    Optional<ButtonType> option = alert.showAndWait();
    
    if (option.isPresent() && option.get() == ButtonType.OK) {
        if (btnPay != null) btnPay.setDisable(true);

        // FIX 1: Dùng AuctionFacade.getInstance() thay vì paymentService
        // FIX 2: Dùng full name com.google.gson.JsonObject để tránh lỗi thiếu import
        AuctionFacade.getInstance().processPayment(currentItem.getAuctionId(), new ApiCallback<com.google.gson.JsonObject>() {
            @Override
            public void onSuccess(com.google.gson.JsonObject result) {
                Platform.runLater(() -> {
                    if (result != null && "success".equals(result.get("status").getAsString())) {
                        showAlert("Thành công", "Thanh toán thành công! Giao dịch đã hoàn tất.");
                        if (btnPay != null) {
                            btnPay.setVisible(false);
                            btnPay.setManaged(false);
                        }
                        
                        // FIX 3: Dùng BiddingRoomController.this để IDE nhận diện rõ field và method của lớp ngoài
                        BiddingRoomController.this.currentItem.setStatus("PAID");
                        BiddingRoomController.this.loadCurrentUserInfo(); 
                        
                    } else {
                        if (btnPay != null) btnPay.setDisable(false);
                        String msg = (result != null && result.has("message")) ? result.get("message").getAsString() : "Lỗi xác định.";
                        showAlert("Lỗi", msg);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    if (btnPay != null) btnPay.setDisable(false);
                    showAlert("Lỗi kết nối", "Không thể thanh toán: " + errorMessage);
                });
            }
        });
    }
  }
  
  
  // =========================================================================
  // HÀM NỘI BỘ
  // =========================================================================

  /**
   * Dọn dẹp tài nguyên: ngắt WebSocket và dừng timer.
   * Gọi khi rời phòng đấu giá.
   */
  private void cleanup() {
    wsClient.disconnect();
    stopCountdownTimer();
  }

  private void loadCurrentUserInfo() {
    AuctionFacade.getInstance().getUserProfile(new ApiCallback<UserDTO>() {
      @Override
      public void onSuccess(UserDTO user) {
        Platform.runLater(() -> {
          if (user == null) return;
          currentUser = user; // Gán user hiện tại vào biến toàn cục
          String name = (user.getFullName() != null && !user.getFullName().isEmpty())
            ? user.getFullName() : user.getUsername();
          setText(lblCurrentUser, "Người dùng: " + name);
          setText(lblUserBalance, "Số dư: " + formatVnd(user.getBalance()));
          
          checkPaymentButtonVisibility(); // Kiểm tra xem có được hiện nút Pay không
        });
      }
      @Override
      public void onError(String err) {
        System.err.println("[BiddingRoom] Lỗi tải profile: " + err);
      }
    });
  }

  // Thêm hàm này ngay bên dưới
  private void checkPaymentButtonVisibility() {
    if (currentItem == null || currentUser == null || btnPay == null) return;

    boolean isFinished = "FINISHED".equalsIgnoreCase(currentItem.getStatus()) || auctionEnded;
    boolean isWinner = (currentUser.getId() == currentItem.getHighestBidderId());

    if (isFinished && isWinner) {
        btnPay.setVisible(true);
        btnPay.setManaged(true);
    } else {
        btnPay.setVisible(false);
        btnPay.setManaged(false);
    }
  }

  private void loadProductImage(String rawPath) {
    if (rawPath == null || rawPath.isBlank() || imgProduct == null) return;
    String imageUrl = resolveImageUrl(rawPath);
    if (imageUrl == null) return;
    try {
      imgProduct.setImage(new Image(imageUrl, true));
    } catch (Exception e) {
      System.err.println("[BiddingRoom] Lỗi load ảnh: " + e.getMessage());
    }
  }

  /**
   * Thêm một điểm vào biểu đồ lịch sử giá.
   * Tự giới hạn tối đa 20 điểm để biểu đồ không bị chật.
   */
  private void addChartPoint(String label, double price) {
    if (chartSeries == null) return;
    Platform.runLater(() -> {
      chartSeries.getData().add(new XYChart.Data<>(label, price));
      if (chartSeries.getData().size() > 20) {
        chartSeries.getData().remove(0);
      }
    });
  }

  private void setText(Label label, String value) {
    if (label != null) label.setText(value);
  }

  private String safeText(String value, String fallback) {
    return (value == null || value.isBlank()) ? fallback : value;
  }

  private String formatVnd(double value) {
    return String.format("%,.0f VNĐ", value);
  }

  private String resolveImageUrl(String rawPath) {
    if (rawPath == null || rawPath.isBlank()) return null;
    if (rawPath.startsWith("http")) return rawPath;
    String filename = rawPath;
    int lastSlash = Math.max(rawPath.lastIndexOf('/'), rawPath.lastIndexOf('\\'));
    if (lastSlash >= 0) filename = rawPath.substring(lastSlash + 1);
    return SERVER_BASE_URL + "/images/" + filename;
  }
}