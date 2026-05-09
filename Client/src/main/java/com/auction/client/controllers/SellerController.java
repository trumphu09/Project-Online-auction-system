package com.auction.client.controllers;

import com.auction.client.model.dto.ElectronicsDTO;
import com.auction.client.model.dto.ArtDTO;
import com.auction.client.model.dto.VehicleDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SellerController extends BaseController implements Initializable {

    @FXML private TextField txtItemName, txtStartingPrice, txtPriceStep, txtTimeStart, txtTimeEnd;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName;
    @FXML private DatePicker dateStart, dateEnd;
    @FXML private TilePane inventoryGrid;
    @FXML private ComboBox<String> cbCategory;
    @FXML private VBox dynamicFieldsContainer;
    // Khai báo các Label thống kê (Đảm bảo bên SellerView.fxml đã có fx:id này)
    @FXML private Label lblRating;
    @FXML private Label lblSaleCount;
    @FXML private Label lblAccountBalance;
    
    // Khai báo TextField tìm kiếm
    @FXML private TextField txtSearch;
    private List<ItemDTO> allItems = new ArrayList<>(); // Lưu tạm để search nhanh

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
        loadUserProfile();
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
                        clearFields();
                        showAlert("Thành công", "Đã ném sản phẩm lên sàn đấu giá thành công!");
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
            v.setCondition(getTextFromField("txtCondition")); 
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
        item.setName(txtItemName.getText());
        try {
            item.setStartingPrice(Double.parseDouble(txtStartingPrice.getText()));
            item.setPriceStep(Double.parseDouble(txtPriceStep.getText()));
            
            // --- ĐOẠN CODE BỔ SUNG: LẤY THỜI GIAN ĐẤU GIÁ ---
            if (dateStart.getValue() != null) {
                item.setStartTime(dateStart.getValue().toString() + " " + txtTimeStart.getText().trim() + ":00");
            }
            if (dateEnd.getValue() != null) {
                // Sẽ tạo ra chuỗi chuẩn SQL: "2024-05-15 20:00:00"
                item.setEndTime(dateEnd.getValue().toString() + " " + txtTimeEnd.getText().trim() + ":00");
            }
            // ------------------------------------------------
            
        } catch (NumberFormatException ignored) {}
        
        item.setCategory(key); 
        item.setDescription(txtDescription.getText());
        item.setImagePath(currentImagePath);
        item.setBase64Image(this.currentBase64Image); 
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
        currentBase64Image = null; 
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

    @FXML
    public void loadSellerInventory() {
        AuctionFacade.getInstance().getMyItems(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> {
                    if (items != null) {
                        for (ItemDTO item : items) {
                            ItemCard card = new ItemCard(item, 180, 150, false);
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

    @FXML
    private void handleSearch() {
        if (txtSearch == null) return;
        String query = txtSearch.getText().toLowerCase();
        List<ItemDTO> filtered = allItems.stream()
                .filter(i -> i.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
        renderGrid(filtered);
    }

    private void renderGrid(List<ItemDTO> items) {
        inventoryGrid.getChildren().clear(); 
        if (items != null) {
            for (ItemDTO item : items) {
                // ItemCard giờ sẽ tự lấy thông tin Electronics/Vehicle/Art để hiển thị
                ItemCard card = new ItemCard(item, 200, 220); 
                
                // Gắn sự kiện click để vẫn mở được popup chi tiết/sửa
                card.setOnMouseClicked(event -> showItemDetailPopup(item));
                card.setCursor(javafx.scene.Cursor.HAND);
                
                inventoryGrid.getChildren().add(card);
            }
        }
    }
    private void showItemDetailPopup(ItemDTO item) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết sản phẩm");
        dialog.setHeaderText(item.getName());

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10; -fx-font-size: 14px;");

        // Thông tin chung từ bảng items và auctions
        content.getChildren().addAll(
            new Label("Mô tả: " + item.getDescription()),
            new Label("Giá khởi điểm: " + item.getStartingPrice() + " VNĐ"),
            new Label("Giá đấu cao nhất: " + item.getCurrentMaxPrice() + " VNĐ"),
            new Label("ID Người đấu giá cao nhất: " + item.getHighestBidderId()),
            new Label("Trạng thái phiên đấu: " + (item.getStatus() != null ? item.getStatus() : "Chưa cập nhật")),
            new Separator()
        );

        // Thông tin riêng từ các bảng con
        if (item instanceof ElectronicsDTO) {
            content.getChildren().add(new Label("Thời gian bảo hành: " + ((ElectronicsDTO) item).getWarrantyMonths() + " tháng"));
        } else if (item instanceof VehicleDTO) {
            VehicleDTO v = (VehicleDTO) item;
            content.getChildren().addAll(
                new Label("Thương hiệu: " + v.getBrand()),
                new Label("Số KM đã đi: " + v.getMileage() + " km"),
                new Label("Tình trạng xe: " + (v.getCondition() != null ? v.getCondition() : "Không rõ"))
            );
        } else if (item instanceof ArtDTO) {
            ArtDTO a = (ArtDTO) item;
            content.getChildren().addAll(
                new Label("Họa sĩ: " + a.getArtist()),
                new Label("Năm sáng tác: " + a.getCreationYear()),
                new Label("Chất liệu: " + a.getMaterial())
            );
        }

        dialog.getDialogPane().setContent(content);

        // Thêm nút Chỉnh sửa và Đóng
        ButtonType editBtn = new ButtonType("Chỉnh sửa", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(editBtn, closeBtn);

        dialog.setResultConverter(btn -> {
            if (btn == editBtn) {
                openEditDialog(item); // Mở hộp thoại chỉnh sửa
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    private void openEditDialog(ItemDTO item) {
        Dialog<ItemDTO> editDialog = new Dialog<>();
        editDialog.setTitle("Chỉnh sửa: " + item.getName());

        // Các trường nhập liệu chung
        TextField nameField = new TextField(item.getName());
        TextArea descField = new TextArea(item.getDescription());
        descField.setPrefRowCount(3);
        TextField priceField = new TextField(String.valueOf(item.getStartingPrice()));

        VBox form = new VBox(10);
        form.setStyle("-fx-padding: 10;");
        form.getChildren().addAll(
            new Label("Tên SP:"), nameField, 
            new Label("Mô tả:"), descField, 
            new Label("Giá khởi điểm:"), priceField,
            new Separator()
        );

        // Các trường nhập liệu riêng tùy loại
        TextField specificField1 = new TextField();
        TextField specificField2 = new TextField();
        TextField specificField3 = new TextField();

        if (item instanceof ArtDTO) {
            ArtDTO art = (ArtDTO) item;
            specificField1.setText(art.getArtist());
            specificField2.setText(String.valueOf(art.getCreationYear()));
            specificField3.setText(art.getMaterial());
            form.getChildren().addAll(
                new Label("Họa sĩ:"), specificField1,
                new Label("Năm sáng tác:"), specificField2,
                new Label("Chất liệu:"), specificField3
            );
        } else if (item instanceof ElectronicsDTO) {
            ElectronicsDTO elec = (ElectronicsDTO) item;
            specificField1.setText(String.valueOf(elec.getWarrantyMonths()));
            form.getChildren().addAll(
                new Label("Thời gian bảo hành (tháng):"), specificField1
            );
        } else if (item instanceof VehicleDTO) {
            VehicleDTO v = (VehicleDTO) item;
            specificField1.setText(v.getBrand());
            specificField2.setText(String.valueOf(v.getMileage()));
            specificField3.setText(v.getCondition() != null ? v.getCondition() : "");
            form.getChildren().addAll(
                new Label("Thương hiệu:"), specificField1,
                new Label("Số KM đã đi:"), specificField2,
                new Label("Tình trạng:"), specificField3
            );
        }

        editDialog.getDialogPane().setContent(form);
        ButtonType btnSave = new ButtonType("Lưu thay đổi", ButtonBar.ButtonData.OK_DONE);
        editDialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        editDialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                // Cập nhật thông tin chung vào DTO
                item.setName(nameField.getText());
                item.setDescription(descField.getText());
                try { item.setStartingPrice(Double.parseDouble(priceField.getText())); } catch (Exception ignored) {}

                // Cập nhật thông tin riêng vào DTO
                if (item instanceof ArtDTO) {
                    ArtDTO art = (ArtDTO) item;
                    art.setArtist(specificField1.getText());
                    try { art.setCreationYear(Integer.parseInt(specificField2.getText())); } catch(Exception ignored){}
                    art.setMaterial(specificField3.getText());
                } else if (item instanceof ElectronicsDTO) {
                    ElectronicsDTO elec = (ElectronicsDTO) item;
                    try { elec.setWarrantyMonths(Integer.parseInt(specificField1.getText())); } catch(Exception ignored){}
                } else if (item instanceof VehicleDTO) {
                    VehicleDTO v = (VehicleDTO) item;
                    v.setBrand(specificField1.getText());
                    try { v.setMileage(Integer.parseInt(specificField2.getText())); } catch(Exception ignored){}
                    v.setCondition(specificField3.getText());
                }
                
                callUpdateAPI(item);
            }
            return null;
        });
        
        editDialog.showAndWait();
    }

    private void callUpdateAPI(ItemDTO item) {
        // Lưu ý: Cần tạo sẵn phương thức updateItem(item, callback) trong AuctionFacade
        AuctionFacade.getInstance().updateItem(item, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Platform.runLater(() -> {
                    showAlert("Thành công", "Đã cập nhật sản phẩm thành công!");
                    loadSellerInventory(); // Load lại lưới đồ để hiển thị thông tin mới nhất
                });
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> showAlert("Lỗi cập nhật", errorMessage));
            }
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AuctionFacade.getInstance().logout(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Platform.runLater(() -> switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500));
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500));
            }
        });
    }

    private void loadUserProfile() {
        AuctionFacade.getInstance().getUserProfile(new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO user) {
                Platform.runLater(() -> {
                    if (user != null) {
                        // Đổ dữ liệu vào các nhãn trên màn hình bên phải
                        lblAccountBalance.setText(String.format("%,.0f VNĐ", user.getBalance()));
                        lblRating.setText(String.format("%.1f / 5.0", user.getTotalRating()));
                        lblSaleCount.setText(user.getSaleCount() + " sản phẩm");
                    }
                });
            }
            @Override
            public void onError(String msg) { /* Handle error */ }
        });
    }
}