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
import java.util.List;
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
  @FXML private Button btnPay;
  @FXML private TextField txtAutoMaxAmount;
  @FXML private Button btnAutoBid;

  private boolean isAutoBidActive = false;
  private boolean isAutoBidProcessing = false;  // ✅ Cờ để tránh bấm liên tục
  private UserDTO currentUser;

  // ─── Hằng số ─────────────────────────────────────────────────────────────
  private static final String SERVER_BASE_URL = "http://localhost:8080/api";
  private static final DateTimeFormatter DT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter DISPLAY_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter CHART_TIME_FMT =
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
  private boolean hasRatedThisAuction = false;

  // ─── Series biểu đồ ──────────────────────────────────────────────────────
  private XYChart.Series<String, Number> chartSeries;

  // =========================================================================
  // KHỞI TẠO
  // =========================================================================

  @FXML
  public void initialize() {
    try {
      if (chartHistory != null) {
        chartSeries = new XYChart.Series<>();
        chartSeries.setName("Lịch sử giá");
        chartHistory.getData().add(chartSeries);
        chartHistory.setAnimated(false);
      }

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
  // NHẬN DỮ LIỆU SẢN PHẨM
  // =========================================================================

  public void setData(ItemDTO item) {
      if (item == null) {
        showAlert("Lỗi", "Không có dữ liệu sản phẩm để hiển thị!");
        return;
      }

      this.currentItem = item;
      this.hasRatedThisAuction = true;

      setText(lblDescription, safeText(item.getDescription(), "Không có mô tả"));
      setText(lblStartPrice,  formatVnd(item.getStartingPrice()));
      setText(lblStepPrice,   formatVnd(item.getPriceStep()));
      setText(lblStartTime,   "Bắt đầu: " + safeText(item.getStartTime(), "N/A"));
      setText(lblEndTime,     "Kết thúc: " + safeText(item.getEndTime(), "N/A"));
      setText(lblCategory,    "Loại: " + safeText(item.getCategory(), "N/A"));

      int sellerId = item.getSellerId();

      AuctionFacade.getInstance().getUserById(sellerId, new ApiCallback<UserDTO>() {
          @Override
          public void onSuccess(UserDTO seller) {
              Platform.runLater(() -> {
                  if (seller != null) {
                      String sellerName =
                              seller.getFullName() != null && !seller.getFullName().isBlank()
                                      ? seller.getFullName()
                                      : seller.getUsername();

                      setText(lblSeller, "Người bán: " + sellerName);

                      if (lblSellerRating != null) {
                          lblSellerRating.setText(String.format("⭐ %.1f", seller.getTotalRating()));
                      }

                      if (lblSellerSaleCount != null) {
                          lblSellerSaleCount.setText("Đã bán: " + seller.getSaleCount());
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
                  if (lblSellerRating != null) lblSellerRating.setText("");
                  if (lblSellerSaleCount != null) lblSellerSaleCount.setText("");
              });
          }
      });

      displayedCurrentPrice = item.getCurrentMaxPrice() > 0
              ? item.getCurrentMaxPrice()
              : item.getStartingPrice();
      setText(lblCurrentPrice, formatVnd(displayedCurrentPrice));

      if (item.getHighestBidderId() > 0) {
        setText(lblHighestBidder, "Bidder #" + item.getHighestBidderId());
      } else {
        setText(lblHighestBidder, "Chưa có người ra giá");
      }

      addChartPoint("Khởi điểm", displayedCurrentPrice);

      loadBidHistoryFromServer(item.getId());

      loadProductImage(item.getImagePath());
      parseAndStartTimer(item.getEndTime());
      connectWebSocket();

      loadRatingStatus();
      syncAutoBidStatus();
  }

  /**
   * Tải lịch sử bid từ server API và thêm vào biểu đồ.
   * @param itemId ID của sản phẩm
   */
  private void loadBidHistoryFromServer(int itemId) {
    AuctionFacade.getInstance().getBidHistory(itemId, new ApiCallback<List<JsonObject>>() {
      @Override
      public void onSuccess(List<JsonObject> bidHistory) {
        Platform.runLater(() -> {
          if (bidHistory != null && !bidHistory.isEmpty()) {
            System.out.println("[BiddingRoom] Đã load " + bidHistory.size() + " bid lịch sử từ server");

            if (!bidHistory.isEmpty()) {
              System.out.println("[BiddingRoom] DEBUG - First bid object: " + bidHistory.get(0).toString());
            }

            if (chartSeries != null && !chartSeries.getData().isEmpty()) {
              chartSeries.getData().clear();
            }

            int successCount = 0;
            for (int idx = 0; idx < bidHistory.size(); idx++) {
              try {
                JsonObject bid = bidHistory.get(idx);

                double bidAmount = 0;
                if (bid.has("bidAmount")) {
                  bidAmount = bid.get("bidAmount").getAsDouble();
                } else if (bid.has("bid_amount")) {
                  bidAmount = bid.get("bid_amount").getAsDouble();
                }

                String bidderUsername = "";
                if (bid.has("bidderUsername")) {
                  bidderUsername = bid.get("bidderUsername").getAsString();
                } else if (bid.has("bidder_username")) {
                  bidderUsername = bid.get("bidder_username").getAsString();
                }

                String timestamp = "";
                if (bid.has("timestamp")) {
                  timestamp = bid.get("timestamp").getAsString();
                } else if (bid.has("bid_time")) {
                  timestamp = bid.get("bid_time").getAsString();
                } else if (bid.has("bidTime")) {
                  timestamp = bid.get("bidTime").getAsString();
                }

                System.out.println("[BiddingRoom] Bid #" + (idx+1) + " - amount=" + bidAmount
                  + ", username=" + bidderUsername + ", timestamp=" + timestamp);

                String timeLabel = parseTimeLabel(timestamp);
                System.out.println("[BiddingRoom] Bid #" + (idx+1) + " - timeLabel=" + timeLabel);

                addChartPoint(timeLabel, bidAmount);
                successCount++;

              } catch (Exception e) {
                System.err.println("[BiddingRoom] Lỗi parse bid #" + (idx+1) + ": " + e.getMessage());
                e.printStackTrace();
              }
            }

            System.out.println("[BiddingRoom] Đã load xong " + successCount + "/" + bidHistory.size() + " bid vào biểu đồ");
          }
        });
      }

      @Override
      public void onError(String errorMessage) {
        System.err.println("[BiddingRoom] Lỗi load lịch sử bid: " + errorMessage);
        Platform.runLater(() -> {
          System.err.println("[BiddingRoom] Fallback: Giữ điểm khởi điểm, chỉ load realtime");
        });
      }
    });
  }

  /**
   * Parse timestamp từ API thành label ngắn cho chart (format: HH:mm:ss)
   */
  private String parseTimeLabel(String timestamp) {
      if (timestamp == null || timestamp.isBlank()) {
          return "N/A";
      }

      String normalized = timestamp.trim();
      if (normalized.endsWith("Z")) {
          normalized = normalized.substring(0, normalized.length() - 1);
      }
      int plusIdx = normalized.lastIndexOf('+');
      if (plusIdx > 10) {
          normalized = normalized.substring(0, plusIdx);
      }
      int minusIdx = normalized.lastIndexOf('-');
      if (minusIdx > 16) {
          normalized = normalized.substring(0, minusIdx);
      }

      for (DateTimeFormatter fmt : TIMESTAMP_PARSERS) {
          try {
              LocalDateTime ldt = LocalDateTime.parse(normalized, fmt);
              return ldt.format(CHART_TIME_FMT);
          } catch (Exception ignored) {
          }
      }

      System.err.println("[BiddingRoom] parseTimeLabel fallback: " + timestamp);
      if (normalized.length() >= 8) {
          return normalized.substring(normalized.length() - 8);
      }
      return timestamp;
  }

  private void loadRatingStatus() {
    if (currentItem == null) return;

    AuctionFacade.getInstance().hasRatedSeller(
        currentItem.getAuctionId(),
        new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Platform.runLater(() -> {
                    boolean rated = false;
                    if (result != null && result.has("data") && result.get("data").isJsonObject()) {
                        JsonObject data = result.getAsJsonObject("data");
                        rated = data.has("rated") && data.get("rated").getAsBoolean();
                    }
                    hasRatedThisAuction = rated;
                    checkPaymentButtonVisibility();
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    hasRatedThisAuction = true;
                    checkPaymentButtonVisibility();
                });
            }
        }
    );
  }

  // =========================================================================
  // WEBSOCKET
  // =========================================================================

  private void connectWebSocket() {
    if (currentItem == null || currentItem.getAuctionId() <= 0) {
      System.err.println("[BiddingRoom] Không thể kết nối WS: auctionId không hợp lệ.");
      return;
    }
    wsClient.connect(currentItem.getAuctionId(), this);
  }

  // =========================================================================
  // BidUpdateListener
  // =========================================================================

  @Override
  public void onConnected() {
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

  @Override
  public void onNewBid(int auctionId, double newPrice, String bidderUsername) {
      onNewBid(auctionId, newPrice, bidderUsername, null);
  }

  @Override
  public void onNewBid(int auctionId, double newPrice, String bidderUsername, String newEndTime) {
      if (newPrice > 0 && newPrice > displayedCurrentPrice) {
          displayedCurrentPrice = newPrice;
          setText(lblCurrentPrice, formatVnd(newPrice));

          if (bidderUsername != null && !bidderUsername.isBlank()) {
              setText(lblHighestBidder, bidderUsername);
          }

          String label = DISPLAY_FORMATTER.format(LocalDateTime.now());
          addChartPoint(label, newPrice);

          loadCurrentUserInfo();
          checkPaymentButtonVisibility();
          syncAutoBidStatus();
      }

      if (newEndTime != null && !newEndTime.isBlank()) {
          applyNewEndTimeFromServer(newEndTime);
      }
  }

  private void applyNewEndTimeFromServer(String newEndTimeIso) {
      try {
          LocalDateTime parsedEndTime = LocalDateTime.parse(newEndTimeIso);

          Platform.runLater(() -> {
              this.auctionEndTime = parsedEndTime;

              if (lblEndTime != null) {
                  lblEndTime.setText("Kết thúc: " + parsedEndTime.format(DT_FORMATTER));
              }

              restartCountdownTimer();

              if (lblBidNote != null) {
                  lblBidNote.setText("⏰ Phiên đấu giá vừa được gia hạn thêm 1 phút!");
                  lblBidNote.setStyle("-fx-font-size: 11; -fx-text-fill: #f39c12; -fx-font-style: italic; -fx-font-weight: bold;");
              }

              if (lblTimer != null) {
                  lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
              }
          });

      } catch (Exception ex) {
          System.err.println("[BiddingRoom] Không parse được newEndTime: " + newEndTimeIso);
      }
  }

  private void restartCountdownTimer() {
      stopCountdownTimer();
      startCountdownTimer();
  }

  @Override
  public void onAuctionEnded(int auctionId, String status) {
    auctionEnded = true;
    if (currentItem != null) currentItem.setStatus("FINISHED");
    stopCountdownTimer();

    if (btnBid != null) {
      btnBid.setDisable(true);
      btnBid.setText("ĐÃ ĐÓNG ĐẤU GIÁ");
      btnBid.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; "
        + "-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 13;");
    }

    if (txtBidAmount != null) txtBidAmount.setDisable(true);
    if (lblTimer != null) {
      lblTimer.setText("KẾT THÚC");
      lblTimer.setStyle("-fx-font-size: 28px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
    }

    Platform.runLater(() -> {
        checkPaymentButtonVisibility();
    });

    boolean isCanceled = "CANCELED".equalsIgnoreCase(status);
    String msg = isCanceled
      ? "Phiên đấu giá đã bị HỦY (không có người mua)."
      : "Phiên đấu giá đã KẾT THÚC! Vui lòng xác nhận thanh toán nếu bạn là người chiến thắng.";
    showAlert("Thông báo phiên đấu giá", msg);
  }

  // =========================================================================
  // COUNTDOWN TIMER
  // =========================================================================

  private void parseAndStartTimer(String endTimeStr) {
    if (endTimeStr == null || endTimeStr.isBlank()) return;

    try {
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

          final String timerStyle = totalSeconds <= 300
              ? "-fx-font-size: 40px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;"
              : "-fx-font-size: 40px; -fx-text-fill: #27ae60; -fx-font-weight: bold;";

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

    if (currentItem == null) {
        showAlert("Lỗi", "Chưa có sản phẩm đấu giá!");
        return;
    }

    if (!"RUNNING".equalsIgnoreCase(currentItem.getStatus())) {
        showAlert("Thông báo", "Phiên đấu giá này hiện chưa mở hoặc đã kết thúc. Bạn không thể đặt giá lúc này!");
        return;
    }

    if (auctionEnded) {
        showAlert("Thông báo", "Phiên đấu giá này đã kết thúc!");
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

      if (btnBid != null) btnBid.setDisable(true);

      AuctionFacade.getInstance().placeBid(auctionId, bidAmount,
        new ApiCallback<com.google.gson.JsonObject>() {

          @Override
          public void onSuccess(com.google.gson.JsonObject result) {
            Platform.runLater(() -> {
              showAlert("Thành công",
                "Đã đặt giá " + formatVnd(bidAmount) + " thành công!");
              if (txtBidAmount != null) txtBidAmount.clear();
              if (btnBid != null) btnBid.setDisable(false);
              syncAutoBidStatus();
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
      if (currentItem == null || currentUser == null) {
          showAlert("Lỗi", "Thiếu dữ liệu phiên hoặc người dùng.");
          return;
      }

      if (!"FINISHED".equalsIgnoreCase(currentItem.getStatus()) && !auctionEnded) {
          showAlert("Thông báo", "Phiên đấu giá chưa kết thúc.");
          return;
      }

      if (currentUser.getId() != currentItem.getHighestBidderId()) {
          showAlert("Thông báo", "Bạn không phải người thắng phiên đấu giá này.");
          return;
      }

      java.util.List<Integer> choices = java.util.Arrays.asList(0, 1, 2, 3, 4, 5);
      javafx.scene.control.ChoiceDialog<Integer> dialog =
          new javafx.scene.control.ChoiceDialog<>(5, choices);

      dialog.setTitle("Đánh giá người bán");
      dialog.setHeaderText("Chọn số sao cho người bán");
      dialog.setContentText("Điểm đánh giá (0 - 5):");

      Optional<Integer> result = dialog.showAndWait();
      if (result.isEmpty()) return;

      int rating = result.get();

      if (btnPay != null) btnPay.setDisable(true);

      AuctionFacade.getInstance().rateSeller(
          currentItem.getSellerId(),
          currentItem.getAuctionId(),
          rating,
          new ApiCallback<com.google.gson.JsonObject>() {
              @Override
              public void onSuccess(com.google.gson.JsonObject response) {
                  Platform.runLater(() -> {
                      hasRatedThisAuction = true;

                      showAlert("Thành công", "Cảm ơn bạn đã đánh giá người bán!");

                      if (btnPay != null) {
                          btnPay.setVisible(false);
                          btnPay.setManaged(false);
                      }

                      refreshSellerInfo(currentItem.getSellerId());
                  });
              }

              @Override
              public void onError(String errorMessage) {
                  Platform.runLater(() -> {
                      if (btnPay != null) btnPay.setDisable(false);
                      showAlert("Lỗi", errorMessage);
                  });
              }
          }
      );
  }

  private void refreshSellerInfo(int sellerId) {
      AuctionFacade.getInstance().getUserById(sellerId, new ApiCallback<UserDTO>() {
          @Override
          public void onSuccess(UserDTO seller) {
              Platform.runLater(() -> {
                  if (seller != null) {
                      String sellerName =
                          seller.getFullName() != null && !seller.getFullName().isBlank()
                              ? seller.getFullName()
                              : seller.getUsername();

                      setText(lblSeller, "Người bán: " + sellerName);

                      if (lblSellerRating != null) {
                          lblSellerRating.setText(String.format("⭐ %.1f", seller.getTotalRating()));
                      }
                      if (lblSellerSaleCount != null) {
                          lblSellerSaleCount.setText("Đã bán: " + seller.getSaleCount());
                      }
                  }
              });
          }

          @Override
          public void onError(String errorMessage) {
              System.err.println("Không tải lại được thông tin seller: " + errorMessage);
          }
      });
  }

  // =========================================================================
  // HÀM NỘI BỘ
  // =========================================================================

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
          currentUser = user;
          String name = (user.getFullName() != null && !user.getFullName().isEmpty())
            ? user.getFullName() : user.getUsername();
          setText(lblCurrentUser, "Người dùng: " + name);
          setText(lblUserBalance, "Số dư: " + formatVnd(user.getBalance()));
          checkPaymentButtonVisibility();
        });
      }
      @Override
      public void onError(String err) {
        System.err.println("[BiddingRoom] Lỗi tải profile: " + err);
      }
    });
  }

  private void checkPaymentButtonVisibility() {
      if (currentItem == null || currentUser == null || btnPay == null) return;

      boolean isFinished = "FINISHED".equalsIgnoreCase(currentItem.getStatus()) || auctionEnded;
      boolean isWinner = (currentUser.getId() == currentItem.getHighestBidderId());

      if (isFinished && isWinner && !hasRatedThisAuction) {
          btnPay.setText("ĐÁNH GIÁ NGƯỜI BÁN");
          btnPay.setVisible(true);
          btnPay.setManaged(true);
          btnPay.setDisable(false);
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

  private static final DateTimeFormatter[] TIMESTAMP_PARSERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
  };

  // chức năng auto-bid: bật/tắt, đồng bộ trạng thái với server, và cập nhật UI tương ứng
  @FXML
  private void handleToggleAutoBid() {
      // ✅ Tránh bấm liên tục
      if (isAutoBidProcessing) {
          System.out.println("[BiddingRoom] Auto-bid đang xử lý, vui lòng chờ...");
          return;
      }

      if (currentItem == null) {
          showAlert("Lỗi", "Chưa có sản phẩm đấu giá.");
          return;
      }

      if (auctionEnded || "FINISHED".equalsIgnoreCase(currentItem.getStatus())) {
          showAlert("Thông báo", "Phiên đấu giá đã kết thúc.");
          return;
      }

      isAutoBidProcessing = true;
      if (btnAutoBid != null) {
          btnAutoBid.setDisable(true);  // ✅ Disable nút khi đang xử lý
      }

      // ✅ TẮT AUTO-BID
      if (isAutoBidActive) {
          System.out.println("[BiddingRoom] Tắt auto-bid cho auction: " + currentItem.getAuctionId());
          AuctionFacade.getInstance().cancelAutoBid(currentItem.getAuctionId(), new ApiCallback<JsonObject>() {
              @Override
              public void onSuccess(JsonObject result) {
                  Platform.runLater(() -> {
                      System.out.println("[BiddingRoom] Tắt auto-bid thành công");
                      isAutoBidActive = false;
                      updateAutoBidUI(false);
                      
                      String message = "Đã tắt auto-bid.";
                      if (result != null && result.has("message")) {
                          message = result.get("message").getAsString();
                      }
                      
                      showAlert("Thành công", message);
                      syncAutoBidStatus();
                      
                      // ✅ Reset flag và enable nút
                      isAutoBidProcessing = false;
                      if (btnAutoBid != null) {
                          btnAutoBid.setDisable(false);
                      }
                  });
              }

              @Override
              public void onError(String errorMessage) {
                  Platform.runLater(() -> {
                      System.err.println("[BiddingRoom] Lỗi tắt auto-bid: " + errorMessage);
                      showAlert("Lỗi", "Không thể tắt auto-bid: " + errorMessage);
                      syncAutoBidStatus();
                      
                      // ✅ Reset flag và enable nút
                      isAutoBidProcessing = false;
                      if (btnAutoBid != null) {
                          btnAutoBid.setDisable(false);
                      }
                  });
              }
          });
          return;
      }

      // ✅ BẬT AUTO-BID
      String raw = txtAutoMaxAmount != null && txtAutoMaxAmount.getText() != null
              ? txtAutoMaxAmount.getText().trim()
              : "";

      if (raw.isBlank()) {
          showAlert("Lỗi", "Vui lòng nhập giá trần auto-bid.");
          isAutoBidProcessing = false;
          if (btnAutoBid != null) {
              btnAutoBid.setDisable(false);
          }
          return;
      }

      try {
          double maxAmount = Double.parseDouble(raw);
          if (maxAmount <= displayedCurrentPrice) {
              showAlert("Lỗi", "Giá trần phải lớn hơn giá hiện tại (" + formatVnd(displayedCurrentPrice) + ")");
              isAutoBidProcessing = false;
              if (btnAutoBid != null) {
                  btnAutoBid.setDisable(false);
              }
              return;
          }

          System.out.println("[BiddingRoom] Bật auto-bid: maxAmount=" + maxAmount + ", auctionId=" + currentItem.getAuctionId());
          
          AuctionFacade.getInstance().setupAutoBid(currentItem.getAuctionId(), maxAmount, new ApiCallback<JsonObject>() {
              @Override
              public void onSuccess(JsonObject result) {
                  Platform.runLater(() -> {
                      System.out.println("[BiddingRoom] Bật auto-bid thành công");
                      isAutoBidActive = true;
                      updateAutoBidUI(true);

                      String message = "Đã bật auto-bid với giá trần: " + formatVnd(maxAmount);
                      boolean autoBidExecuted = false;

                      try {
                          if (result != null) {
                              // Nếu message bị đẩy ra cấp root
                              if (result.has("message") && !result.get("message").isJsonNull()) {
                                  message = result.get("message").getAsString();
                              }

                              // Cập nhật kiểm tra trường hợp Facade đã bóc vỏ data
                              if (result.has("data") && result.get("data").isJsonObject()) {
                                  JsonObject data = result.getAsJsonObject("data");
                                  autoBidExecuted = data.has("auto_bid_executed") && data.get("auto_bid_executed").getAsBoolean();
                              } else if (result.has("auto_bid_executed")) {
                                  autoBidExecuted = result.get("auto_bid_executed").getAsBoolean();
                              }
                          }
                      } catch (Exception e) {
                          System.err.println("[BiddingRoom] Lỗi parse response: " + e.getMessage());
                      }

                      showAlert("Thành công", message);

                      if (autoBidExecuted) {
                          System.out.println("[BiddingRoom] Auto-bid đã thực thi, cập nhật user info");
                          loadCurrentUserInfo();
                      }
                      
                      // ✅ Clear text field khi bật thành công
                      if (txtAutoMaxAmount != null) {
                          txtAutoMaxAmount.clear();
                      }
                      
                      syncAutoBidStatus();
                      
                      // ✅ Reset flag và enable nút
                      isAutoBidProcessing = false;
                      if (btnAutoBid != null) {
                          btnAutoBid.setDisable(false);
                      }
                  });
              }

              @Override
              public void onError(String errorMessage) {
                  Platform.runLater(() -> {
                      System.err.println("[BiddingRoom] Lỗi bật auto-bid: " + errorMessage);
                      showAlert("Lỗi", "Không thể bật auto-bid: " + errorMessage);
                      syncAutoBidStatus();
                      
                      // ✅ Reset flag và enable nút
                      isAutoBidProcessing = false;
                      if (btnAutoBid != null) {
                          btnAutoBid.setDisable(false);
                      }
                  });
              }
          });

      } catch (NumberFormatException e) {
          System.err.println("[BiddingRoom] Giá trần không hợp lệ: " + raw);
          showAlert("Lỗi", "Giá trần không hợp lệ. Vui lòng nhập số.");
          isAutoBidProcessing = false;
          if (btnAutoBid != null) {
              btnAutoBid.setDisable(false);
          }
      }
  }

  private void updateAutoBidUI(boolean active) {
      if (btnAutoBid == null) return;

      if (active) {
          // ✅ AUTO-BID ĐANG HOẠT ĐỘNG - Hiển thị nút TẮT
          btnAutoBid.setText("❌ TẮT AUTO-BID");
          btnAutoBid.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 13; -fx-padding: 12; -fx-cursor: hand;");
          btnAutoBid.setDisable(false);  // ✅ Đảm bảo nút vẫn clickable
          
          // Disable text field khi auto-bid active
          if (txtAutoMaxAmount != null) {
              txtAutoMaxAmount.setDisable(true);
              txtAutoMaxAmount.setStyle("-fx-text-fill: #95a5a6; -fx-control-inner-background: #ecf0f1; -fx-opacity: 0.7;");
          }
      } else {
          // ✅ AUTO-BID TẮT - Hiển thị nút BẬT
          btnAutoBid.setText("⚡ BẬT AUTO-BID");
          btnAutoBid.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 13; -fx-padding: 12; -fx-cursor: hand;");
          btnAutoBid.setDisable(false);  // ✅ Đảm bảo nút vẫn clickable
          
          // Enable text field khi auto-bid tắt
          if (txtAutoMaxAmount != null) {
              txtAutoMaxAmount.setDisable(false);
              txtAutoMaxAmount.setStyle("-fx-opacity: 1.0;");
          }
      }
  }

  private void syncAutoBidStatus() {
      if (currentItem == null) return;
      AuctionFacade.getInstance().getAutoBidStatus(currentItem.getAuctionId(), new ApiCallback<JsonObject>() {
          @Override
          public void onSuccess(JsonObject result) {
              Platform.runLater(() -> {
                  boolean active = false;
                  double maxAmount = 0;
                   
                  if (result != null) {
                      // SỬA LỖI Ở ĐÂY: Hỗ trợ cả trường hợp chưa bóc và đã bóc lớp "data"
                      if (result.has("data") && result.get("data").isJsonObject()) {
                          JsonObject data = result.getAsJsonObject("data");
                          active = data.has("active") && data.get("active").getAsBoolean();
                          if (data.has("max_amount")) {
                              maxAmount = data.get("max_amount").getAsDouble();
                          }
                      } else {
                          // Xử lý khi AuctionFacade đã bóc lớp "data", result chính là dữ liệu bên trong
                          active = result.has("active") && result.get("active").getAsBoolean();
                          if (result.has("max_amount")) {
                              maxAmount = result.get("max_amount").getAsDouble();
                          }
                      }
                  }
                  
                  System.out.println("[BiddingRoom] Auto-bid status: active=" + active + ", maxAmount=" + maxAmount);
                  
                  isAutoBidActive = active;
                  updateAutoBidUI(active);
                  
                  // Cập nhật text field với giá trần từ server
                  if (active && txtAutoMaxAmount != null && maxAmount > 0) {
                      txtAutoMaxAmount.setText(formatVnd(maxAmount));
                  }
              });
          }

          @Override
          public void onError(String errorMessage) {
              System.err.println("[BiddingRoom] Lỗi load trạng thái auto-bid: " + errorMessage);
          }
      });
  }
}