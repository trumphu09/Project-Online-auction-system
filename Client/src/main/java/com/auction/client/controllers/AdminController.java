package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AdminController extends BaseController {

    @FXML private TableView<Object> tableData; 
    @FXML private Label lblTitle;
    @FXML private TextField txtSearch;

    @FXML
    public void initialize() {
        showUserManagement();
    }

    // ==========================================
    // 1. TAB QUẢN LÝ NGƯỜI DÙNG
    // ==========================================
    @FXML
    private void showUserManagement() {
        lblTitle.setText("Quản lý Người dùng");
        tableData.getColumns().clear();

        TableColumn<Object, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Object, String> colUser = new TableColumn<>("Tên đăng nhập");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Object, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Object, String> colRole = new TableColumn<>("Vai trò");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // ✅ THÊM: Cột trạng thái hiển thị có màu xanh/đỏ
        TableColumn<Object, Void> colStatus = new TableColumn<>("Trạng thái");
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Object rowItem = getTableRow().getItem();
                if (rowItem instanceof UserDTO) {
                    boolean active = ((UserDTO) rowItem).isActive();
                    Label lbl = new Label(active ? "✅ Hoạt động" : "🔒 Bị khóa");
                    lbl.setStyle(active
                        ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                        : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    setGraphic(lbl);
                }
            }
        });

        tableData.getColumns().addAll(colId, colUser, colEmail, colRole, colStatus);
        loadUsers();
    }
    private void loadItems() {
        AuctionFacade.getInstance().getAllItems(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> {
                    // SỬA LỖI ÉP KIỂU: Tạo list rỗng trước rồi mới addAll
                    ObservableList<Object> data = FXCollections.observableArrayList();
                    if (items != null) {
                        data.addAll(items);
                    }
                    tableData.setItems(data);
                });
            }
            @Override public void onError(String err) { Platform.runLater(() -> showAlert("Lỗi", err)); }
        });
    }

    // ==========================================
    // 2. TAB QUẢN LÝ SẢN PHẨM & AUCTION
    // ==========================================
    @FXML
    private void showItemManagement() {
        lblTitle.setText("Quản lý Sản phẩm");
        tableData.getColumns().clear();

        TableColumn<Object, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Object, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Object, Double> colPrice = new TableColumn<>("Giá khởi điểm");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        TableColumn<Object, String> colCategory = new TableColumn<>("Danh mục");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        // NÚT BẤM XEM PHIÊN ĐẤU GIÁ NẰM NGAY TRONG BẢNG
        TableColumn<Object, Void> colAuctionAction = new TableColumn<>("Phiên đấu giá");
        colAuctionAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Xem Auction");
            {
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    Object item = getTableView().getItems().get(getIndex());
                    if (item instanceof ItemDTO) {
                        showAuctionDetail((ItemDTO) item);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableData.getColumns().addAll(colId, colName, colPrice, colCategory, colAuctionAction);

        loadItems(); // Nạp dữ liệu từ Server
    }



    private void showAuctionDetail(ItemDTO item) {
        if (item.getAuctionId() <= 0) {
            showAlert("Thông báo", "Sản phẩm này chưa có phiên đấu giá.");
            return;
        }

        StringBuilder info = new StringBuilder();
        info.append("--- CHI TIẾT PHIÊN ĐẤU GIÁ ---\n\n");
        info.append("Mã phiên (ID): ").append(item.getAuctionId()).append("\n");
        info.append("Trạng thái: ").append(item.getStatus() != null ? item.getStatus() : "N/A").append("\n");
        info.append("Giá cao nhất: ").append(String.format("%,.0f VNĐ", item.getCurrentMaxPrice())).append("\n");
        info.append("ID Người đặt cao nhất: ").append(item.getHighestBidderId() > 0 ? item.getHighestBidderId() : "Chưa có").append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông tin Auction");
        alert.setHeaderText("Sản phẩm: " + item.getName());
        alert.setContentText(info.toString());
        alert.showAndWait();
    }

    // ==========================================
    // 3. CÁC NÚT CHỨC NĂNG BÊN DƯỚI
    // ==========================================
    @FXML
    private void handleLockUnlock() {
        Object selected = tableData.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Hãy chọn một người dùng để Khóa/Mở khóa!");
            return;
        }

        if (!(selected instanceof UserDTO)) {
            showAlert("Lỗi", "Chức năng này chỉ dành cho Người dùng!");
            return;
        }

        UserDTO u = (UserDTO) selected;

        // Không cho phép khóa ADMIN
        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            showAlert("Lỗi", "Không thể khóa tài khoản Admin!");
            return;
        }

        // ✅ DÙNG isActive() thay vì check role
        boolean currentlyActive = u.isActive();
        String action    = currentlyActive ? "ban" : "unban";
        String actionText = currentlyActive ? "KHÓA" : "MỞ KHÓA";
        String confirm   = currentlyActive
            ? "Bạn có chắc muốn KHÓA tài khoản [" + u.getUsername() + "]?"
            : "Bạn có chắc muốn MỞ KHÓA tài khoản [" + u.getUsername() + "]?";

        // Hỏi xác nhận trước khi thực hiện
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(confirm);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AuctionFacade.getInstance().updateUserStatus(u.getId(), action, new ApiCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        Platform.runLater(() -> {
                            showAlert("Thành công", "Đã " + actionText + " tài khoản " + u.getUsername());
                            loadUsers(); // ✅ Đúng tên hàm
                        });
                    }
                    @Override
                    public void onError(String err) {
                        Platform.runLater(() -> showAlert("Lỗi", err));
                    }
                });
            }
        });
    }

    @FXML
    private void handleDelete() {
        Object selected = tableData.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một sản phẩm để gỡ!");
            return;
        }

        if (selected instanceof ItemDTO) {
            ItemDTO item = (ItemDTO) selected;
            AuctionFacade.getInstance().adminDeleteItem(item.getId(), new ApiCallback<JsonObject>() {
                @Override
                public void onSuccess(JsonObject result) {
                    Platform.runLater(() -> {
                        showAlert("Thành công", "Đã gỡ sản phẩm khỏi hệ thống!");
                        loadItems(); // Tải lại bảng
                    });
                }
                @Override public void onError(String err) { Platform.runLater(() -> showAlert("Lỗi", err)); }
            });
        } else {
            showAlert("Lỗi", "Vui lòng dùng nút Khóa tài khoản thay vì xóa User!");
        }
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

    private void loadUsers() {
        AuctionFacade.getInstance().getAllUsers(new ApiCallback<List<UserDTO>>() {
            @Override
            public void onSuccess(List<UserDTO> users) {
                Platform.runLater(() -> {
                    // Sử dụng tableData thay vì tableUsers để khớp với FXML
                    javafx.collections.ObservableList<Object> data = javafx.collections.FXCollections.observableArrayList();
                    if (users != null) data.addAll(users);
                    tableData.setItems(data);
                });
            }
            @Override 
            public void onError(String err) { 
                Platform.runLater(() -> showAlert("Lỗi", err)); 
            }
        });
    }
}