package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.google.gson.JsonObject;

import java.util.List;

import com.auction.client.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminController extends BaseController {

    @FXML private TableView<Object> tableData; // Dùng Object để hỗ trợ Đa hình (hiện cả User hoặc Item)
    @FXML private Label lblTitle;
    @FXML private TextField txtSearch;

    // Danh sách dữ liệu giả định để test giao diện
    private ObservableList<Object> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Mặc định khi mở lên sẽ hiển thị quản lý người dùng
        showUserManagement();
    }

    @FXML
    private void showUserManagement() {
        lblTitle.setText("Quản lý Người dùng");
        tableData.getColumns().clear();

        // Định nghĩa các cột cho bảng User
        TableColumn<Object, String> colUser = new TableColumn<>("Tên đăng nhập");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Object, String> colRole = new TableColumn<>("Vai trò");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<Object, Boolean> colStatus = new TableColumn<>("Bị khóa");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("locked"));

        tableData.getColumns().addAll(colUser, colRole, colStatus);

        // Giả sử lấy dữ liệu từ Server (Tuần 9 sẽ thay bằng Socket)
        // tableData.setItems(userDataFromServer);
    }

    @FXML
    private void showItemManagement() {
        lblTitle.setText("Quản lý Sản phẩm");
        tableData.getColumns().clear();

        // Định nghĩa các cột cho bảng ItemDTO
        TableColumn<Object, String> colName = new TableColumn<>("Tên sản phẩm");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Object, Double> colPrice = new TableColumn<>("Giá khởi điểm");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        tableData.getColumns().addAll(colName, colPrice);
    }

    @FXML
    private void handleLockUnlock() {
        // 1. Lấy đối tượng đang chọn trong bảng
        Object selected = tableData.getSelectionModel().getSelectedItem();

        // 2. Kiểm tra tính hợp lệ (Validation)
        if (selected == null) {
            showAlert("Thông báo", "Đại ơi, hãy chọn một người dùng để Khóa/Mở khóa nhé!");
            return;
        }

        // 3. Sử dụng ĐA HÌNH và Kiểm tra kiểu (Instanceof)
        if (selected instanceof User) {
            User u = (User) selected;

            // Đảo ngược trạng thái khóa (Nếu đang khóa thì mở, nếu đang mở thì khóa)
            boolean currentStatus = u.isLocked();
            u.setLocked(!currentStatus);

            // 4. HIỂN THỊ THÔNG BÁO (Tái sử dụng BaseController)
            String action = u.isLocked() ? "KHÓA" : "MỞ KHÓA";
            System.out.println("Đã gửi lệnh " + action + " cho User: " + u.getUsername());

            // Cập nhật lại TableView để thấy thay đổi ngay lập tức
            tableData.refresh();

            showAlert("Thành công", "Đã " + action + " tài khoản " + u.getUsername() + " thành công!");

        } else {
            // Nếu đang ở tab Sản phẩm mà bấm nút này thì báo lỗi
            showAlert("Lỗi", "Chức năng Khóa chỉ dành cho Người dùng, không dành cho Sản phẩm!");
        }
    }

    @FXML
    private void handleDelete() {
        Object selected = tableData.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một dòng để xóa!");
            return;
        }

        // Sử dụng instanceof (OOP) để kiểm tra loại đối tượng trước khi xử lý
        if (selected instanceof User) {
            User u = (User) selected;
            System.out.println("Lệnh xóa User gửi lên Server: " + u.getUsername());
        } else if (selected instanceof ItemDTO) {
            ItemDTO item = (ItemDTO) selected;
            System.out.println("Lệnh xóa Item gửi lên Server: " + item.getName());
        }

        // Sau khi xóa thành công sẽ cập nhật lại TableView
    }

    @FXML private TableView<UserDTO> tableUsers;

    private void loadUsers() {
        AuctionFacade.getInstance().getAllUsers(new ApiCallback<List<UserDTO>>() {
            @Override
            public void onSuccess(List<UserDTO> users) {
                tableUsers.getItems().setAll(users);
            }
            @Override public void onError(String err) { showAlert("Lỗi", err); }
        });
    }

    // Gắn hàm này vào một nút "Khóa tài khoản" trên giao diện Admin
    @FXML
    private void handleBanUser() {
        UserDTO selectedUser = tableUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Lỗi", "Hãy chọn một người dùng từ bảng!");
            return;
        }

        AuctionFacade.getInstance().updateUserStatus(selectedUser.getId(), "ban", new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                showAlert("Thành công", "Đã khóa tài khoản " + selectedUser.getUsername());
                loadUsers(); // Tải lại bảng
            }
            @Override public void onError(String err) { showAlert("Lỗi", err); }
        });
    }
    @FXML private TableView<ItemDTO> tableItems;

    @FXML
    private void handleDeleteItem() {
        ItemDTO selectedItem = tableItems.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        AuctionFacade.getInstance().adminDeleteItem(selectedItem.getId(), new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                showAlert("Thành công", "Đã gỡ sản phẩm khỏi hệ thống!");
                // Gọi lại hàm loadItems() của ông để refresh bảng
            }
            @Override public void onError(String err) { showAlert("Lỗi", err); }
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AuctionFacade.getInstance().logout(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                System.out.println("Server đã hủy Session.");
                // Dùng switchScene từ BaseController cha
                switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500);
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("Lỗi đăng xuất Server: " + errorMessage);
                // Dù Server lỗi (mất mạng), vẫn ép Client về trang Login cho an toàn
                switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500);
            }
        });
    }
}