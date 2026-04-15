package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import com.auction.client.model.ItemDTO;

import javafx.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;

public class SellerController {

    @FXML
    private TextField txtItemName;
    @FXML private TextField txtStartingPrice;
    @FXML private TextField txtPriceStep;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName; // Cái label hiện tên file ảnh
    @FXML private DatePicker dateStart;
    @FXML private TextField txtTimeStart;
    @FXML private DatePicker dateEnd;
    @FXML private TextField txtTimeEnd;

    private String currentImagePath = ""; // Biến tạm lưu đường dẫn ảnh
    private int currentSellerId = 1; // ID giả lập của Đại (sau này lấy từ login)
    @FXML private TilePane inventoryGrid;
    @FXML
    // ham xu li hinh anh
    private void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        // FileChooser dung de mo cua so chon file, setTitle la ten tren cung cua so

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        // loc file anh

        File selectedFile = fileChooser.showOpenDialog(null);
        // fileChooser.showOpenDialog(...): mở cửa sổ chọn file (Open File Dialog). Khi chạy, nó sẽ hiện một cửa sổ cho user duyệt file trong máy
        // howOpenDialog(null): Hiện ra độc lập ở giữa màn hình, có thể click chuột quay lại cửa sổ chính phía sau

        if (selectedFile != null) {
            currentImagePath = selectedFile.getAbsolutePath(); // Lấy đường dẫn đầy đủ
            lblFileName.setText(selectedFile.getName()); // Hiển thị tên file lên giao diện
        }
    }

    @FXML
    // dong goi san pham nhap vao
    private void handlePostItem(ActionEvent event) {
        System.out.println("--- Đang xử lý đăng sản phẩm mới ---");

        try {
            // 1. LẤY THÔNG TIN TỪ GIAO DIỆN
            String name = txtItemName.getText().trim();
            String priceStr = txtStartingPrice.getText().trim();
            String stepStr = txtPriceStep.getText().trim();
            String description = txtDescription.getText().trim();
            LocalDate startDay = dateStart.getValue();
            LocalDate endDay = dateEnd.getValue();

            // lay du lieu cho cac kieu Double
            if (name.isEmpty() || priceStr.isEmpty() || stepStr.isEmpty()) {
                System.out.println("Lỗi: Bạn quên chưa nhập Tên, Giá đầu hoặc Bước giá kìa!");
                // Sau này Đại có thể dùng Alert để hiện thông báo popup
                return;
            }
            // Du lieu nhap vao la String nen can doi sang double
            double startingPrice = Double.parseDouble(priceStr);
            double priceStep = Double.parseDouble(stepStr);

            // lay du lieu cho thoi gian dau gia
            String sTime = txtTimeStart.getText(); // ví dụ "08:00"
            String eTime = txtTimeEnd.getText();   // ví dụ "20:00"

            if (startDay == null || endDay == null) {
                System.out.println("Lỗi: Đại ơi, chọn ngày đã nhé!");
                return;
            }

            //Gộp lại thành chuỗi hoàn chỉnh
            String fullStart = startDay.toString() + " " + sTime;
            String fullEnd = endDay.toString() + " " + eTime;
            // startDay.toString(): tra ve ding dang XXXX-YY-ZZ

            //ĐÓNG GÓI VÀO DTO (Cái "thùng hàng" mình vừa tạo)
            ItemDTO newItem = new ItemDTO();
            newItem.setName(name);
            newItem.setStartingPrice(startingPrice);
            newItem.setPriceStep(priceStep);
            newItem.setDescription(description);
            newItem.setImagePath(currentImagePath); // Đường dẫn ảnh từ hàm chọn ảnh
            newItem.setSellerId(currentSellerId);
            newItem.setStartTime(fullStart);
            newItem.setEndTime(fullEnd);

            // resert dữ liệu nhập vào
            txtItemName.clear(); // Reset các ô nhập văn bản
            txtStartingPrice.clear();
            txtPriceStep.clear();
            txtDescription.clear();
            dateStart.setValue(null);  // Reset lịch (DatePicker)
            dateEnd.setValue(null);
            currentImagePath = null; // Reset biến lưu đường dẫn ảnh (nếu có)

            // 4. KIỂM TRA KẾT QUẢ (In ra Console để xem thử)
            System.out.println(newItem.toString());

            displayItem(newItem);
            System.out.println("Đã hiển thị sản phẩm mới lên màn hình!");

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Giá tiền và Bước giá phải nhập bằng số (ví dụ: 500000)!");
        } catch (Exception e) {
            System.out.println("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    // day thu len center ben canh
    private void displayItem(ItemDTO item) {
        // 1. Tạo một cái khung dọc (VBox) cho mỗi sản phẩm (Card)
        VBox itemCard = new VBox(10); // khoang cach padding = 10
        itemCard.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5;");
        itemCard.setPrefWidth(200); // Đặt kích thước cố định cho các ô gạch

        // 2. Thêm ảnh
        ImageView imageView = new ImageView();
        if (item.getImagePath() != null) {
            imageView.setImage(new Image("file:" + item.getImagePath()));
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true); // luôn giữ đúng cái dáng của tấm ảnh gốc

        // 3. Thông tin sản phẩm
        Label lblName = new Label(item.getName());
        lblName.setStyle("-fx-font-weight: bold;");

        Label lblPrice = new Label("Giá khởi điểm: " + item.getStartingPrice());

        Label lblTimeStart = new Label("Bắt đầu: " + item.getStartTime());
        lblTimeStart.setStyle("-fx-font-size: 10px; -fx-text-fill: red;");
        Label lblTimeEnd = new Label("Kết thúc: " + item.getEndTime());
        lblTimeEnd.setStyle("-fx-font-size: 10px; -fx-text-fill: red;");

        // 4. Cho vào Card và "ném" vào Grid
        itemCard.getChildren().addAll(imageView, lblName, lblPrice, lblTimeStart, lblTimeEnd);

        // ĐÂY LÀ DÒNG QUAN TRỌNG: Thêm vào TilePane
        inventoryGrid.getChildren().add(itemCard);
    }
}