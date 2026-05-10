package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// 1. TÍNH KẾ THỨA: Kế thừa để dùng chung showAlert và các tiện ích khác
public class BiddingRoomController extends BaseController {

    @FXML private ImageView imgProduct;
    @FXML private Label lblDescription, lblSeller, lblStartPrice, lblStepPrice, lblStartTime, lblEndTime;
    @FXML private Label lblTimer, lblCurrentPrice, lblHighestBidder;
    @FXML private Label lblCurrentUser, lblUserBalance, lblCategory;
    @FXML private LineChart<String, Number> chartHistory;
    @FXML private TextField txtBidAmount;
    @FXML private Button btnBid;

    private ItemDTO currentItem; // Biến lưu trữ đối tượng đang đấu giá (Encapsulation)

    @FXML
    public void initialize() {
        // Khởi tạo biểu đồ trống
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lịch sử giá");
        chartHistory.getData().add(series);

        if (lblTimer != null) {
            lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: red; -fx-font-weight: bold;");
        }
        
        // Load thông tin user khi vào phòng đấu giá
        loadCurrentUserInfo();
    }
    
    // Load thông tin người dùng hiện tại
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
                        lblUserBalance.setText("Số dư: " + String.format("%,.0f VNĐ", user.getBalance()));
                    }
                });
            }
            @Override
            public void onError(String errorMessage) {
                System.err.println("Lỗi tải thông tin user: " + errorMessage);
            }
        });
    }

    // 2. SỬA HÀM setData: Nhận đầu vào là ItemDTO xịn từ ProductViewController
    public void setData(ItemDTO item) {
        this.currentItem = item; // Lưu lại để dùng cho nút BID

        // Đổ dữ liệu từ "thùng hàng" DTO ra các Label
        if (lblDescription != null) lblDescription.setText(item.getDescription());
        if (lblStartPrice != null) lblStartPrice.setText(String.format("%,.0f VNĐ", item.getStartingPrice()));
        if (lblStepPrice != null) lblStepPrice.setText(String.format("%,.0f VNĐ", item.getPriceStep()));
        if (lblStartTime != null) lblStartTime.setText("Bắt đầu: " + item.getStartTime());
        if (lblEndTime != null) lblEndTime.setText("Kết thúc: " + item.getEndTime());
        if (lblCategory != null) lblCategory.setText("Loại: " + (item.getCategory() != null ? item.getCategory() : "N/A"));

        // Giá hiện tại lúc mới mở phòng chính là giá khởi điểm
        if (lblCurrentPrice != null) lblCurrentPrice.setText(String.format("%,.0f VNĐ", item.getStartingPrice()));
        if (lblHighestBidder != null) lblHighestBidder.setText("Chưa có người ra giá");

        // Cập nhật ảnh nếu có
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                Image img = new Image("file:" + item.getImagePath(), true);
                if (imgProduct != null) {
                    imgProduct.setImage(img);
                }
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh: " + e.getMessage());
            }
        }
    }

@FXML
    private void handleBidAction() {
        String amountStr = txtBidAmount.getText().trim();
        if (amountStr.isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập số tiền muốn đấu giá!");
            return;
        }

        try {
            double bidAmount = Double.parseDouble(amountStr);
            if (bidAmount <= currentItem.getStartingPrice()) { 
                showAlert("Lỗi", "Giá đấu phải cao hơn giá hiện tại!");
                return;
            }

            // GỌI API ĐẶT GIÁ XUỐNG DB
            com.auction.client.service.AuctionFacade.getInstance().placeBid(currentItem.getId(), bidAmount, new com.auction.client.service.ApiCallback<com.google.gson.JsonObject>() {
                @Override
                public void onSuccess(com.google.gson.JsonObject result) {
                    showAlert("Thành công", "Lệnh đặt giá $" + bidAmount + " đã được ghi nhận!");
                    txtBidAmount.clear();
                    lblCurrentPrice.setText(String.format("%,.0f VNĐ", bidAmount));
                }

                @Override
                public void onError(String errorMessage) {
                    showAlert("Từ chối giá", errorMessage);
                }
            });

        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số tiền phải là con số hợp lệ!");
        }
    }
}