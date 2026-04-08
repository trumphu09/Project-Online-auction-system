package org.example.controller;
import org.example.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert; // Phải import cái này
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

import static org.example.controller.Controller.userDatabase;

public class RegisterController {
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;

    // Hàm xử lý logic chung (Không cần @FXML vì nó chỉ dùng nội bộ)
    private void processSignUp(String role) {
        String user = regUsername.getText();
        String pass = regPassword.getText();

        // 1. Kiểm tra tính hợp lệ của Email
        if (!user.endsWith("@gmail.com")) {
            showAlert("Thông báo", "Tên đăng nhập phải là tài khoản Gmail (@gmail.com)!");
            return;
        }

        // 2. Kiểm tra trùng Username (Đã xóa đoạn kiểm tra trùng Password)
        for (User u : userDatabase) {
            if (u.getUsername().equals(user)) {
                showAlert("Thông báo", "Tài khoản (Username) đã tồn tại!");
                return;
            }
        }

        // 3. Nếu đúng hết thì lưu vào database kèm theo Role
        userDatabase.add(new User(user, pass, role));

        // Hiển thị thông báo linh hoạt theo Role
        if (role.equals("SELLER")) {
            showAlert("Thành công", "Đã tạo tài khoản Người Bán thành công! Quay trở lại màn hình Login để đăng nhập vào tài khoản");
        } else {
            showAlert("Thành công", "Đã tạo tài khoản Người Mua thành công! Quay trở lại màn hình Login để đăng nhập vào tài khoản");
        }

        // 4. Xóa trắng ô nhập sau khi đăng ký
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

    // Bạn phải tự viết hàm này để Controller hiểu nó là gì
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null); // Không hiển thị tiêu đề phụ
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void handleBackToLogin(ActionEvent event) {
        try {
            // 1. Tải file FXML của màn hình Login
            // Đảm bảo đường dẫn đúng với cấu trúc thư mục của bạn (thường là "/org/example/login.fxml")
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/view/View.fxml"));

            // 2. Lấy Stage hiện tại từ sự kiện Click chuột
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Tạo Scene mới với màn hình Login và đặt vào Stage
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể quay lại màn hình đăng nhập!");
        }

    }
}