# Project-Online-auction-system
A real-time online auction platform for buying and selling products through competitive bidding.

## 🚀 New Advanced Features

### 1. Auto-Bidding (Đặt giá tự động)
- **Description**: Cho phép người dùng thiết lập giá đặt tối đa và bước tăng tự động
- **How to use**:
  1. Vào trang Bid History của một phiên đấu giá
  2. Nhập Max Bid (giá tối đa bạn chấp nhận)
  3. Nhập Increment (bước tăng giá)
  4. Click "Setup Auto-Bid"
- **Benefits**: Không cần theo dõi liên tục, hệ thống tự động outbid đối thủ

### 2. Concurrent Auctions (Đấu giá đồng thời)
- **Description**: Hỗ trợ nhiều phiên đấu giá chạy song song
- **Technical**: Sử dụng ExecutorService để xử lý concurrent requests
- **Benefits**: Tăng hiệu suất và khả năng mở rộng

### 3. Auction Extension (Gia hạn phiên đấu giá)
- **Description**: Tự động gia hạn thời gian nếu có bid trong 5 phút cuối
- **Logic**: Khi có người đặt giá trong 5 phút cuối, gia hạn thêm 5 phút
- **Benefits**: Đảm bảo công bằng, tránh sniping

### 4. Real-time Updates (Cập nhật thời gian thực)
- **Description**: WebSocket server broadcast updates ngay lập tức
- **Features**:
  - Giá hiện tại cập nhật realtime
  - Số lượng bids cập nhật realtime
  - Thời gian kết thúc cập nhật realtime
- **Technical**: WebSocket server trên port 8081

### 5. Bid History Visualization (Trực quan hóa lịch sử đấu giá)
- **Description**: Biểu đồ đường cong giá theo thời gian
- **Features**:
  - Line chart hiển thị tiến trình giá
  - Thông tin giá hiện tại, tổng bids, thời gian còn lại
  - Giao diện trực quan với JavaFX Chart

## 🛠 Technical Implementation

### Server Side
- **Auction.java**: Enhanced with auto-bidding, extension logic
- **AuctionManager.java**: Concurrent processing with ExecutorService
- **AutoBid.java**: Model for automatic bidding
- **AuctionWebSocketServer.java**: WebSocket server for realtime updates
- **AutoBidDAO.java**: Database operations for auto-bids

### Client Side
- **BidHistoryController.java**: Controller for bid history visualization
- **BidHistoryView.fxml**: UI with LineChart for price curves
- **ProductViewController.java**: Updated with navigation to bid history

### Database
- **auto_bids table**: Store automatic bidding configurations
- Enhanced existing tables for better auction management

## 🚀 How to Run

### Start Server
```bash
cd Server
javac -cp "lib/*" src/main/java/com/auction/server/AuctionServer.java
java -cp "lib/*:src/main/java" com.auction.server.AuctionServer
```

### Start Client
```bash
cd Client
mvn javafx:run
```

### WebSocket Connection
Client tự động kết nối WebSocket server tại `ws://localhost:8081`

## 📊 Features Overview

| Feature | Status | Description |
|---------|--------|-------------|
| Auto-Bidding | ✅ Implemented | Automatic bidding with max limits |
| Concurrent Auctions | ✅ Implemented | Multi-threaded auction processing |
| Auction Extension | ✅ Implemented | Automatic time extension on late bids |
| Real-time Updates | ✅ Implemented | WebSocket-based live updates |
| Bid History Chart | ✅ Implemented | Visual price curve over time |

## 🔧 Dependencies

### Server
- Java WebSocket API (Java-WebSocket)
- MySQL Connector/J
- ExecutorService (built-in)

### Client
- JavaFX 11+
- Java-WebSocket client
- ControlsFX (optional for enhanced charts)
