package com.auction.client.util;

import com.auction.client.model.dto.*;
import javafx.geometry.Insets;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class ItemCard extends VBox {

    // Địa chỉ gốc của server — phải khớp với ApiService.BASE_URL
    private static final String SERVER_BASE_URL = "http://localhost:8080/api";
//    private final String SERVER_BASE_URL = "http://10.11.113.69:8080/api";

    public ItemCard(ItemDTO item, double width, double height, boolean isBuyer) {
        // Cấu hình khung bên ngoài
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPrefWidth(200);
        this.setStyle("-fx-border-color: #ffffff; -fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
    // 1. Xử lý ảnh chuyên nghiệp
        ImageView imageView = new ImageView();
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                String path = item.getImagePath();
                String imageUri;

                if (path.startsWith("http")) {
                    imageUri = path; // Nếu là link web
                } else {
                    // Tự động chuyển đường dẫn ổ cứng (C:\...) thành URI (file:///...)
                    java.io.File file = new java.io.File(path);
                    imageUri = file.toURI().toString();
                }

                Image img = new Image(imageUri, true); 
                imageView.setImage(img);
            } catch (Exception e) {
                System.err.println("Lỗi load ảnh: " + e.getMessage());
            }
        }
    // Đảm bảo kích thước hiển thị
    imageView.setFitWidth(width);
    imageView.setFitHeight(height);
    imageView.setPreserveRatio(true);

        // 2. Tên sản phẩm
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        nameLabel.setWrapText(true);

        // 3. Thuộc tính riêng theo loại DTO
        Label specificAttrLabel = new Label();
        specificAttrLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        if (item instanceof ElectronicsDTO) {
            specificAttrLabel.setText("🔧 Bảo hành: " + ((ElectronicsDTO) item).getWarrantyMonths() + " tháng");
        } else if (item instanceof VehicleDTO) {
            specificAttrLabel.setText("🚗 Xe: " + ((VehicleDTO) item).getBrand());
        } else if (item instanceof ArtDTO) {
            specificAttrLabel.setText("🎨 Họa sĩ: " + ((ArtDTO) item).getArtist());
        }

        // 4. Giá hiển thị
        double displayPrice = item.getCurrentMaxPrice() > 0 ? item.getCurrentMaxPrice() : item.getStartingPrice();
        Label priceLabel = new Label(String.format("%,.0f", displayPrice) + " VNĐ");
        priceLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");

        // 5. Trạng thái đấu giá
        Label statusLabel = new Label(item.getStatus() != null ? item.getStatus() : "OPEN");
        statusLabel.setPadding(new Insets(2, 8, 2, 8));
        updateStatusStyle(statusLabel, item.getStatus());

        // Đưa các thành phần vào card
        this.getChildren().addAll(imageView, nameLabel, specificAttrLabel, priceLabel, statusLabel);

        // 6. CHỈ TẠO NÚT NẾU LÀ NGƯỜI MUA (BUYER)
        if (isBuyer) {
            Button btnAddToCart = new Button("🛒 Thêm vào giỏ");
            btnAddToCart.setStyle("-fx-background-color: #ff8c00; -fx-text-fill: white; -fx-cursor: hand;");

            btnAddToCart.setOnAction(e -> {
                e.consume(); // Chặn click lan ra ngoài thẻ
                AuctionFacade.getInstance().addToWatchlist(item.getId(), new ApiCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        Platform.runLater(() -> System.out.println("Đã thêm " + item.getName() + " vào giỏ!"));
                    }

                    @Override
                    public void onError(String err) {
                        Platform.runLater(() -> System.out.println("Lỗi thêm vào giỏ: " + err));
                    }
                });
            });

            HBox bottom = new HBox(btnAddToCart);
            bottom.setAlignment(Pos.CENTER_RIGHT);
            this.getChildren().add(bottom);
        }

        // Hiệu ứng di chuột (Hover)
        this.setOnMouseEntered(e -> this.setStyle(this.getStyle() + "-fx-border-color: #3498db;"));
        this.setOnMouseExited(e ->  this.setStyle(this.getStyle().replace("-fx-border-color: #3498db;", "-fx-border-color: #ffffff;")));
    }


    private String buildImageUrl(String rawPath) {
        if (rawPath.startsWith("http://") || rawPath.startsWith("https://")) {
            return rawPath; // Đã là URL rồi, dùng thẳng
        }

        // Lấy tên file từ đường dẫn (bỏ thư mục cha)
        String filename = rawPath;
        int lastSlash = Math.max(rawPath.lastIndexOf('/'), rawPath.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            filename = rawPath.substring(lastSlash + 1);
        }

        // Trả về URL qua HTTP server
        return SERVER_BASE_URL + "/images/" + filename;
    }

    private void updateStatusStyle(Label label, String status) {
        String baseStyle = "-fx-background-radius: 10; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;";
        if ("RUNNING".equalsIgnoreCase(status)) {
            label.setStyle(baseStyle + "-fx-background-color: #2ecc71;");
        } else if ("FINISHED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            label.setStyle(baseStyle + "-fx-background-color: #e74c3c;");
        } else {
            label.setStyle(baseStyle + "-fx-background-color: #3498db;");
        }
    }

    // Client: com.auction.client.util.ItemCard
    private void setupImage(ItemDTO item, double width, double height) {
        ImageView iv = new ImageView();
        String rawPath = item.getImagePath();
        
        if (rawPath != null && !rawPath.isEmpty()) {
            // Dò tìm ảnh: 
            // 1. Thử đường dẫn trực tiếp (nếu là path tuyệt đối)
            // 2. Thử lùi 1 cấp (nếu chạy từ thư mục Client/)
            // 3. Thử đường dẫn gốc
            java.io.File f1 = new java.io.File(rawPath);
            java.io.File f2 = new java.io.File("../uploads/" + new java.io.File(rawPath).getName());
            java.io.File f3 = new java.io.File("uploads/" + new java.io.File(rawPath).getName());

            String finalUri = null;
            if (f1.exists()) finalUri = f1.toURI().toString();
            else if (f2.exists()) finalUri = f2.toURI().toString();
            else if (f3.exists()) finalUri = f3.toURI().toString();

            if (finalUri != null) {
                iv.setImage(new Image(finalUri, true));
            } else {
                System.err.println("Không tìm thấy ảnh: " + rawPath);
            }
        }
        
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setPreserveRatio(true);
        this.getChildren().add(0, iv); // Đẩy ảnh lên đầu thẻ
    }
}