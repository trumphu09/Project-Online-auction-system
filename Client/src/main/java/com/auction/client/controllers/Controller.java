package com.auction.client.controllers;

import com.auction.client.model.Admin;
import com.auction.client.util.BaseController;
import javafx.scene.control.RadioButton;
import com.auction.client.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;

public class Controller extends BaseController implements Initializable { // KẾ THỪA TỪ BaseController

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private RadioButton rbBidder;
    @FXML
    private StackPane leftPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String imageUrl = "https://image3.luatvietnam.vn/uploaded/images/original/2024/06/08/dau-gia-tai-san-la-gi_0806112057.jpg";
        leftPane.setStyle("-fx-background-image: url('" + imageUrl + "'); " +
                "-fx-background-size: cover; -fx-background-position: center;");
        userDatabase.add(new Admin("pdai@gmail.com", "12345"));
    }
    // ham khoi tao anh cho left

    static List<User> userDatabase = new ArrayList<>();

    @FXML
    private void handleRegister(ActionEvent event) {
        // Dùng luôn hàm của cha, không cần viết lại try-catch rườm rà
        switchScene(event, "/view/Register.fxml", "Đăng ký tài khoản", 800, 500);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        // Lấy RadioButton đang được chọn trong nhóm
        RadioButton selectedRB = (RadioButton) rbBidder.getToggleGroup().getSelectedToggle();
        String role = selectedRB.getText(); // Trả về "BIDDER", "SELLER" hoặc "ADMIN" [cite: 22]

        for (User u : userDatabase) {
            if (u.getUsername().equals(user)) {
                if (u.getPassword().equals(pass)) {
                    if (u.getRole().equals(role)) {
                        // Kiểm tra xem tài khoản có bị khóa không (Tuần 8: Xử lý ngoại lệ/Trạng thái) [cite: 73, 82]
                        if (u.isLocked()) {
                            showAlert("Lỗi", "Tài khoản của bạn đã bị Admin khóa!");
                            return;
                        }

                        showAlert("Thông báo", "Đăng nhập thành công với vai trò " + role);

                        // SỬA LẠI PHẦN CHUYỂN CẢNH Ở ĐÂY:
                        if (role.equals("ADMIN")) {
                            switchScene(event, "/view/AdminView.fxml", "Hệ thống Quản trị", 900, 600);  //[cite:21, 130] ds phien dau gia cho tuong lai
                        } else if (role.equals("BIDDER")) {
                            switchScene(event, "/view/Product.fxml", "Chợ Đấu Giá", 800, 500);          //[cite:129]
                        } else {
                            switchScene(event, "/view/SellerView.fxml", "Quản lý tài sản", 1000, 700);  //[cite:132]
                        }
                        return;
                    } else {
                        showAlert("Lỗi", "Sai vai trò truy cập!");
                        return;
                    }
                } else {
                    showAlert("Lỗi", "Sai mật khẩu!");
                    return;
                }
            }
        }
        showAlert("Lỗi", "Tài khoản không tồn tại!");
    }
}