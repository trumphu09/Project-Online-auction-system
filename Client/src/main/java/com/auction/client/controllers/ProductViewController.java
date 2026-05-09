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

public class ProductViewController extends BaseController {

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

    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        loadAllItems();        // Tải toàn bộ sản phẩm
        loadUserProfile();     // Tải thông tin người dùng & số dư ví
        loadBidderHistory();   // Tải lịch sử đấu giá
    }

    // ==========================================
    // PHẦN 1: TẢI & HIỂN THỊ SẢN PHẨM
    // ==========================================
    private void loadAllItems() {
        AuctionFacade.getInstance().getAllItems(new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> updateProductGrid(items));
            }
            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> showAlert("Lỗi tải dữ liệu", errorMessage));
            }
        });
    }

    private void updateProductGrid(List<ItemDTO> items) {
        productGridContainer.getChildren().clear();
        if (items != null) {
            for (ItemDTO item : items) {
                ItemCard card = new ItemCard(item, 180, 150, true);
                card.setUserData(item); // Cất dữ liệu vào card
                card.setCursor(javafx.scene.Cursor.HAND);
                card.setOnMouseClicked(this::openBiddingRoom);
                productGridContainer.getChildren().add(card);
            }
        }
    }

    // ==========================================
    // PHẦN 2: THÔNG TIN CÁ NHÂN & NẠP TIẾN
    // ==========================================
    private void loadUserProfile() {
        AuctionFacade.getInstance().getUserProfile(new ApiCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO user) {
                Platform.runLater(() -> {
                    if (user != null) {
                        if (lblFullName != null) lblFullName.setText("Chào, " + user.getFullName());
                        if (lblBalance != null) lblBalance.setText(String.format("%,.0f VNĐ", user.getBalance()));
                    }
                });
            }
            @Override
            public void onError(String errorMessage) {
                System.err.println("Lỗi tải Profile: " + errorMessage);
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
                        loadUserProfile(); // Cập nhật lại số dư trên nhãn ngay lập tức
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
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadAllItems();
            return;
        }

        AuctionFacade.getInstance().searchItems(keyword, new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> updateProductGrid(items));
            }
            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> showAlert("Lỗi tìm kiếm", errorMessage));
            }
        });
    }

    @FXML private void handleFilterElectronics() { filterByCategory("ELECTRONICS"); }
    @FXML private void handleFilterVehicles() { filterByCategory("VEHICLE"); }
    @FXML private void handleFilterArts() { filterByCategory("ART"); }

    private void filterByCategory(String category) {
        AuctionFacade.getInstance().getItemsByCategory(category, new ApiCallback<List<ItemDTO>>() {
            @Override
            public void onSuccess(List<ItemDTO> items) {
                Platform.runLater(() -> updateProductGrid(items));
            }
            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> showAlert("Lỗi lọc danh mục", errorMessage));
            }
        });
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
                            listActiveBids.getItems().add("SP ID: " + bid.get("auction_id").getAsInt() + " | Bạn Bid: " + String.format("%,.0f", bid.get("amount").getAsDouble()));
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
      transition.setToX(300); // Trượt giấu đi
      isCartOpen = false;
    } else {
      transition.setToX(0);   // Trượt hiện ra
      isCartOpen = true;
      loadCartItems();        // Gọi dữ liệu khi mở
    }
    transition.play();
  }

  private void loadCartItems() {
    AuctionFacade.getInstance().getWatchlist(new ApiCallback<List<ItemDTO>>() {
      @Override
      public void onSuccess(List<ItemDTO> items) {
        Platform.runLater(() -> {
          cartItemsContainer.getChildren().clear();
          for (ItemDTO item : items) {
            // Tạo một Label hoặc MiniItemCard cho từng món
            cartItemsContainer.getChildren().add(new Label(item.getName() + " - " + item.getStartingPrice()));
          }
        });
      }
      @Override
      public void onError(String err) {
        // Xử lý lỗi
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