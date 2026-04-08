package org.example.controller;
import javafx.scene.control.RadioButton;
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
//        String role = "";
//
//        if (rbBidder.isSelected()) {
//            role = "BIDDER";
//        } else {
//            role = "SELLER";
//        }
//  code sau này dùng để đăng nhập vào BIDDER hoặc SELLER

        boolean isValid = false;
        for (User u : userDatabase){
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                 showAlert("Thông báo", "Đăng nhập thành công!");
                 isValid = true;
                try {
                    // 1. Nạp file giao diện ProductView
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Product.fxml"));
                    Parent root = loader.load();

                    // 2. Lấy cửa sổ hiện tại (Stage) từ cái sự kiện click nút Login
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    // 3. Đặt giao diện mới vào và hiển thị
                    Scene scene = new Scene(root, 900, 600);
                    stage.setScene(scene);
                    stage.setTitle("Hệ thống Đấu giá - Danh sách sản phẩm");
                    stage.centerOnScreen(); // Đưa cửa sổ ra giữa màn hình
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Lỗi", "Không thể tải màn hình sản phẩm!");
                }
                return;

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
// có thể dùng DIP và OCP để codetrong handlelogin chỉ cần làm nhiệm vụ và dễ dàng mở rộng sau này


//private void navigateToBidderDashboard(ActionEvent event) {
//    try {
//        Parent root = FXMLLoader.load(getClass().getResource("/view/Product.fxml"));
//Khởi tạo đối tượng Parent bằng cách nạp (load) tệp cấu hình giao diện FXML từ tài nguyên (resources).
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root, 900, 600));
//        stage.setTitle("Chợ Đấu Giá - Người mua");
//        stage.show();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//}
// Cac ham dung de login
//private void navigateToSellerDashboard(ActionEvent event) {
//    try {
//        // Giả sử bạn đã có file SellerView.fxml
//        Parent root = FXMLLoader.load(getClass().getResource("/view/SellerView.fxml"));
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.setScene(new Scene(root, 1000, 700)); // Giao diện Seller thường rộng hơn để quản lý
//        stage.setTitle("Quản lý tài sản - Người bán");
//        stage.show();
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//}