package com.auction.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.auction.client.util.BaseController;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BidHistoryController extends BaseController {

    @FXML private TextField txtAuctionId;
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;
    @FXML private TextField txtBidAmount;
    @FXML private Button btnLoadHistory;
    @FXML private Button btnSetupAutoBid;
    @FXML private Button btnPlaceBid;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblTotalBids;
    @FXML private Label lblTimeRemaining;
    @FXML private LineChart<String, Number> bidHistoryChart;
    @FXML private NumberAxis priceAxis;

    private WebSocketClient webSocketClient;
    private int currentAuctionId = -1;
    private ObservableList<XYChart.Data<String, Number>> chartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bid History");
        series.setData(chartData);
        bidHistoryChart.getData().add(series);

        // Connect to WebSocket for realtime updates
        connectWebSocket();
    }

    private void connectWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI("ws://localhost:8081")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to WebSocket server");
                }

                @Override
                public void onMessage(String message) {
                    // Handle realtime updates
                    Platform.runLater(() -> handleRealtimeUpdate(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket connection closed");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void handleRealtimeUpdate(String message) {
        // Parse JSON message and update UI
        try {
            // Simple JSON parsing (in real app, use Jackson or Gson)
            String[] parts = message.replace("{", "").replace("}", "").replace("\"", "").split(",");
            int auctionId = Integer.parseInt(parts[0].split(":")[1]);
            double currentPrice = Double.parseDouble(parts[1].split(":")[1]);
            int totalBids = Integer.parseInt(parts[2].split(":")[1]);

            if (auctionId == currentAuctionId) {
                updateUI(currentPrice, totalBids);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadBidHistory(ActionEvent event) {
        try {
            currentAuctionId = Integer.parseInt(txtAuctionId.getText());
            // In real implementation, call server API to get bid history
            // For demo, we'll simulate some data
            loadMockBidHistory();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid auction ID");
        }
    }

    private void loadMockBidHistory() {
        // Clear existing data
        chartData.clear();

        // Mock bid history data
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        chartData.add(new XYChart.Data<>(now.minusMinutes(10).format(formatter), 100.0));
        chartData.add(new XYChart.Data<>(now.minusMinutes(8).format(formatter), 120.0));
        chartData.add(new XYChart.Data<>(now.minusMinutes(5).format(formatter), 150.0));
        chartData.add(new XYChart.Data<>(now.minusMinutes(2).format(formatter), 180.0));

        updateUI(180.0, 4);
    }

    @FXML
    private void setupAutoBid(ActionEvent event) {
        try {
            double maxBid = Double.parseDouble(txtMaxBid.getText());
            double increment = Double.parseDouble(txtIncrement.getText());

            if (currentAuctionId == -1) {
                showAlert("Error", "Please load an auction first");
                return;
            }

            // In real implementation, send to server
            showAlert("Success", "Auto-bid setup for max $" + maxBid + " with increment $" + increment);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for max bid and increment");
        }
    }

    @FXML
    private void placeBid(ActionEvent event) {
        try {
            double bidAmount = Double.parseDouble(txtBidAmount.getText());

            if (currentAuctionId == -1) {
                showAlert("Error", "Please load an auction first");
                return;
            }

            // In real implementation, send bid to server
            showAlert("Success", "Bid placed: $" + bidAmount);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid bid amount");
        }
    }

    private void updateUI(double currentPrice, int totalBids) {
        lblCurrentPrice.setText(String.format("Current Price: $%.2f", currentPrice));
        lblTotalBids.setText("Total Bids: " + totalBids);
        // Update time remaining logic would go here
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Product.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("Chợ Đấu Giá - Người mua");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}