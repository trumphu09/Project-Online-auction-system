package com.auction.client.controllers;

import com.auction.client.model.dto.ArtDTO;
import com.auction.client.model.dto.ItemDTO;
import com.auction.client.util.ItemCard;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.event.Event;
import java.io.IOException;

// 1. TÍNH KẾ THỨA: Kế thừa BaseController để dùng switchScene và showAlert
public class ProductViewController extends BaseController {

    @FXML
    private TilePane productGridContainer;

    @FXML
    public void initialize() {
        // Giả lập danh sách sản phẩm (Sau này sẽ lấy từ Server)
        for (int i = 1; i <= 12; i++) {
            ItemDTO item = new ArtDTO();
            item.setName("Sản phẩm " + i);
            item.setStartingPrice(i * 100000.0);
            // item.setImagePath(...); // Đại có thể set ảnh thật ở đây

            // 2. TÁI SỬ DỤNG: Dùng ItemCard thay vì tự vẽ VBox, Label...
            // Truyền tham số x=150, y=150 như Đại đã sửa ở ItemCard
            ItemCard productCard = new ItemCard(item, 150, 150);

            // ĐÓNG GÓI: Cất dữ liệu vào túi ngầm để lát nữa dùng
            productCard.setUserData(item);
            // cai dat hieu ung ban tay khi an
            productCard.setCursor(javafx.scene.Cursor.HAND);
            // Cài đặt sự kiện click
            productCard.setOnMouseClicked(event -> openBiddingRoom(event));
            // Thêm vào lưới hiển thị
            productGridContainer.getChildren().add(productCard);
        }
    }

    public void openBiddingRoom(Event event) {
        try {
            // 1. Lấy dữ liệu từ túi (ItemDTO)
            Node sourceNode = (Node) event.getSource();
            ItemDTO selectedItem = (ItemDTO) sourceNode.getUserData();

            // 2. Tự nạp FXML thủ công để lấy Controller (Vì hàm switchScene của cha chưa hỗ trợ truyền data)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BiddingRoomView.fxml"));
            Parent root = loader.load();

            // 3. BƠM DỮ LIỆU: Lấy Controller của màn hình mới và truyền item sang
            BiddingRoomController controller = loader.getController();
            controller.setData(selectedItem); // Truyền đối tượng ItemDTO xịn sang

            // 4. TẬN DỤNG stage từ event (Giống logic trong BaseController)
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            // 5. THIẾT LẬP GIAO DIỆN (Sử dụng các thông số giống như switchScene của cha)
            stage.setScene(new Scene(root, 900, 600)); // Cố định kích thước như Đại muốn
            stage.setTitle("Phòng đấu giá: " + selectedItem.getName());
            stage.show();

        } catch (IOException e) {
            // SỬ DỤNG LẠI TÀI SẢN CHA: Gọi showAlert thay vì System.out.println
            showAlert("Lỗi hệ thống", "Không thể nạp giao diện phòng đấu giá!");
            e.printStackTrace();
        }
    }
}