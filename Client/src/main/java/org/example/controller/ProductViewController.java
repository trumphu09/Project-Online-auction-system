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

            // Thêm chữ để phân biệt
            Label label = new Label("Sản phẩm " + i);
            label.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

            // Add bid history button
            Button bidHistoryBtn = new Button("View Bid History");
            bidHistoryBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10 5 10;");
            bidHistoryBtn.setOnAction(event -> viewBidHistory(event, i));

            productCard.getChildren().addAll(mockImage, label, bidHistoryBtn);
            // them anh va label

            productGridContainer.getChildren().add(productCard);
            // Thêm cái thẻ vừa tạo vào TilePane
        }
    }

    private void viewBidHistory(ActionEvent event, int auctionId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BidHistoryView.fxml"));
            Parent root = loader.load();

            // Pass auction ID to controller
            BidHistoryController controller = loader.getController();
            // Note: In real implementation, you'd set the auction ID programmatically

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Bid History - Auction " + auctionId);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}