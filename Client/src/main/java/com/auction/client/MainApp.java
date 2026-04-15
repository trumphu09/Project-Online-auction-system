package com.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Nạp file View.fxml nằm ngay trong thư mục resources
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/view/View.fxml"));

        // Tạo Scene (Cửa sổ) với kích thước 600x400
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("Ứng dụng Up/Down/Reset");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//public class MainApp extends Application {
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        // 1. Nạp file FXML từ thư mục resources/view/
//        Parent root = FXMLLoader.load(getClass().getResource("/view/Product.fxml"));
//
//        // 2. Tạo Scene với kích thước phù hợp
//        Scene scene = new Scene(root, 900, 600);
//
//        // 3. Thiết lập tiêu đề và gắn scene vào stage
//        stage.setTitle("Hệ thống Đấu giá - Danh sách sản phẩm");
//        stage.setScene(scene);
//
//        // 4. Hiển thị cửa sổ
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}