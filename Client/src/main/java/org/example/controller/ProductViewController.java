package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.io.IOException;

public class ProductViewController {

    @FXML
    private TilePane productGridContainer;

    @FXML
    public void initialize() {
        // Tạo thử 8 ô sản phẩm
        for (int i = 1; i <= 12; i++) {
            // Tạo một cái khung VBox cho từng sản phẩm
            VBox productCard = new VBox();
            productCard.setPrefSize(180, 250); // Kích thước mỗi ô
            productCard.setStyle("-fx-background-color: white; " +
                    "-fx-border-color: #ccc; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            productCard.setAlignment(Pos.CENTER);
            //căn lề các thành phần con (như ảnh, tên sản phẩm, giá tiền) vào chính giữa cái khung VBox

            // Tạo một cái hình chữ nhật giả làm ảnh
            Rectangle mockImage = new Rectangle(150, 150);
            mockImage.setFill(Color.LIGHTSTEELBLUE);
            mockImage.setArcWidth(15); // Bo góc cho ảnh đẹp hơn
            mockImage.setArcHeight(15);

            // Thêm chữ để phân biệt
            Label label = new Label("Sản phẩm " + i);
            label.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

            // Tạo thêm giá tiền cho giống thật
            Label priceLabel = new Label("Giá khởi điểm : " + (i * 100) + "k VNĐ");
            priceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            // tạo thêm phí tham gia
            Label participationFee = new Label("Phí tham gia : 100k VNĐ");
            participationFee.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            // --- ĐÂY LÀ PHẦN QUAN TRỌNG ĐẠI ĐANG THIẾU ---

            // Bước A: Thêm ảnh và các Label vào trong khung VBox
            productCard.getChildren().addAll(mockImage, label, priceLabel, participationFee);

            // Bước B: Thêm cả cái khung VBox đã hoàn thiện vào lưới TilePane (cái lưới lớn trên màn hình)
            productGridContainer.getChildren().add(productCard);

        }
    }

}