package com.auction.client.controllers;

import com.auction.client.model.ItemDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import com.auction.client.model.ItemDTO;// Import lớp vừa tạo
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;

public class SellerController extends BaseController {

    @FXML private TextField txtItemName, txtStartingPrice, txtPriceStep, txtTimeStart, txtTimeEnd;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName;
    @FXML private DatePicker dateStart, dateEnd;
    @FXML private TilePane inventoryGrid;

    private String currentImagePath = "";

    @FXML
    private void handlePostItem(ActionEvent event) {
        try {
            // TÍNH TRỪU TƯỢNG (Abstraction): Giấu việc đóng gói dữ liệu vào hàm riêng
            ItemDTO newItem = packData();

            // KIỂM TRA ĐÓNG GÓI (Dùng hàm của BaseController)
            if (newItem.getName().isEmpty() || newItem.getStartingPrice() <= 0) {
                showAlert("Lỗi", "Đại ơi, nhập tên và giá hợp lệ nhé!");
                return;
            }

            // HIỂN THỊ: Cực kỳ ngắn gọn nhờ lớp ItemCard
            ItemCard card = new ItemCard(newItem);
            inventoryGrid.getChildren().add(card);

            clearFields();
            showAlert("Thành công", "Đã đăng sản phẩm thành công!");

        } catch (Exception e) {
            showAlert("Lỗi", "Có gì đó sai sai: " + e.getMessage());
        }
    }

    // Hàm đóng gói dữ liệu (Vẫn nên có để handlePostItem sạch sẽ)
    private ItemDTO packData() {
        String fullStart = (dateStart.getValue() != null) ? dateStart.getValue().toString() + " " + txtTimeStart.getText() : "";
        String fullEnd = (dateEnd.getValue() != null) ? dateEnd.getValue().toString() + " " + txtTimeEnd.getText() : "";

        return new ItemDTO(
                txtItemName.getText().trim(),
                Double.parseDouble(txtStartingPrice.getText()),
                Double.parseDouble(txtPriceStep.getText()),
                txtDescription.getText().trim(),
                currentImagePath,
                1, // Giả lập SellerID
                fullStart,
                fullEnd
        );
    }

    private void clearFields() {
        txtItemName.clear();
        txtStartingPrice.clear();
        txtPriceStep.clear();
        txtDescription.clear();
        dateStart.setValue(null);
        dateEnd.setValue(null);
        lblFileName.setText("Chưa chọn ảnh");
        currentImagePath = "";
    }

    @FXML
    private void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            currentImagePath = selectedFile.getAbsolutePath();
            lblFileName.setText(selectedFile.getName());
        }
    }
}

// TÍNH KẾ THỪA: Thừa hưởng mọi thứ của VBox
class ItemCard extends VBox {

    public ItemCard(ItemDTO item) {
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
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // 2. Thông tin
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label priceLabel = new Label("Giá: " + String.format("%,.0f", item.getStartingPrice()) + " VNĐ");

        // Đóng gói các thành phần vào chính nó (this)
        this.getChildren().addAll(imageView, nameLabel, priceLabel);

        // Có thể thêm hiệu ứng di chuột tại đây để dùng chung cho cả App
        this.setOnMouseEntered(e -> this.setStyle(this.getStyle() + "-fx-border-color: #2196F3;"));
        this.setOnMouseExited(e -> this.setStyle(this.getStyle().replace("-fx-border-color: #2196F3;", "-fx-border-color: #ddd;")));
    }
}