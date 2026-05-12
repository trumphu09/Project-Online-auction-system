package com.auction.client.util;

import com.auction.client.model.dto.*;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ItemCard extends VBox {

    // IP máy SERVER
    private static final String SERVER_BASE_URL = "http://localhost:8080/api";

    public ItemCard(ItemDTO item, double width, double height, boolean isBuyer) {

        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);
        this.setPrefWidth(200);

        this.setStyle(
                "-fx-border-color: #ffffff;" +
                "-fx-padding: 10;" +
                "-fx-background-color: white;" +
                "-fx-border-radius: 5;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        // =========================
        // IMAGE
        // =========================
        ImageView imageView = new ImageView();

        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);

        loadImage(item, imageView);

        // =========================
        // NAME
        // =========================
        Label nameLabel = new Label(item.getName());

        nameLabel.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;"
        );

        nameLabel.setWrapText(true);

        // =========================
        // CATEGORY INFO
        // =========================
        Label specificAttrLabel = new Label();

        specificAttrLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #7f8c8d;"
        );

        if (item instanceof ElectronicsDTO) {
            specificAttrLabel.setText(
                    "🔧 Bảo hành: " +
                    ((ElectronicsDTO) item).getWarrantyMonths() +
                    " tháng"
            );
        }

        else if (item instanceof VehicleDTO) {
            specificAttrLabel.setText(
                    "🚗 Xe: " +
                    ((VehicleDTO) item).getBrand()
            );
        }

        else if (item instanceof ArtDTO) {
            specificAttrLabel.setText(
                    "🎨 Họa sĩ: " +
                    ((ArtDTO) item).getArtist()
            );
        }

        // =========================
        // PRICE
        // =========================
        double displayPrice =
                item.getCurrentMaxPrice() > 0
                        ? item.getCurrentMaxPrice()
                        : item.getStartingPrice();

        Label priceLabel =
                new Label(String.format("%,.0f", displayPrice) + " VNĐ");

        priceLabel.setStyle(
                "-fx-text-fill: #e67e22;" +
                "-fx-font-weight: bold;"
        );

        // =========================
        // STATUS
        // =========================
        Label statusLabel =
                new Label(item.getStatus() != null
                        ? item.getStatus()
                        : "OPEN");

        statusLabel.setPadding(new Insets(2, 8, 2, 8));

        updateStatusStyle(statusLabel, item.getStatus());

        // =========================
        // ADD COMPONENTS
        // =========================
        this.getChildren().addAll(
                imageView,
                nameLabel,
                specificAttrLabel,
                priceLabel,
                statusLabel
        );

        // =========================
        // BUYER BUTTON
        // =========================
        if (isBuyer) {

            Button btnAddToCart =
                    new Button("🛒 Thêm vào giỏ");

            btnAddToCart.setStyle(
                    "-fx-background-color: #ff8c00;" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;"
            );

            btnAddToCart.setOnAction(e -> {

                e.consume();

                System.out.println("[ItemCard] Adding to watchlist - itemId: " + item.getId());

                AuctionFacade.getInstance().addToWatchlist(
                        item.getId(),
                        new ApiCallback<JsonObject>() {

                            @Override
                            public void onSuccess(JsonObject result) {

                                Platform.runLater(() -> {
                                    System.out.println("[ItemCard] SUCCESS: Đã thêm " + item.getName() + " vào giỏ!");
                                    btnAddToCart.setDisable(true);
                                    btnAddToCart.setText("✓ Đã thêm vào giỏ");
                                    btnAddToCart.setStyle(
                                            "-fx-background-color: #28a745;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-cursor: hand;"
                                    );
                                });
                            }

                            @Override
                            public void onError(String err) {

                                Platform.runLater(() -> {
                                    System.err.println("[ItemCard] ERROR: Lỗi thêm vào giỏ: " + err);
                                    btnAddToCart.setText("❌ Lỗi");
                                    btnAddToCart.setStyle(
                                            "-fx-background-color: #dc3545;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-cursor: hand;"
                                    );
                                    // Hiển thị cảnh báo cho người dùng
                                    javax.swing.JOptionPane.showMessageDialog(
                                        null,
                                        "Không thể thêm sản phẩm vào giỏ:\n" + err,
                                        "Lỗi",
                                        javax.swing.JOptionPane.ERROR_MESSAGE
                                    );
                                });
                            }
                        }
                );
            });

            HBox bottom = new HBox(btnAddToCart);

            bottom.setAlignment(Pos.CENTER_RIGHT);

            this.getChildren().add(bottom);
        }

        // Hover
        this.setOnMouseEntered(e ->
                this.setStyle(
                        this.getStyle() +
                        "-fx-border-color: #3498db;"
                )
        );

        this.setOnMouseExited(e ->
                this.setStyle(
                        this.getStyle().replace(
                                "-fx-border-color: #3498db;",
                                "-fx-border-color: #ffffff;"
                        )
                )
        );
    }

    // =====================================
    // LOAD IMAGE FROM SERVER
    // =====================================
    private void loadImage(ItemDTO item, ImageView imageView) {

        try {

            String imageUrl = buildImageUrl(item.getImagePath());

            System.out.println("DEBUG imagePath = " + item.getImagePath());
            System.out.println("DEBUG imageUrl  = " + imageUrl);

            if (imageUrl == null) {
                return;
            }

            Image image = new Image(imageUrl, true);

            image.errorProperty().addListener((obs, oldVal, newVal) -> {

                if (newVal) {
                    System.err.println("Không load được ảnh: " + imageUrl);
                }
            });

            imageView.setImage(image);

        }

        catch (Exception e) {

            System.err.println("Lỗi load ảnh:");
            e.printStackTrace();
        }
    }

    // =====================================
    // BUILD IMAGE URL
    // =====================================
    private String buildImageUrl(String rawPath) {

        if (rawPath == null || rawPath.isBlank()) {
            return null;
        }

        // Nếu đã là URL
        if (rawPath.startsWith("http://") ||
                rawPath.startsWith("https://")) {

            return rawPath;
        }

        // Cắt filename
        String filename = rawPath;

        int lastSlash = Math.max(
                rawPath.lastIndexOf('/'),
                rawPath.lastIndexOf('\\')
        );

        if (lastSlash >= 0) {
            filename = rawPath.substring(lastSlash + 1);
        }

    // ✅ FIX: URL-encode filename trước khi ghép vào URL
    try {
        String encoded = java.net.URLEncoder.encode(filename, "UTF-8")
                            .replace("+", "%20");  // URLEncoder dùng + cho space, HTTP cần %20
        return SERVER_BASE_URL + "/images/" + encoded;
    } catch (java.io.UnsupportedEncodingException e) {
        return SERVER_BASE_URL + "/images/" + filename;
    }
    }

    // =====================================
    // STATUS STYLE
    // =====================================
    private void updateStatusStyle(Label label, String status) {

        String baseStyle =
                "-fx-background-radius: 10;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;";

        if ("RUNNING".equalsIgnoreCase(status)) {

            label.setStyle(
                    baseStyle +
                    "-fx-background-color: #2ecc71;"
            );
        }

        else if (
                "FINISHED".equalsIgnoreCase(status)
                        || "PAID".equalsIgnoreCase(status)
                        || "CANCELED".equalsIgnoreCase(status)
        ) {

            label.setStyle(
                    baseStyle +
                    "-fx-background-color: #e74c3c;"
            );
        }

        else {

            label.setStyle(
                    baseStyle +
                    "-fx-background-color: #3498db;"
            );
        }
    }
}