package com.auction.client.controllers;

import com.auction.client.model.dto.ElectronicsDTO;
import com.auction.client.model.dto.ArtDTO;
import com.auction.client.model.dto.VehicleDTO;
import com.auction.client.model.dto.ItemDTO;
import com.auction.client.util.ItemCard; // Import lớp vừa tạo
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import java.io.File;

import javafx.collections.FXCollections;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable; // Để dùng được implements Initializable [cite: 37]

public class SellerController extends BaseController implements Initializable {

    @FXML private TextField txtItemName, txtStartingPrice, txtPriceStep, txtTimeStart, txtTimeEnd;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName;
    @FXML private DatePicker dateStart, dateEnd;
    @FXML private TilePane inventoryGrid;
    @FXML private ComboBox<String> cbCategory;
    private String currentImagePath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Nạp danh sách phân loại
        cbCategory.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));

        // 2. (Tùy chọn) Đặt giá trị mặc định để tránh bị null
        cbCategory.getSelectionModel().selectFirst();

        // Giữ lại các phần khởi tạo ảnh hoặc bảng cũ của Đại ở đây...
    }

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
        String category = cbCategory.getValue(); 

        // Thu thập dữ liệu chung
        String name = txtItemName.getText().trim();
        double price = Double.parseDouble(txtStartingPrice.getText().trim());
        double step = Double.parseDouble(txtPriceStep.getText().trim());
        String desc = txtDescription.getText().trim();
        String start = (dateStart.getValue() != null) ? dateStart.getValue().toString() + " " + txtTimeStart.getText() : "";
        String end = (dateEnd.getValue() != null) ? dateEnd.getValue().toString() + " " + txtTimeEnd.getText() : "";

        // TÍNH ĐA HÌNH: Khởi tạo rỗng đúng lớp con
        ItemDTO item;
        if ("Electronics".equals(category)) {
            item = new ElectronicsDTO();
        } else if ("Art".equals(category)) {
            item = new ArtDTO();
        } else {
            item = new VehicleDTO();
        }

        // Bơm dữ liệu chung bằng Setter (Sạch sẽ, dễ đọc, dễ bảo trì)
        item.setName(name);
        item.setStartingPrice(price);
        item.setPriceStep(step);
        item.setDescription(desc);
        item.setStartTime(start);
        item.setEndTime(end);
        item.setImagePath(currentImagePath);

        return item;
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
