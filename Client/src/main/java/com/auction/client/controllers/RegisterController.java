package com.auction.client.controllers;

import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController extends BaseController {

  @FXML private TextField regUsername;
  @FXML private PasswordField regPassword;

  private void processSignUp(String role) {
    String user = regUsername.getText().trim();
    String pass = regPassword.getText().trim();

    if (!user.endsWith("@gmail.com")) {
      showAlert("Thông báo", "Tên đăng nhập phải là tài khoản Gmail!");
      return;
    }

    // 🚀 GỌI API ĐĂNG KÝ THẬT TỪ FACADE
    AuctionFacade.getInstance().register(user, pass, role, new ApiCallback<JsonObject>() {
      @Override
      public void onSuccess(JsonObject result) {
        String message = "Đã tạo tài khoản " + (role.equals("SELLER") ? "Người Bán" : "Người Mua") + " thành công!";
        showAlert("Thành công", message + "\nQuay trở lại màn hình Login để đăng nhập.");

        regUsername.clear();
        regPassword.clear();
      }

      @Override
      public void onError(String errorMessage) {
        // Trùng username hoặc lỗi DB sẽ quăng ra đây
        showAlert("Đăng ký thất bại", errorMessage);
      }
    });
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
    // Chỉ giữ lại một hàm duy nhất này thôi Đại nhé
    switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500);
  }
}