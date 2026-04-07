package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

public class ProductViewController {

    @FXML
    private TilePane productGridContainer;

    @FXML
    public void initialize() {
        // Tạo thử 8 ô sản phẩm
        for (int i = 1; i <= 8; i++) {
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

            // Thêm chữ để phân biệt
            Label label = new Label("Sản phẩm " + i);
            label.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

            productCard.getChildren().addAll(mockImage, label);
            // them anh va label

            productGridContainer.getChildren().add(productCard);
            // Thêm cái thẻ vừa tạo vào TilePane
        }
    }
}