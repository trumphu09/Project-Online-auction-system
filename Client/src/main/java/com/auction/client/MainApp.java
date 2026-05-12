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
        Scene scene = new Scene(fxmlLoader.load(), 800, 500);

        stage.setTitle("Ứng dụng Up/Down/Reset");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
