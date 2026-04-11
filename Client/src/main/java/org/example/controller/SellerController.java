package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.example.model.ItemDTO;

import javafx.event.ActionEvent;
import java.io.File;

public class SellerController {

    @FXML
    private TextField txtItemName;
    @FXML private TextField txtStartingPrice;
    @FXML private TextField txtPriceStep;
    @FXML private TextArea txtDescription;
    @FXML private Label lblFileName; // Cái label hiện tên file ảnh

    private String currentImagePath = ""; // Biến tạm lưu đường dẫn ảnh
    private int currentSellerId = 1; // ID giả lập của Đại (sau này lấy từ login)

    @FXML
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
            System.out.println("Đã chọn ảnh: " + currentImagePath);
        }
    }

    @FXML
    private void handlePostItem(ActionEvent event) {
        System.out.println("--- Đang xử lý đăng sản phẩm mới ---");

        try {
            // 1. LẤY THÔNG TIN TỪ GIAO DIỆN
            String name = txtItemName.getText().trim();
            String priceStr = txtStartingPrice.getText().trim();
            String stepStr = txtPriceStep.getText().trim();
            String description = txtDescription.getText().trim();

            // 2. KIỂM TRA DỮ LIỆU (VALIDATION) - Rất quan trọng!
            if (name.isEmpty() || priceStr.isEmpty() || stepStr.isEmpty()) {
                System.out.println("Lỗi: Bạn quên chưa nhập Tên, Giá đầu hoặc Bước giá kìa!");
                // Sau này Đại có thể dùng Alert để hiện thông báo popup
                return;
            }

            // Du lieu nhap vao la String nen can doi sang double
            double startingPrice = Double.parseDouble(priceStr);
            double priceStep = Double.parseDouble(stepStr);

            // 3. ĐÓNG GÓI VÀO DTO (Cái "thùng hàng" mình vừa tạo)
            ItemDTO newItem = new ItemDTO();
            newItem.setName(name);
            newItem.setStartingPrice(startingPrice);
            newItem.setPriceStep(priceStep);
            newItem.setDescription(description);
            newItem.setImagePath(currentImagePath); // Đường dẫn ảnh từ hàm chọn ảnh
            newItem.setSellerId(currentSellerId);

            // 4. KIỂM TRA KẾT QUẢ (In ra Console để xem thử)
            System.out.println("Chúc mừng Đại! Đã đóng gói dữ liệu thành công:");
            System.out.println(newItem.toString());

            // 5. (Tùy chọn) Xóa sạch Form sau khi đăng để nhập món tiếp theo
            // clearForm();

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Giá tiền và Bước giá phải nhập bằng số (ví dụ: 500000)!");
        } catch (Exception e) {
            System.out.println("Có lỗi xảy ra: " + e.getMessage());
        }
    }
}