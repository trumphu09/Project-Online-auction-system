package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
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

        lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: red; -fx-font-weight: bold;");
    }

    // 2. SỬA HÀM setData: Nhận đầu vào là ItemDTO xịn từ ProductViewController
    public void setData(ItemDTO item) {
        this.currentItem = item; // Lưu lại để dùng cho nút BID

        // Đổ dữ liệu từ "thùng hàng" DTO ra các Label
        lblDescription.setText(item.getDescription());
        lblStartPrice.setText(String.format("%,.0f VNĐ", item.getStartingPrice()));
        lblStepPrice.setText(String.format("%,.0f VNĐ", item.getPriceStep()));
        lblStartTime.setText("Bắt đầu: " + item.getStartTime());
        lblEndTime.setText("Kết thúc: " + item.getEndTime());

        // Giá hiện tại lúc mới mở phòng chính là giá khởi điểm
        lblCurrentPrice.setText(String.format("%,.0f VNĐ", item.getStartingPrice()));
        lblHighestBidder.setText("Chưa có người ra giá");

        // Cập nhật ảnh nếu có
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            imgProduct.setImage(new Image("file:" + item.getImagePath()));
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