package org.example.controller;
import org.example.model.User;

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
//    private void handleRegister(ActionEvent event) {
//
//        String user = txtUsername.getText();
//        String pass = txtPassword.getText();
//
//        if (!user.endsWith("@gmail.com")) {
//            showAlert("Thông báo", "Tên đăng nhập phải là tài khoản Gmail (@gmail.com)!");
//            return; // Dừng hàm, không lưu vào database nữa
//        }
//        for (User u : userDatabase){
//            if (u.getUsername().equals(user)) {
//                showAlert("Thông báo", "Tai khoan da ton tai");
//                return;
//
//            }else if(u.getPassword().equals(pass)){
//                showAlert("Thông báo", "mat khau da ton tai");
//                return;
//            }
//
//        }
//
//        // Nếu đúng thì mới tiếp tục lưu
//        userDatabase.add(new User(user, pass));
//        showAlert("Thành công", "Đã tạo tài khoản Gmail thành công!");
//
//        // Xóa trắng ô nhập sau khi đăng ký
//        txtUsername.clear();
//        txtPassword.clear();
//    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        boolean isValid = false;
        for (User u : userDatabase){
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                 showAlert("Thông báo", "Đăng nhập thành công!");
                 isValid = true;

            }else if(u.getUsername().equals(user) && !u.getPassword().equals(pass)){
                showAlert("Thông báo", "Sai mat khau!");
                isValid = true;
            }

        }
        if(!isValid){
            showAlert("thong bao", "tai khoan chua ton tai");}
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}