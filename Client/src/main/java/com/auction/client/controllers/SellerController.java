package com.auction.client.controllers;

import com.auction.client.model.ItemDTO;
import com.auction.client.util.BaseController;
import com.auction.client.util.ItemCard; // Import lớp vừa tạo
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
            ItemCard card = new ItemCard(newItem, 180, 120);
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
