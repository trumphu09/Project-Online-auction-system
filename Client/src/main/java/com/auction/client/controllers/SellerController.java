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
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SellerController extends BaseController implements Initializable {

    @FXML private TextField txtItemName, txtStartingPrice, txtPriceStep, txtTimeStart, txtTimeEnd;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName;
    @FXML private DatePicker dateStart, dateEnd;
    @FXML private TilePane inventoryGrid;
    @FXML private ComboBox<String> cbCategory;
    @FXML private VBox dynamicFieldsContainer;

    // Labels thống kê bên phải
    @FXML private Label lblRating;
    @FXML private Label lblSaleCount;
    @FXML private Label lblAccountBalance;

    // ListView lịch sử giao dịch (khai báo đúng khớp với fx:id trong SellerView.fxml)
    @FXML private ListView<String> historyList;

    // TextField tìm kiếm
    @FXML private TextField txtSearch;

    // Danh sách tạm để search nhanh không cần gọi lại server
    private List<ItemDTO> allItems = new ArrayList<>();

    // Biến lưu trữ ảnh
    private String currentImagePath = "";
    private String currentBase64Image = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cấu hình ComboBox
        cbCategory.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));

        cbCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDynamicFields(newVal);
            }
        });

        cbCategory.getSelectionModel().selectFirst();

        // --- BỔ SUNG GỌI HÀM ĐỂ HIỂN THỊ DỮ LIỆU NGAY KHI MỞ MÀN HÌNH ---
        loadUserProfile();       // Cập nhật ví, số sao và số lượng đã bán
        loadSellerInventory();   // Hiển thị danh sách sản phẩm đang bán
    }

    private void updateDynamicFields(String category) {
        dynamicFieldsContainer.getChildren().clear();
        String key = category.toUpperCase();

        if ("ELECTRONICS".equals(key)) {
            addTextField("txtWarranty", "Thời gian bảo hành (tháng)");
        } else if ("VEHICLE".equals(key)) {
            addTextField("txtBrand", "Thương hiệu xe");
            addTextField("txtMileage", "Số KM đã đi (mileage)");
            addTextField("txtCondition", "Tình trạng xe (condition state)");
        } else if ("ART".equals(key)) {
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
            // === VALIDATION CÁC TRƯỜNG BẮT BUỘC ===
            String itemName = txtItemName.getText().trim();
            if (itemName.isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập tên sản phẩm!");
                return;
            }
            
            if (txtStartingPrice.getText().trim().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập giá khởi điểm!");
                return;
            }
            
            if (txtPriceStep.getText().trim().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập bước giá!");
                return;
            }
            
            String selectedCategory = cbCategory.getValue();
            if (selectedCategory == null || selectedCategory.isEmpty()) {
                showAlert("Lỗi", "Vui lòng chọn loại sản phẩm!");
                return;
            }
            
            if (txtDescription.getText().trim().isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập mô tả sản phẩm!");
                return;
            }
            
            if (currentImagePath == null || currentImagePath.isEmpty()) {
                showAlert("Lỗi", "Vui lòng chọn hình ảnh sản phẩm!");
                return;
            }
            
            if (dateStart.getValue() == null) {
                showAlert("Lỗi", "Vui lòng chọn ngày bắt đầu đấu giá!");
                return;
            }
            
            if (dateEnd.getValue() == null) {
                showAlert("Lỗi", "Vui lòng chọn ngày kết thúc đấu giá!");
                return;
            }
            
            // Validate định dạng thời gian
            String timeStart = txtTimeStart.getText().trim();
            String timeEnd = txtTimeEnd.getText().trim();
            
            if (!timeStart.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showAlert("Lỗi", "Giờ bắt đầu phải có định dạng HH:mm (ví dụ: 08:00)!");
                return;
            }
            
            if (!timeEnd.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showAlert("Lỗi", "Giờ kết thúc phải có định dạng HH:mm (ví dụ: 20:00)!");
                return;
            }
            
            // Validate giá và bước giá là số dương
            try {
                double startingPrice = Double.parseDouble(txtStartingPrice.getText());
                double priceStep = Double.parseDouble(txtPriceStep.getText());
                
                if (startingPrice <= 0) {
                    showAlert("Lỗi", "Giá khởi điểm phải lớn hơn 0!");
                    return;
                }
                
                if (priceStep <= 0) {
                    showAlert("Lỗi", "Bước giá phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Lỗi", "Giá phải là số hợp lệ!");
                return;
            }
            
            // Validate category-specific fields
            String key = selectedCategory.toUpperCase();
            if ("ELECTRONICS".equals(key)) {
                TextField warrantyField = (TextField) dynamicFieldsContainer.lookup("#txtWarranty");
                if (warrantyField != null && warrantyField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập thời gian bảo hành (tháng)!");
                    return;
                }
            } else if ("VEHICLE".equals(key)) {
                TextField brandField = (TextField) dynamicFieldsContainer.lookup("#txtBrand");
                TextField mileageField = (TextField) dynamicFieldsContainer.lookup("#txtMileage");
                TextField conditionField = (TextField) dynamicFieldsContainer.lookup("#txtCondition");
                
                if (brandField != null && brandField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập thương hiệu xe!");
                    return;
                }
                if (mileageField != null && mileageField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập số KM đã đi!");
                    return;
                }
                if (conditionField != null && conditionField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập tình trạng xe!");
                    return;
                }
            } else if ("ART".equals(key)) {
                TextField artistField = (TextField) dynamicFieldsContainer.lookup("#txtArtist");
                TextField yearField = (TextField) dynamicFieldsContainer.lookup("#txtYear");
                TextField materialField = (TextField) dynamicFieldsContainer.lookup("#txtMaterial");
                
                if (artistField != null && artistField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập tên họa sĩ!");
                    return;
                }
                if (yearField != null && yearField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập năm sáng tác!");
                    return;
                }
                if (materialField != null && materialField.getText().trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng nhập chất liệu!");
                    return;
                }
            }
            
            // Nếu tất cả validation đều pass, thực hiện packData và post item
            ItemDTO newItem = packData();

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
        try {
            item.setStartingPrice(Double.parseDouble(txtStartingPrice.getText()));
            item.setPriceStep(Double.parseDouble(txtPriceStep.getText()));

            if (dateStart.getValue() != null) {
                item.setStartTime(dateStart.getValue().toString() + " " + txtTimeStart.getText().trim() + ":00");
            }
            if (dateEnd.getValue() != null) {
                item.setEndTime(dateEnd.getValue().toString() + " " + txtTimeEnd.getText().trim() + ":00");
            }
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
                    // Xóa grid cũ và cập nhật danh sách tạm
                    inventoryGrid.getChildren().clear();
                    allItems.clear();
                    if (items != null) {
                        allItems.addAll(items);
                    }
                    renderGrid(allItems);
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

    // FIX: truyền đúng 4 tham số — isBuyer = false vì đây là màn hình Seller
    private void renderGrid(List<ItemDTO> items) {
        inventoryGrid.getChildren().clear();
        if (items != null) {
            for (ItemDTO item : items) {
                ItemCard card = new ItemCard(item, 200, 220, false);
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

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15; -fx-font-size: 13px;");

        // Thông tin cơ bản
        VBox basicInfo = new VBox(8);
        basicInfo.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: #f9f9f9;");
        basicInfo.getChildren().addAll(
            new Label("THÔNG TIN CƠ BẢN"),
            new Label("━━━━━━━━━━━━━━━━━━━━━━"),
            new Label("Mô tả: " + item.getDescription()),
            new Label("Loại sản phẩm: " + (item.getCategory() != null ? item.getCategory() : "Không xác định")),
            new Separator(),
            new Label("THÔNG TIN GIÁ"),
            new Label("━━━━━━━━━━━━━━━━━━━━━━"),
            new Label("Giá khởi điểm: " + String.format("%,.0f VNĐ", item.getStartingPrice())),
            new Label("Bước giá tối thiểu: " + String.format("%,.0f VNĐ", item.getPriceStep())),
            new Label("Giá cao nhất hiện tại: " + String.format("%,.0f VNĐ", item.getCurrentMaxPrice())),
            new Separator(),
            new Label("THÔNG TIN THỜI GIAN"),
            new Label("━━━━━━━━━━━━━━━━━━━━━━"),
            new Label("Bắt đầu: " + (item.getStartTime() != null ? item.getStartTime() : "Chưa xác định")),
            new Label("Kết thúc: " + (item.getEndTime() != null ? item.getEndTime() : "Chưa xác định")),
            new Separator(),
            new Label("THÔNG TIN PHIÊN ĐẤU"),
            new Label("━━━━━━━━━━━━━━━━━━━━━━"),
            new Label("Người đấu giá cao nhất: ID " + item.getHighestBidderId()),
            new Label("Trạng thái: " + (item.getStatus() != null ? item.getStatus() : "Chưa cập nhật"))
        );
        
        content.getChildren().add(basicInfo);

        // Category-specific info
        if (item instanceof ElectronicsDTO) {
            ElectronicsDTO elec = (ElectronicsDTO) item;
            VBox electronicsInfo = new VBox(8);
            electronicsInfo.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: #f0f8ff;");
            electronicsInfo.getChildren().addAll(
                new Label("THÔNG TIN ĐIỆN TỬ"),
                new Label("━━━━━━━━━━━━━━━━━━━━━━"),
                new Label("Bảo hành: " + elec.getWarrantyMonths() + " tháng")
            );
            content.getChildren().add(electronicsInfo);
        } else if (item instanceof VehicleDTO) {
            VehicleDTO v = (VehicleDTO) item;
            VBox vehicleInfo = new VBox(8);
            vehicleInfo.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: #f0f8ff;");
            vehicleInfo.getChildren().addAll(
                new Label("THÔNG TIN XE CỘ"),
                new Label("━━━━━━━━━━━━━━━━━━━━━━"),
                new Label("Thương hiệu: " + v.getBrand()),
                new Label("Số KM đã đi: " + v.getMileage() + " km"),
                new Label("Tình trạng: " + (v.getCondition() != null ? v.getCondition() : "Không rõ"))
            );
            content.getChildren().add(vehicleInfo);
        } else if (item instanceof ArtDTO) {
            ArtDTO a = (ArtDTO) item;
            VBox artInfo = new VBox(8);
            artInfo.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: #f0f8ff;");
            artInfo.getChildren().addAll(
                new Label("THÔNG TIN NGHỆ THUẬT"),
                new Label("━━━━━━━━━━━━━━━━━━━━━━"),
                new Label("Họa sĩ: " + a.getArtist()),
                new Label("Năm sáng tác: " + a.getCreationYear()),
                new Label("Chất liệu: " + a.getMaterial())
            );
            content.getChildren().add(artInfo);
        }

        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(600);

        ButtonType editBtn = new ButtonType("Chỉnh sửa", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(editBtn, closeBtn);

        dialog.setResultConverter(btn -> {
            if (btn == editBtn) {
                openEditDialog(item);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void openEditDialog(ItemDTO item) {
        Dialog<ItemDTO> editDialog = new Dialog<>();
        editDialog.setTitle("Chỉnh sửa: " + item.getName());

        TextField nameField = new TextField(item.getName());
        TextArea descField = new TextArea(item.getDescription());
        descField.setPrefRowCount(3);
        TextField priceField = new TextField(String.valueOf(item.getStartingPrice()));
        TextField priceStepField = new TextField(String.valueOf(item.getPriceStep()));
        DateTimeEditor startEditor = new DateTimeEditor(item.getStartTime());
        DateTimeEditor endEditor = new DateTimeEditor(item.getEndTime());

        VBox form = new VBox(10);
        form.setStyle("-fx-padding: 10;");
        form.getChildren().addAll(
            new Label("Tên SP:"), nameField,
            new Label("Mô tả:"), descField,
            new Label("Giá khởi điểm:"), priceField,
            new Label("Bước giá:"), priceStepField,
            new Label("Thời gian bắt đầu:"), startEditor.root,
            new Label("Thời gian kết thúc:"), endEditor.root,
            new Separator()
        );

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
                item.setName(nameField.getText().trim());
                item.setDescription(descField.getText().trim());

                try {
                    item.setStartingPrice(Double.parseDouble(priceField.getText().trim()));
                } catch (Exception ignored) {}

                try {
                    item.setPriceStep(Double.parseDouble(priceStepField.getText().trim()));
                } catch (Exception ignored) {}

                item.setStartTime(startEditor.toDbString());
                item.setEndTime(endEditor.toDbString());

                callUpdateAPI(item);
            }
            return null;
        });

        editDialog.showAndWait();
    }

    private void callUpdateAPI(ItemDTO item) {
        AuctionFacade.getInstance().updateItem(item, new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Platform.runLater(() -> {
                    showAlert("Thành công", "Đã cập nhật sản phẩm thành công!");
                    loadSellerInventory();
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

    private String normalizeDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.trim().isEmpty()) return null;
        String s = dateTimeText.trim();
        // Chấp nhận: yyyy-MM-dd HH:mm hoặc yyyy-MM-dd HH:mm:ss
        if (s.length() == 16) s += ":00";
        return s;
    }

    private double parseDoubleOrDefault(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static class DateTimeEditor {
        final DatePicker datePicker = new DatePicker();
        final Spinner<Integer> hourSpinner = new Spinner<>();
        final Spinner<Integer> minuteSpinner = new Spinner<>();
        final HBox root = new HBox(8);

        DateTimeEditor(String initialValue) {
            hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0));
            minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
            hourSpinner.setEditable(true);
            minuteSpinner.setEditable(true);

            root.getChildren().addAll(datePicker, new Label("Giờ"), hourSpinner, new Label("Phút"), minuteSpinner);
            HBox.setHgrow(datePicker, Priority.ALWAYS);

            applyInitialValue(initialValue);
        }

        private void applyInitialValue(String initialValue) {
            if (initialValue == null || initialValue.trim().isEmpty()) return;

            try {
                String normalized = initialValue.trim().replace('T', ' ');
                if (normalized.length() > 19) {
                    normalized = normalized.substring(0, 19);
                }
                LocalDateTime dt = LocalDateTime.parse(normalized, DB_FORMAT);
                datePicker.setValue(dt.toLocalDate());
                hourSpinner.getValueFactory().setValue(dt.getHour());
                minuteSpinner.getValueFactory().setValue(dt.getMinute());
            } catch (Exception ignored) {
            }
        }

        String toDbString() {
            LocalDate d = datePicker.getValue();
            if (d == null) return null;

            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            LocalDateTime dt = LocalDateTime.of(d, LocalTime.of(hour, minute));
            return dt.format(DB_FORMAT);
        }
    }
}