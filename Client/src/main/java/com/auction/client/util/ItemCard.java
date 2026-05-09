package com.auction.client.util;

import com.auction.client.model.dto.*;
import javafx.geometry.Insets;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import com.auction.client.model.dto.ItemDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// TÍNH KẾ THỪA: Thừa hưởng mọi thứ của VBox
public class ItemCard extends VBox {

    public ItemCard(ItemDTO item, double width, double height, boolean isBuyer) {
        // Cấu hình khung bên ngoài (Thay cho setStyle rườm rà)
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPrefWidth(200);
        // Thêm hiệu ứng đổ bóng cho Card đẹp hơn
        this.setStyle("-fx-border-color: #ffffff; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // 1. Xử lý ảnh (Giữ nguyên logic load ảnh siêu chuẩn của ông)
        ImageView imageView = new ImageView();
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                String path = item.getImagePath();
                // Nếu là link web hoặc chuẩn uri rồi thì giữ nguyên, nếu là đường dẫn Windows thì format lại
                if (!path.startsWith("http") && !path.startsWith("file:")) {
                    // Đổi \ thành / và thêm file:/// để JavaFX đọc mượt trên Windows
                    path = "file:///" + path.replace("\\", "/"); 
                }
                
                Image img = new Image(path, true); // true = load ngầm không đơ app
                
                // Bắt lỗi nếu ảnh bị hỏng hoặc xóa mất trên máy
                img.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) System.err.println("Không tải được ảnh từ đường dẫn: " + item.getImagePath());
                });
                
                imageView.setImage(img);
            } catch (Exception e) {
                System.err.println("Lỗi format đường dẫn ảnh: " + e.getMessage());
            }
        }
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        // 2. Thông tin tên sản phẩm
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // 3. THUỘC TÍNH RIÊNG (Nhận diện theo class DTO)
        Label specificAttrLabel = new Label();
        specificAttrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        if (item instanceof ElectronicsDTO) {
            specificAttrLabel.setText("🔧 Bảo hành: " + ((ElectronicsDTO) item).getWarrantyMonths() + " tháng");
        } else if (item instanceof VehicleDTO) {
            specificAttrLabel.setText("🚗 Xe: " + ((VehicleDTO) item).getBrand());
        } else if (item instanceof ArtDTO) {
            specificAttrLabel.setText("🎨 Họa sĩ: " + ((ArtDTO) item).getArtist());
        }

        // 4. Giá (Ưu tiên hiển thị giá đấu cao nhất, nếu chưa ai đấu thì hiện giá khởi điểm)
        double displayPrice = item.getCurrentMaxPrice() > 0 ? item.getCurrentMaxPrice() : item.getStartingPrice();
        Label priceLabel = new Label(String.format("%,.0f", displayPrice) + " VNĐ");
        priceLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");

        // 5. TRẠNG THÁI ĐẤU GIÁ (Có màu sắc để Seller dễ nhìn)
        Label statusLabel = new Label(item.getStatus() != null ? item.getStatus() : "OPEN");
        statusLabel.setPadding(new Insets(2, 8, 2, 8));
        updateStatusStyle(statusLabel, item.getStatus());

        // Đóng gói các thành phần vào chính nó (this)
        this.getChildren().addAll(imageView, nameLabel, specificAttrLabel, priceLabel, statusLabel);

        // Hiệu ứng di chuột (Hover) làm sáng Card
        this.setOnMouseEntered(e -> this.setStyle(this.getStyle() + "-fx-border-color: #3498db;"));
        this.setOnMouseExited(e -> this.setStyle(this.getStyle().replace("-fx-border-color: #3498db;", "-fx-border-color: #ffffff;")));
    }

    // Hàm set màu sắc theo trạng thái
    private void updateStatusStyle(Label label, String status) {
        String baseStyle = "-fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;";
        if ("RUNNING".equalsIgnoreCase(status)) {
            label.setStyle(baseStyle + "-fx-background-color: #2ecc71;"); // Xanh lá
        } else if ("FINISHED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            label.setStyle(baseStyle + "-fx-background-color: #e74c3c;"); // Đỏ
        } else {
            label.setStyle(baseStyle + "-fx-background-color: #3498db;"); // Xanh dương (OPEN)
        }
      // CHỈ TẠO NÚT NẾU LÀ NGƯỜI MUA (BUYER)
      if (isBuyer) {
        Button btnAddToCart = new Button("🛒 Thêm vào giỏ");
        btnAddToCart.setStyle("-fx-background-color: #ff8c00; -fx-text-fill: white; -fx-cursor: hand;");

        btnAddToCart.setOnAction(e -> {
          e.consume(); // Chặn click lan ra ngoài thẻ
          // Gọi thư ký Facade để báo cho Back-end
          AuctionFacade.getInstance().addToWatchlist(item.getId(), new ApiCallback<JsonObject>() {

            @Override
            public void onSuccess(JsonObject result) {
              Platform.runLater(() -> {
                System.out.println("Đã thêm thành công!");
                // Gợi ý: Chỗ này có thể dùng showAlert() để báo cho người dùng biết
              });
            }

            @Override
            public void onError(String err) {
              Platform.runLater(() -> {
                System.out.println("Lỗi: " + err);
                // Gợi ý: showAlert("Lỗi", err);
              });
            }

          });
        });
        // Cho vào HBox căn phải rồi ép vào cuối ItemCard
        HBox bottom = new HBox(btnAddToCart);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        this.getChildren().add(bottom);
      }

      // CHỈ TẠO NÚT NẾU LÀ NGƯỜI MUA (BUYER)
      if (isBuyer) {
        Button btnAddToCart = new Button("🛒 Thêm vào giỏ");
        btnAddToCart.setStyle("-fx-background-color: #ff8c00; -fx-text-fill: white; -fx-cursor: hand;");

        btnAddToCart.setOnAction(e -> {
          e.consume(); // Chặn click lan ra ngoài thẻ
          // Gọi thư ký Facade để báo cho Back-end
          AuctionFacade.getInstance().addToWatchlist(item.getId(), new ApiCallback<JsonObject>() {

            @Override
            public void onSuccess(JsonObject result) {
              Platform.runLater(() -> {
                System.out.println("Đã thêm thành công!");
                // Gợi ý: Chỗ này có thể dùng showAlert() để báo cho người dùng biết
              });
            }

            @Override
            public void onError(String err) {
              Platform.runLater(() -> {
                System.out.println("Lỗi: " + err);
                // Gợi ý: showAlert("Lỗi", err);
              });
            }

          });
        });
        // Cho vào HBox căn phải rồi ép vào cuối ItemCard
        HBox bottom = new HBox(btnAddToCart);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        this.getChildren().add(bottom);
      }

//        // Có thể thêm hiệu ứng di chuột tại đây để dùng chung cho cả App
//        this.setOnMouseEntered(e -> this.setStyle(this.getStyle() + "-fx-border-color: #2196F3;"));
//        this.setOnMouseExited(e -> this.setStyle(this.getStyle().replace("-fx-border-color: #2196F3;", "-fx-border-color: #ddd;")));
    }
}