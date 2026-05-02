package com.auction.client.controllers;

import com.auction.client.model.Admin;
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
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.google.gson.JsonObject;

public class LoginController extends BaseController implements Initializable { // KẾ THỪA TỪ BaseController

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
  }
  // ham khoi tao anh cho left

  @FXML
  private void handleRegister(ActionEvent event) {
    // Dùng luôn hàm của cha, không cần viết lại try-catch rườm rà
    switchScene(event, "/view/Register.fxml", "Đăng ký tài khoản", 800, 500);
  }
  @FXML
  private void handleLogin(ActionEvent event) {
    String user = txtUsername.getText().trim();
    String pass = txtPassword.getText().trim();

    if (user.isEmpty() || pass.isEmpty()) {
      showAlert("Lỗi", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
      return;
    }

    // 🚀 GỌI API ĐĂNG NHẬP THẬT TỪ FACADE
    AuctionFacade.getInstance().login(user, pass, new ApiCallback<JsonObject>() {
      @Override
      public void onSuccess(JsonObject result) {
        // Đọc Role từ JSON Server trả về (Dựa theo chuẩn file LoginAPI của nhóm bạn)
        JsonObject data = result.getAsJsonObject("data");
        String role = data.get("role").getAsString();

        showAlert("Thông báo", "Đăng nhập thành công với vai trò: " + role);

        // Dựa vào Role thật để chuyển màn hình
        if (role.equals("ADMIN")) {
          switchScene(event, "/view/AdminView.fxml", "Hệ thống Quản trị", 900, 600);
        } else if (role.equals("BIDDER")) {
          switchScene(event, "/view/Product.fxml", "Chợ Đấu Giá", 800, 500);
        } else {
          switchScene(event, "/view/SellerView.fxml", "Quản lý tài sản", 1000, 700);
        }
      }

      @Override
      public void onError(String errorMessage) {
        // Nếu sai pass hoặc Server sập, lỗi sẽ hiện ở đây
        showAlert("Đăng nhập thất bại", errorMessage);
      }
    });
  }
}