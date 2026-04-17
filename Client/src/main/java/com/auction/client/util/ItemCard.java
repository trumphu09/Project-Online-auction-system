package com.auction.client.util;

import com.auction.client.model.ItemDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

// TÍNH KẾ THỪA: Thừa hưởng mọi thứ của VBox
public class ItemCard extends VBox {

    public ItemCard(ItemDTO item, double width, double height) {
        // Cấu hình khung bên ngoài (Thay cho setStyle rườm rà)
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPrefWidth(200);
        this.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5;");

        // 1. Xử lý ảnh
        ImageView imageView = new ImageView();
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            imageView.setImage(new Image("file:" + item.getImagePath()));
        }
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        // 2. Thông tin
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label("Giá: " + String.format("%,.0f", item.getStartingPrice()) + " VNĐ");

        // Đóng gói các thành phần vào chính nó (this)
        this.getChildren().addAll(imageView, nameLabel, priceLabel);

//        // Có thể thêm hiệu ứng di chuột tại đây để dùng chung cho cả App
//        this.setOnMouseEntered(e -> this.setStyle(this.getStyle() + "-fx-border-color: #2196F3;"));
//        this.setOnMouseExited(e -> this.setStyle(this.getStyle().replace("-fx-border-color: #2196F3;", "-fx-border-color: #ddd;")));
    }
}