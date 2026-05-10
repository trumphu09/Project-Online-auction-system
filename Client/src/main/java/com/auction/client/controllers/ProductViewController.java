package com.auction.client.controllers;

import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.auction.client.service.ApiCallback;
import com.auction.client.service.AuctionFacade;
import com.auction.client.util.ItemCard;
import com.google.gson.JsonObject;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProductViewController extends BaseController implements Initializable {

    // --- CÁC ID CỦA SẢN PHẨM & TÌM KIẾM ---
    @FXML private TilePane productGridContainer;
    @FXML private TextField txtSearch;

    // --- CÁC ID CỦA VÍ (WALLET) ---
    @FXML private Label lblFullName;
    @FXML private Label lblBalance;
    @FXML private TextField txtDepositAmount;

    // --- CÁC ID CỦA LỊCH SỬ ĐẤU GIÁ ---
    @FXML private ListView<String> listActiveBids;
    @FXML private ListView<String> listWonItems;

    // --- CÁC ID CỦA GIỎ HÀNG ---
    @FXML private VBox cartSidebar;
    @FXML private VBox cartItemsContainer;
    private boolean isCartOpen = false;
    
    // Lưu trữ tất cả items cho tìm kiếm cục bộ
    private List<ItemDTO> allItems = new java.util.ArrayList<>();

    @FXML
    public void initialize(URL url, ResourceBundle rb) {

        // loading state
        if (lblFullName != null) {
            lblFullName.setText("Đang tải...");
        }

        if (lblBalance != null) {
            lblBalance.setText("Đang tải...");
        }

        // LOAD DATA - ưu tiên tải items trước, sau đó tải user profile
        loadAllItems();         // Tải danh sách sản phẩm TRƯỚC
        loadUserProfile();      // Sau đó tải thông tin người dùng
        loadBidderHistory();    // Tải lịch sử đấu giá
    }

    // ==========================================
    // PHẦN 1: TẢI & HIỂN THỊ SẢN PHẨM
    // ==========================================
    private void loadAllItems() {
        AuctionFacade.getInstance().getAllItems(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> {
                    allItems.clear();
                    if (items != null && !items.isEmpty()) {
                        allItems.addAll(items);
                        System.out.println("Đã tải " + items.size() + " sản phẩm");
                    } else {
                        System.out.println("Danh sách sản phẩm rỗng");
                    }
                    updateProductGrid(allItems);
                });
            }
            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    System.err.println("Lỗi tải dữ liệu sản phẩm: " + errorMessage);
                    showAlert("Lỗi tải dữ liệu", errorMessage);
                });
            }
        });
    }

    private void updateProductGrid(List<ItemDTO> items) {
        productGridContainer.getChildren().clear();
        if (items != null && !items.isEmpty()) {
            for (ItemDTO item : items) {
                ItemCard card = new ItemCard(item, 180, 150, true);
                card.setUserData(item);
                card.setCursor(javafx.scene.Cursor.HAND);
                card.setOnMouseClicked(this::openBiddingRoom);
                productGridContainer.getChildren().add(card);
            }
        } else {
            Label emptyLabel = new Label("Không có sản phẩm nào");
            productGridContainer.getChildren().add(emptyLabel);
        }
    }

    // ==========================================
    // PHẦN 2: THÔNG TIN CÁ NHÂN & NẠP TIỀN
    // ==========================================
    private void loadUserProfile() {
        AuctionFacade.getInstance().getUserProfile(new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO user) {
                Platform.runLater(() -> {
                    if (user != null) {
                        // === FIX LỖI 1: fullName thường null → fallback về username ===
                        // Server lưu trường "username", không phải "full_name" nên fullName luôn null.
                        // Ưu tiên fullName nếu có, còn không thì dùng username.
                        String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                                ? user.getFullName()
                                : user.getUsername();

                        if (lblFullName != null) {
                            lblFullName.setText("Chào, " + (displayName != null ? displayName : "Người dùng"));
                        }
                        if (lblBalance != null) {
                            lblBalance.setText(String.format("%,.0f VNĐ", user.getBalance()));
                        }
                    } else {
                        if (lblFullName != null) lblFullName.setText("Lỗi: Không thể tải tên");
                        if (lblBalance != null) lblBalance.setText("0 VNĐ");
                    }
                });
            }
            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    System.err.println("Lỗi tải Profile: " + errorMessage);
                    if (lblFullName != null) lblFullName.setText("Lỗi tải thông tin");
                    if (lblBalance != null) lblBalance.setText("0 VNĐ");
                });
            }
        });
    }

    @FXML
    private void handleDepositAction() {
        try {
            double amount = Double.parseDouble(txtDepositAmount.getText());
            if (amount <= 0) {
                showAlert("Lỗi", "Số tiền nạp phải lớn hơn 0!");
                return;
            }

            AuctionFacade.getInstance().depositMoney(amount, new ApiCallback<JsonObject>() {
                @Override
                public void onSuccess(JsonObject result) {
                    Platform.runLater(() -> {
                        showAlert("Thành công", "Đã nạp " + String.format("%,.0f", amount) + " VNĐ vào ví!");
                        txtDepositAmount.clear();
                        loadUserProfile(); // Cập nhật lại số dư + tên người dùng
                    });
                }
                @Override
                public void onError(String errorMessage) {
                    Platform.runLater(() -> showAlert("Lỗi nạp tiền", errorMessage));
                }
            });
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    // ==========================================
    // PHẦN 3: TÌM KIẾM & LỌC DANH MỤC
    // ==========================================
    @FXML
    private void handleSearchAction() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            // Nếu search trống, hiển thị tất cả
            updateProductGrid(allItems);
            return;
        }

        // Tìm kiếm cục bộ từ danh sách allItems
        List<ItemDTO> filtered = new java.util.ArrayList<>();
        for (ItemDTO item : allItems) {
            if (item.getName().toLowerCase().contains(keyword) || 
                item.getDescription().toLowerCase().contains(keyword)) {
                filtered.add(item);
            }
        }
        updateProductGrid(filtered);
    }

    @FXML private void handleFilterElectronics() { filterByCategory("ELECTRONICS"); }
    @FXML private void handleFilterVehicles()    { filterByCategory("VEHICLE"); }
    @FXML private void handleFilterArts()        { filterByCategory("ART"); }

    private void filterByCategory(String category) {
        // Lọc cục bộ từ danh sách allItems
        List<ItemDTO> filtered = new java.util.ArrayList<>();
        for (ItemDTO item : allItems) {
            if (category.equalsIgnoreCase(item.getCategory())) {
                filtered.add(item);
            }
        }
        updateProductGrid(filtered);
    }

    // ==========================================
    // PHẦN 4: LỊCH SỬ ĐẤU GIÁ
    // ==========================================
    private void loadBidderHistory() {
        AuctionFacade.getInstance().getMyActiveBids(new ApiCallback<List<JsonObject>>() {
            @Override
            public void onSuccess(List<JsonObject> bids) {
                Platform.runLater(() -> {
                    if (listActiveBids != null) listActiveBids.getItems().clear();
                    if (bids != null) {
                        for (JsonObject bid : bids) {
                            listActiveBids.getItems().add(
                                "SP ID: " + bid.get("auction_id").getAsInt()
                                + " | Bạn Bid: " + String.format("%,.0f", bid.get("amount").getAsDouble())
                            );
                        }
                    }
                });
            }
            @Override public void onError(String err) {}
        });

        AuctionFacade.getInstance().getMyWonItems(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> {
                    if (listWonItems != null) listWonItems.getItems().clear();
                    if (items != null) {
                        for (ItemDTO item : items) {
                            listWonItems.getItems().add("Thắng: " + item.getName());
                        }
                    }
                });
            }
            @Override public void onError(String err) {}
        });
    }

    // ==========================================
    // PHẦN 5: CHUYỂN PHÒNG & ĐĂNG XUẤT
    // ==========================================
    public void openBiddingRoom(Event event) {
        try {
            Node sourceNode = (Node) event.getSource();
            ItemDTO selectedItem = (ItemDTO) sourceNode.getUserData();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BiddingRoomView.fxml"));
            Parent root = loader.load();

            BiddingRoomController controller = loader.getController();
            controller.setData(selectedItem);

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Phòng đấu giá: " + selectedItem.getName());
            stage.show();

        } catch (IOException e) {
            showAlert("Lỗi hệ thống", "Không thể nạp giao diện phòng đấu giá!");
            e.printStackTrace();
        }
    }

    // ==========================================
    // PHẦN 6: GIỎ HÀNG
    // ==========================================
    @FXML
    private void handleToggleCart() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), cartSidebar);
        if (isCartOpen) {
            transition.setToX(350);
            isCartOpen = false;
        } else {
            transition.setToX(0);
            isCartOpen = true;
            loadCartItems();
        }
        transition.play();
    }

    private void loadCartItems() {
        AuctionFacade.getInstance().getWatchlist(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> {
                    cartItemsContainer.getChildren().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ItemDTO item : items) {
                            VBox itemBox = new VBox(5);
                            itemBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
                            itemBox.getChildren().addAll(
                                new Label(item.getName()),
                                new Label("Giá khởi: " + String.format("%,.0f VNĐ", item.getStartingPrice())),
                                new Label("Bước giá: " + String.format("%,.0f VNĐ", item.getPriceStep()))
                            );
                            cartItemsContainer.getChildren().add(itemBox);
                        }
                    } else {
                        cartItemsContainer.getChildren().add(new Label("Chưa có sản phẩm yêu thích"));
                    }
                });
            }
            @Override
            public void onError(String err) {
                Platform.runLater(() -> cartItemsContainer.getChildren().clear());
            }
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        AuctionFacade.getInstance().logout(new ApiCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Platform.runLater(() -> switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500));
            }
            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> switchScene(event, "/view/View.fxml", "Đăng nhập hệ thống", 800, 500));
            }
        });
    }
}