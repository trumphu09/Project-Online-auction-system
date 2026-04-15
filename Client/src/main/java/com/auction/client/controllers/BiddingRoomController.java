package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class BiddingRoomController {

    // --- LEFT PANE ---
    @FXML private ImageView imgProduct;
    @FXML private Label lblDescription, lblSeller, lblStartPrice, lblStepPrice, lblStartTime, lblEndTime;

    // --- CENTER PANE ---
    @FXML private Label lblTimer;        // Đồng hồ đếm ngược
    @FXML private Label lblCurrentPrice; // Giá hiện tại
    @FXML private Label lblHighestBidder;// Tên người đang thắng

    // --- RIGHT PANE ---
    @FXML private LineChart<String, Number> chartHistory; // Biểu đồ
    @FXML private TextField txtBidAmount;                // Ô nhập giá
    @FXML private Button btnBid;                          // Nút BID

    @FXML
    public void initialize() {
        // Khởi tạo biểu đồ giả lập để Đại nhìn cho đẹp
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lịch sử giá");
        series.getData().add(new XYChart.Data<>("10:00", 100));
        series.getData().add(new XYChart.Data<>("10:05", 150));
        chartHistory.getData().add(series);

        // Gợi ý: Đại nên cho lblTimer có font to và màu đỏ
        lblTimer.setStyle("-fx-font-size: 40px; -fx-text-fill: red; -fx-font-weight: bold;");
    }

    @FXML
    private void handleBidAction() {
        String amount = txtBidAmount.getText();
        if (amount.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tiền!");
            return;
        }

        // Tạm thời in ra để test nút
        System.out.println("Đại vừa trả giá: " + amount);

        // Logic sau này: Kiểm tra giá nhập > giá hiện tại + bước giá
        // Sau đó gửi lên Server cho Toản/Thành xử lý
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
