package com.auction.client.controllers;

import com.auction.client.model.dto.ElectronicsDTO;
import com.auction.client.model.dto.ArtDTO;
import com.auction.client.model.dto.VehicleDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.auction.client.model.dto.ItemDTO;
import com.auction.client.util.ItemCard;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SellerController extends BaseController implements Initializable {

    @FXML private TextField txtItemName, txtStartingPrice, txtPriceStep, txtTimeStart, txtTimeEnd;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName;
    @FXML private DatePicker dateStart, dateEnd;
    @FXML private TilePane inventoryGrid;
    @FXML private ComboBox<String> cbCategory;
    @FXML private VBox dynamicFieldsContainer;

    // Biến lưu trữ ảnh
    private String currentImagePath = "";
    private String currentBase64Image = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbCategory.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        
        cbCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDynamicFields(newVal);
            }
        });
        
        cbCategory.getSelectionModel().selectFirst();
        loadSellerInventory();
    }

    private void updateDynamicFields(String category) {
        dynamicFieldsContainer.getChildren().clear();
        String key = category.toUpperCase();

        if ("ELECTRONICS".equals(key)) {
            addTextField("txtWarranty", "Thời gian bảo hành (tháng)");
        } 
        else if ("VEHICLE".equals(key)) {
            addTextField("txtBrand", "Thương hiệu xe");
            addTextField("txtMileage", "Số KM đã đi (mileage)");
            addTextField("txtCondition", "Tình trạng xe (condition state)");
        } 
        else if ("ART".equals(key)) {
            addTextField("txtArtist", "Tên họa sĩ");
            addTextField("txtYear", "Năm sáng tác (creation year)");
            addTextField("txtMaterial", "Chất liệu (material)");
        }
    }

    private void addTextField(String id, String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setId(id);
        dynamicFieldsContainer.getChildren().add(tf);
    }

    @FXML
    private void handlePostItem(ActionEvent event) {
        try {
            ItemDTO newItem = packData();
            if (newItem.getName() == null || newItem.getName().isEmpty() || newItem.getStartingPrice() <= 0) {
                showAlert("Lỗi", "Hãy nhập tên và giá hợp lệ nhé!");
                return;
            }

            AuctionFacade.getInstance().addItem(newItem, new ApiCallback<JsonObject>() {
                @Override
                public void onSuccess(JsonObject result) {
                    Platform.runLater(() -> {
                        ItemCard card = new ItemCard(newItem, 180, 120);
                        inventoryGrid.getChildren().add(card);
                        clearFields();
                        showAlert("Thành công", "Đã ném sản phẩm lên sàn đấu giá thành công!");
                        // Load lại cho chắc ăn để lấy hình ảnh chuẩn từ server
                        loadSellerInventory(); 
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Platform.runLater(() -> showAlert("Lỗi đăng bán", errorMessage));
                }
            });

        } catch (Exception e) {
            showAlert("Lỗi", "Có gì đó sai sai: " + e.getMessage());
        }
    }

    private ItemDTO packData() {
        String category = cbCategory.getValue();
        String key = category != null ? category.toUpperCase() : "";
        ItemDTO item;

        if ("ELECTRONICS".equals(key)) {
            ElectronicsDTO e = new ElectronicsDTO();
            e.setWarrantyMonths(getIntFromField("txtWarranty"));
            item = e;
        } else if ("VEHICLE".equals(key)) {
            VehicleDTO v = new VehicleDTO();
            v.setBrand(getTextFromField("txtBrand"));
            v.setMileage(getIntFromField("txtMileage"));
            v.setCondition(getTextFromField("txtCondition")); // Lưu ý: hàm set là setConditionState hay setCondition tùy DTO của ông
            item = v;
        } else if ("ART".equals(key)) {
            ArtDTO a = new ArtDTO();
            a.setArtist(getTextFromField("txtArtist"));
            a.setCreationYear(getIntFromField("txtYear"));
            a.setMaterial(getTextFromField("txtMaterial"));
            item = a;
        } else {
            item = new ItemDTO();
        }

        item.setName(txtItemName.getText());
        try {
            item.setStartingPrice(Double.parseDouble(txtStartingPrice.getText()));
            item.setPriceStep(Double.parseDouble(txtPriceStep.getText()));
        } catch (NumberFormatException ignored) {}
        
        item.setCategory(key); 
        item.setDescription(txtDescription.getText());
        item.setImagePath(currentImagePath);
        item.setBase64Image(this.currentBase64Image); // Sửa lỗi gán nhầm biến ở đây
        return item;
    }

    private String getTextFromField(String id) {
        TextField tf = (TextField) dynamicFieldsContainer.lookup("#" + id);
        return (tf != null) ? tf.getText() : "";
    }

    private int getIntFromField(String id) {
        String text = getTextFromField(id);
        return text.isEmpty() ? 0 : Integer.parseInt(text);
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
        currentBase64Image = null; // Xóa luôn base64
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                byte[] fileContent = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                String base64String = java.util.Base64.getEncoder().encodeToString(fileContent);
                
                this.currentBase64Image = base64String; 
                this.currentImagePath = selectedFile.getName(); 
                
                lblFileName.setText(selectedFile.getName());
            } catch (IOException e) {
                showAlert("Lỗi", "Không thể đọc file ảnh!");
            }
        }
    }

    private void loadSellerInventory() {
        AuctionFacade.getInstance().getMyItems(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> {
                    inventoryGrid.getChildren().clear(); 
                    if (items != null) {
                        for (ItemDTO item : items) {
                            ItemCard card = new ItemCard(item, 180, 150);
                            card.setOnMouseClicked(event -> showItemDetailPopup(item));
                            inventoryGrid.getChildren().add(card);
                        }
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> showAlert("Lỗi", "Không thể tải danh sách sản phẩm: " + errorMessage));
            }
        });
    }

    private void showItemDetailPopup(ItemDTO item) {
        StringBuilder detail = new StringBuilder();
        detail.append("--- THÔNG TIN CHUNG ---\n");
        detail.append("Tên: ").append(item.getName()).append("\n");
        detail.append("Giá khởi điểm: ").append(item.getStartingPrice()).append(" VNĐ\n");
        // Kiểm tra null cho ngày tạo để tránh lỗi
        if(item.getCreatedAt() != null) {
            detail.append("Ngày tạo: ").append(item.getCreatedAt()).append("\n");
        }
        detail.append("Mô tả: ").append(item.getDescription()).append("\n\n");

        detail.append("--- THÔNG TIN RIÊNG (").append(item.getCategory()).append(") ---\n");

        if (item instanceof ElectronicsDTO) {
            detail.append("Bảo hành: ").append(((ElectronicsDTO) item).getWarrantyMonths()).append(" tháng\n");
        } else if (item instanceof VehicleDTO) {
            VehicleDTO v = (VehicleDTO) item;
            detail.append("Thương hiệu: ").append(v.getBrand()).append("\n");
            detail.append("Số KM: ").append(v.getMileage()).append(" km\n");
            detail.append("Tình trạng: ").append(v.getCondition() != null ? v.getCondition() : "Không rõ").append("\n");
        } else if (item instanceof ArtDTO) {
            ArtDTO a = (ArtDTO) item;
            detail.append("Họa sĩ: ").append(a.getArtist()).append("\n");
            detail.append("Năm sáng tác: ").append(a.getCreationYear()).append("\n");
            detail.append("Chất liệu: ").append(a.getMaterial()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết sản phẩm");
        alert.setHeaderText(item.getName());
        alert.setContentText(detail.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AuctionFacade.getInstance().logout(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Platform.runLater(() -> {
                    System.out.println("Server đã hủy Session.");
                    switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500);
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    System.err.println("Lỗi đăng xuất Server: " + errorMessage);
                    switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500);
                });
            }
        });
    }
}