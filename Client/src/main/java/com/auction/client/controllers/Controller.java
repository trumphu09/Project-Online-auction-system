package com.auction.client.controllers;

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


public class Controller implements Initializable {

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
        // Link ảnh URL của bạn
        String imageUrl = "https://image3.luatvietnam.vn/uploaded/images/original/2024/06/08/dau-gia-tai-san-la-gi_0806112057.jpg";

        // Thiết lập CSS thông qua code Java
        // Lưu ý: Phải bao quanh URL bằng dấu nháy đơn trong chuỗi CSS
        leftPane.setStyle(
                "-fx-background-image: url('" + imageUrl + "'); " +
                        "-fx-background-size: cover; " +
                        "-fx-background-position: center;"
        );
    }

    // Giả lập Database lưu danh sách người dùng
    static List<User> userDatabase = new ArrayList<>();

    @FXML
    private void handleRegister(ActionEvent event) {
        // chuyen sang giao dien Register
        try {
            // 1. Tải file FXML của màn hình đăng ký
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Register.fxml"));
            Parent registerRoot = loader.load();

            // 2. Lấy Stage (cửa sổ) hiện tại từ sự kiện Click chuột
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Tạo Scene mới với màn hình đăng ký
            Scene registerScene = new Scene(registerRoot);

            // 4. Đặt Scene mới lên Stage và hiển thị
            stage.setScene(registerScene);
            stage.setTitle("Đăng ký tài khoản");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Không tìm thấy file Register.fxml!");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String role = "";

        if (rbBidder.isSelected()) {
            role = "BIDDER";
        } else {
            role = "SELLER";
        }
        // code sau này dùng để đăng nhập vào BIDDER hoặc SELLER

        boolean isValid = false;
        for (User u : userDatabase) {
            // 1. Tìm đúng tên người dùng trước
            if (u.getUsername().equals(user)) {
                isValid = true; // Đã tìm thấy tài khoản tồn tại

                // 2. Kiểm tra mật khẩu của người đó
                if (u.getPassword().equals(pass)) {

                    // 3. Kiểm tra vai trò
                    if (u.getRole().equals(role)) {
                        if (role.equals("BIDDER")) {
                            showAlert("Thông báo", "Chào mừng Người mua!");
                            navigateToBidderDashboard(event);
                        } else {
                            showAlert("Thông báo", "Chào mừng Người bán!");
                            navigateToSellerDashboard(event);
                        }
                        return; // Thoát hàm vì đã thành công
                    } else {
                        // Đúng pass nhưng sai Role (người bán cố vào người mua)
                        showAlert("Thông báo", "Tài khoản không có quyền truy cập vai trò này!");
                        return;
                    }

                } else {
                    // Đúng tên nhưng sai mật khẩu
                    showAlert("Thông báo", "Sai mật khẩu!");
                    return;
                }
            }
        }

// 4. Nếu chạy hết vòng lặp mà isValid vẫn là false
        if (!isValid) {
            showAlert("Thông báo", "Tài khoản chưa tồn tại!");
        }
    }

    private void navigateToBidderDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Product.fxml"));
//  Khởi tạo đối tượng Parent bằng cách nạp (load) tệp cấu hình giao diện FXML từ tài nguyên (resources).
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Chợ Đấu Giá - Người mua");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToSellerDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/SellerView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700)); // Giao diện Seller thường rộng hơn để quản lý
            stage.setTitle("Quản lý tài sản - Người bán");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
// có thể dùng DIP và OCP để codetrong handlelogin chỉ cần làm nhiệm vụ và dễ dàng mở rộng sau này


