package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
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

public class BiddingRoomController extends BaseController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblDescription, lblSeller, lblStartPrice, lblStepPrice, lblStartTime, lblEndTime;
    @FXML private Label lblTimer, lblCurrentPrice, lblHighestBidder;
    @FXML private Label lblCurrentUser, lblUserBalance, lblCategory;
    @FXML private LineChart<String, Number> chartHistory;
    @FXML private TextField txtBidAmount;
    @FXML private Button btnBid;
    @FXML private Label lblSellerRating, lblSellerSaleCount, lblBidNote;

    private static final String SERVER_BASE_URL = "http://localhost:8080/api";

    private ItemDTO currentItem;
    private double displayedCurrentPrice = 0;

    @FXML
    public void initialize() {
        try {
            if (chartHistory != null) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Lịch sử giá");
                chartHistory.getData().add(series);
            } else {
                System.err.println("ERROR: chartHistory is null - check fx:id in BiddingRoomView.fxml");
            }

            if (lblTimer != null) {
                lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: red; -fx-font-weight: bold;");
            }

            loadCurrentUserInfo();
        } catch (Exception e) {
            System.err.println("ERROR in initialize(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCurrentUserInfo() {
        AuctionFacade.getInstance().getUserProfile(new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO user) {
                Platform.runLater(() -> {
                    if (user != null && lblCurrentUser != null && lblUserBalance != null) {
                        String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                                ? user.getFullName()
                                : user.getUsername();
                        lblCurrentUser.setText("Người dùng: " + displayName);
                        lblUserBalance.setText("Số dư: " + formatVnd(user.getBalance()));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("Lỗi tải thông tin user: " + errorMessage);
            }
        });
    }

    public void setData(ItemDTO item) {
        System.out.println("DEBUG BiddingRoomController.setData(): item = " + item);

        if (item == null) {
            showAlert("Lỗi", "Không có dữ liệu sản phẩm để hiển thị!");
            return;
        }

        this.currentItem = item;

        setText(lblDescription, safeText(item.getDescription(), "Không có mô tả"));
        setText(lblStartPrice, formatVnd(item.getStartingPrice()));
        setText(lblStepPrice, formatVnd(item.getPriceStep()));
        setText(lblStartTime, "Bắt đầu: " + safeText(item.getStartTime(), "N/A"));
        setText(lblEndTime, "Kết thúc: " + safeText(item.getEndTime(), "N/A"));
        setText(lblCategory, "Loại: " + safeText(item.getCategory(), "N/A"));
        setText(lblSeller, "Người bán: " + item.getSellerId());

        displayedCurrentPrice = item.getCurrentMaxPrice() > 0
                ? item.getCurrentMaxPrice()
                : item.getStartingPrice();

        setText(lblCurrentPrice, formatVnd(displayedCurrentPrice));
        setText(lblHighestBidder, "Chưa có người ra giá");

        String imageUrl = resolveImageUrl(item.getImagePath());
        if (imageUrl != null && imgProduct != null) {
            try {
                imgProduct.setImage(new Image(imageUrl, true));
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh: " + e.getMessage());
            }
        }

        System.out.println("DEBUG: setData() completed successfully");
    }

    @FXML
    private void handleBidAction() {
        if (currentItem == null) {
            showAlert("Lỗi", "Chưa có sản phẩm đấu giá để đặt giá!");
            return;
        }

        if (txtBidAmount == null) {
            showAlert("Lỗi", "Ô nhập giá chưa được khởi tạo!");
            return;
        }

        String amountStr = txtBidAmount.getText() != null ? txtBidAmount.getText().trim() : "";
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

            AuctionFacade.getInstance().placeBid(auctionId, bidAmount, new ApiCallback<com.google.gson.JsonObject>() {
                @Override
                public void onSuccess(com.google.gson.JsonObject result) {
                    Platform.runLater(() -> {
                        showAlert("Thành công",
                                "Lệnh đặt giá " + formatVnd(bidAmount) + " đã được ghi nhận!");
                        txtBidAmount.clear();
                        displayedCurrentPrice = bidAmount;
                        setText(lblCurrentPrice, formatVnd(bidAmount));
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Platform.runLater(() -> showAlert("Từ chối giá", errorMessage));
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

    private void setText(Label label, String value) {
        if (label != null) {
            label.setText(value);
        }
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String formatVnd(double value) {
        return String.format("%,.0f VNĐ", value);
    }

    private String resolveImageUrl(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return null;
        }

        if (rawPath.startsWith("http")) {
            return rawPath;
        }

        String filename = rawPath;
        int lastSlash = Math.max(rawPath.lastIndexOf('/'), rawPath.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            filename = rawPath.substring(lastSlash + 1);
        }
        return SERVER_BASE_URL + "/images/" + filename;
    }
}