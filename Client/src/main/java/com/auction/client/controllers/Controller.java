package com.auction.client.controllers;

import com.auction.client.util.BaseController;
import javafx.scene.control.RadioButton;
import com.auction.client.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.event.Event;

public class Controller extends BaseController implements Initializable { // KẾ THỪA TỪ BaseController

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private RadioButton rbBidder;
    @FXML private StackPane leftPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String imageUrl = "https://image3.luatvietnam.vn/uploaded/images/original/2024/06/08/dau-gia-tai-san-la-gi_0806112057.jpg";
        leftPane.setStyle("-fx-background-image: url('" + imageUrl + "'); " +
                "-fx-background-size: cover; -fx-background-position: center;");
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
        String role = rbBidder.isSelected() ? "BIDDER" : "SELLER";

        for (User u : userDatabase) {

            if (u.getUsername().equals(user)) {
                if (u.getPassword().equals(pass)) {
                    if (u.getRole().equals(role)) {
                        showAlert("Thông báo", "Chào mừng " + (role.equals("BIDDER") ? "Người mua!" : "Người bán!"));

                        // Chuyển màn hình cực gọn
                        if (role.equals("BIDDER")) {
                            switchScene(event, "/view/Product.fxml", "Chợ Đấu Giá", 800, 500);
                        } else {
                            switchScene(event, "/view/SellerView.fxml", "Quản lý tài sản", 1000, 700);
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

    // Các hàm navigate cũ ĐÃ BỊ XÓA vì switchScene đã lo hết rồi!
}