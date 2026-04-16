package com.auction.client.controllers;

import com.auction.client.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import static com.auction.client.controllers.Controller.userDatabase;

// KẾ THỪA: Thừa hưởng mọi "phép thuật" từ BaseController
public class RegisterController extends BaseController {

    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;

    // TÍNH ĐÓNG GÓI: Hàm xử lý nội bộ, che giấu logic đăng ký phức tạp
    private void processSignUp(String role) {
        String user = regUsername.getText();
        String pass = regPassword.getText();

        if (!user.endsWith("@gmail.com")) {
            showAlert("Thông báo", "Tên đăng nhập phải là tài khoản Gmail!");
            return;
        }

        for (User u : userDatabase) {
            if (u.getUsername().equals(user)) {
                showAlert("Thông báo", "Tài khoản đã tồn tại!");
                return;
            }
        }

        userDatabase.add(new User(user, pass, role));

        // ĐA HÌNH (nhẹ): Tùy biến thông báo dựa trên tham số truyền vào
        String message = "Đã tạo tài khoản " + (role.equals("SELLER") ? "Người Bán" : "Người Mua") + " thành công!";
        showAlert("Thành công", message + " Quay trở lại màn hình Login để đăng nhập.");

        regUsername.clear();
        regPassword.clear();
    }

    @FXML
    private void handleSignUpSeller(ActionEvent event) {
        processSignUp("SELLER");
    }

    @FXML
    private void handleSignUpBidder(ActionEvent event) {
        processSignUp("BIDDER");
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        // TÁI SỬ DỤNG: Dùng hàm của lớp cha, không cần try-catch rườm rà ở đây nữa
        switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500);
    }
}