# 🏷️ Project-Online-auction-system

Hệ thống đấu giá trực tuyến theo mô hình **Client–Server**, xây dựng bằng Java thuần.  
Cho phép nhiều người dùng tham gia đặt giá theo thời gian thực, quản lý sản phẩm đa danh mục, đấu giá tự động, thanh toán tự động khi kết thúc phiên nếu có người thắng, và đánh giá người bán sau khi phiên kết thúc.

---

## 📖 Mô tả bài toán & Phạm vi hệ thống

Hệ thống gồm ba vai trò chính:

- **Bidder (Người mua):** Xem danh sách sản phẩm, tìm kiếm/lọc theo phân loại, vào phòng đấu giá, đặt giá thủ công hoặc đăng ký đấu giá tự động (Auto-Bid), theo dõi sản phẩm yêu thích (Watchlist), xem bảng lịch sử giá, nạp tiền, thanh toán tự động và đánh giá người bán.
- **Seller (Người bán):** Đăng bán sản phẩm thuộc các danh mục (Xe cộ, Nghệ thuật, Điện tử), cập nhật, tạo tự động phiên đấu giá và quản lý phiên đấu giá của mình.
- **Admin (Quản trị viên):** Quản lý toàn bộ tài khoản người dùng (khóa/mở khóa), quản lý tất cả sản phẩm trong hệ thống.

**Tính năng đặc trưng:**
- Giá đấu cập nhật **realtime** qua WebSocket — không cần refresh trang.
- **Auto-Bid:** đặt giá tự động khi bị vượt, chỉ có 1 người được cầm giá cao nhất auto-bid.
- **Tự động đóng phiên & chuyển tiền:** `AuctionStatusScheduler` chạy nền mỗi giây, tự chuyển tiền cho Seller khi phiên kết thúc.
- **Giam tiền an toàn:** tạm thời cầm tiền của người cầm giá cao nhất.
- **Gia hạn thời gian:** phiên tự cộng thêm thời gian nếu có bid vào gần cuối.

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ | Phiên bản |
|---|---|---|
| Ngôn ngữ | Java | 17 |
| Giao diện Client | JavaFX + FXML | 17 |
| HTTP Server | Embedded Apache Tomcat | 9.0.83 |
| WebSocket | Java-WebSocket (org.java_websocket) | 1.5.4 |
| Tuần tự hóa JSON | Gson | 2.10.1 |
| Cơ sở dữ liệu | MySQL | 8.0+ |
| Connection Pool | HikariCP | 5.1.0 |
| Mã hóa mật khẩu | jBCrypt | 0.4 |
| Build tool | Apache Maven | 3.8+ |
| HTTP Client (Client-side) | java.net.http.HttpClient | Java 11+ |
| Xác thực | Session-based (HTTP Cookie + HttpSession) | — |
| Testing | JUnit 5 + Mockito | 5.10 / 5.5 |

---

## ⚙️ Yêu cầu môi trường

| Yêu cầu | Phiên bản tối thiểu |
|---|---|
| JDK | **17** trở lên (đã kiểm thử với JDK 17 và JDK 21) |
| Apache Maven | 3.8 trở lên |
| MySQL | 8.0 trở lên |
| RAM | 512 MB (khuyến nghị 1 GB) |
| OS | Windows 10+, Ubuntu 20.04+, macOS 12+ |

> **Lưu ý JavaFX:** Client sử dụng `javafx-maven-plugin` để tự động xử lý module path và native libraries. Không cần cài JavaFX SDK riêng — plugin sẽ tải đúng bản native cho hệ điều hành hiện tại qua Maven.

---

## 📁 Cấu trúc thư mục

```
Project-Online-auction-system/
│
├── README.md
├── API_USAGE_GUIDE.md
│
├── Client/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/auction/client/
│           │   ├── controllers/          # Xử lý giao diện JavaFX
│           │   │   ├── AdminController.java
│           │   │   ├── BiddingRoomController.java
│           │   │   ├── BidHistoryController.java
│           │   │   ├── LoginController.java
│           │   │   ├── ProductViewController.java
│           │   │   ├── RegisterController.java
│           │   │   └── SellerController.java
│           │   ├── model/dto/            # DTO trao đổi dữ liệu
│           │   ├── network/              # HTTP Client, WebSocket Client
│           │   │   ├── ApiService.java
│           │   │   └── AuctionWebSocketClient.java
│           │   ├── service/              # Business logic phía client
│           │   ├── util/                 # Tiện ích hỗ trợ UI
│           │   ├── Main.java             # Entry point
│           │   └── MainApp.java          # JavaFX Application
│           └── resources/view/           # File FXML giao diện
│
├── Server/
│   ├── pom.xml
│   ├── database.sql                      # Script khởi tạo CSDL
│   └── src/main/java/com/auction/
│       ├── controller/                   # Điều phối request HTTP
│       ├── service/                      # Xử lý nghiệp vụ (AuctionService, BidService, …)
│       └── server/
│           ├── dao/                      # Truy cập CSDL (UserDAO, ItemDAO, BidsDAO, …)
│           ├── filters/                  # AuthFilter – xác thực request
│           ├── models/                   # Entity + DTO
│           ├── scheduler/                # AuctionStatusScheduler
│           ├── servlets/                 # REST API endpoints
│           ├── websocket/                # AuctionWebSocketServer
│           ├── utils/
│           └── AuctionServer.java        # Khởi động Server (entry point)
│
└── uploads/                              # Ảnh sản phẩm (tự tạo khi Server khởi động)
```

---

## 🗄️ Cấu hình Database

### Bước 1 — Tạo database

Đăng nhập MySQL và tạo database:

```sql
CREATE DATABASE online_auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2 — Import schema

```bash
mysql -u root -p online_auction < Server/database.sql
```

### Bước 3 — Cập nhật thông tin kết nối

Mở file `Server/src/main/java/com/auction/server/dao/DatabaseConnection.java` và chỉnh:

```java
config.setJdbcUrl("jdbc:mysql://localhost:3306/online_auction?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh");
config.setUsername("root");      // ← đổi thành username của bạn
config.setPassword("123456");    // ← đổi thành password của bạn
```

---

## 🔨 Hướng dẫn Build

### Bước 1 — Clone repository

```bash
git clone https://github.com/trumphu09/Project-Online-auction-system.git
cd Project-Online-auction-system
```

### Bước 2 — Build Server

```bash
cd Server
mvn clean package -DskipTests
```

### Bước 3 — Build Client

```bash
# Windows
cd ..\Client
mvn clean package -DskipTests

# Linux / macOS
cd ../Client
mvn clean package -DskipTests
```

---

## 🖥️ Hướng dẫn chạy

> ⚠️ **Trình tự bắt buộc:** MySQL → Server → Client(s)

### 1. Khởi động MySQL

Đảm bảo MySQL đang chạy và database `online_auction` đã được import (xem phần Cấu hình Database).

### 2. Khởi động Server

**Server phải được khởi động TRƯỚC khi mở bất kỳ Client nào.**

```bash
# Windows / Linux / macOS
cd Server
mvn exec:java -Dexec.mainClass="com.auction.server.AuctionServer"
```

Kết quả khi Server khởi động thành công:

```
=== AUCTION SERVER STARTING ===
[AuctionServer] Upload directory set to: .../uploads
✓ [HTTP API] Web Server đang chạy ở cổng 8080!
✓ [WEBSOCKET] Server started on port 8081
[SCHEDULER] Hệ thống tự động quản lý phiên đấu giá đã khởi động!
```

Các cổng sử dụng:
- **8080** — HTTP REST API (Tomcat)
- **8081** — WebSocket realtime broadcast

### 3. Khởi động Client(s)

Client sử dụng `javafx-maven-plugin` — lệnh `mvn javafx:run` tự xử lý module path JavaFX trên mọi hệ điều hành, không cần cài JavaFX SDK riêng.

Mở **nhiều terminal/cửa sổ lệnh riêng biệt**, mỗi cửa sổ chạy một Client độc lập:

```bash
# Terminal 1 — Client 1 (Windows / Linux / macOS — cú pháp giống nhau)
cd Client
mvn javafx:run

# Terminal 2 — Client 2 (mở terminal/tab mới)
cd Client
mvn javafx:run

# Terminal 3 — Client 3 (tương tự)
cd Client
mvn javafx:run
```

> **Lưu ý:** Lần chạy đầu tiên Maven sẽ tải `javafx-maven-plugin` và native JavaFX libs phù hợp với OS của bạn (~vài chục MB). Các lần sau sẽ chạy ngay lập tức từ local cache.

---

## 🌐 Chạy Client từ máy khác (khác LAN)

Nếu Server và Client **không cùng máy**, cần đổi `localhost` thành IP của máy chạy Server tại hai file sau trong Client:

| File | Dòng cần sửa |
|---|---|
| `Client/src/main/java/com/auction/client/network/ApiService.java` | `private final String BASE_URL = "http://<SERVER_IP>:8080/api";` |
| `Client/src/main/java/com/auction/client/network/AuctionWebSocketClient.java` | `private static final String WS_URL = "ws://<SERVER_IP>:8081";` |

Sau khi sửa, build lại Client: `mvn clean package -DskipTests`, rồi `mvn javafx:run`.

---

## ✅ Danh sách chức năng đã hoàn thành

### Xác thực & Tài khoản
- [x] Đăng nhập bằng email + mật khẩu (mật khẩu mã hóa BCrypt, lưu session cookie)
- [x] Đăng xuất, hủy session
- [x] Đăng ký tài khoản với vai trò: SELLER, BIDDER
- [x] Nạp tiền vào tài khoản (Deposit)
- [x] Hiển thị thông tin cá nhân (User Profile)

### Sản phẩm & Danh mục
- [x] Xem danh sách sản phẩm 
- [x] Tìm kiếm sản phẩm theo từ khóa
- [x] Lọc sản phẩm theo danh mục (VEHICLE, ART, ELECTRONICS, GENERAL)
- [x] Xem chi tiết sản phẩm (thông tin đặc thù theo từng loại)
- [x] Upload ảnh sản phẩm từ Client lên Server (base64 → file)
- [x] Người bán thêm mới / cập nhật sản phẩm của mình
- [x] Watchlist — theo dõi sản phẩm yêu thích (tính năng sáng tạo)

### Phiên Đấu giá
- [x] Tạo phiên đấu giá (Seller đặt giá khởi điểm, bước giá, thời gian)
- [x] Đặt giá thủ công (Place Bid) với kiểm tra hợp lệ
- [x] Đặt giá tự động (Auto-Bid) — tự động bid khi bị vượt, theo mức giá tối đa
- [x] Cập nhật giá realtime qua WebSocket đến tất cả Client trong phòng
- [x] Biểu đồ lịch sử giá (LineChart) cập nhật theo thời gian thực
- [x] Đồng hồ đếm ngược đến khi kết thúc phiên
- [x] Tự động kích hoạt phiên (OPEN → RUNNING) khi đến giờ bắt đầu
- [x] Tự động đóng phiên (RUNNING → FINISHED) và chuyển tiền khi hết giờ
- [x] Gia hạn thêm thời gian khi có bid gần lúc kết thúc
- [x] Giam tiền an toàn, tự động giam tiền của người đấu giá khi đặt giá thành công

### Thanh toán & Đánh giá
- [x] Tự động thanh toán sau khi thắng phiên đấu giá
- [x] Đánh giá người bán (SellerRating) bằng sao sau khi kết thúc phiên
- [x] Xem danh sách vật phẩm đã thắng (My Won Items)
- [x] Xem danh sách bid đang hoạt động (My Active Bids)

### Admin
- [x] Quản lý toàn bộ người dùng: xem, khóa/mở khóa tài khoản
- [x] Quản lý toàn bộ sản phẩm trong hệ thống

### Testing
- [x] Unit test Server: AuctionService, BidService, ItemService, PaymentService, UserService
- [x] Unit test Client: ItemDTO, UserDTO, RegisterValidator
- [x] Integration test: AuctionController, UserController, WatchlistController

---

## 🔗 Tài liệu liên quan

- 📄 **Báo cáo PDF:** [*(BÁO CÁO)*](https://drive.google.com/file/d/1UlRyY0RSwEwptj8O1y1zsziGr3Tqpz2m/view?usp=drive_link)
- 🎬 **Video Demo:** [*(VIDEO DEMO)*](https://drive.google.com/file/d/1khqk1LHRvCWxBKezDj3pk8PV3Fj389Q1/view?usp=drive_link)
- 📘 **API Usage Guide:** `API_USAGE_GUIDE.md`

---

## ⚠️ Lưu ý quan trọng

1. **Đường dẫn ảnh:** Thư mục `uploads/` sẽ được tự tạo tại thư mục gốc của Server khi khởi động.
2. **Cùng mạng:** Server và Client hiện chỉ chạy trên cùng mạng LAN/localhost. Nếu cần chạy Client từ máy khác, đổi địa chỉ `localhost` theo hướng dẫn ở mục "Chạy Client từ máy khác" phía trên.
3. **Timezone:** Cả Server và Client đều set `Asia/Ho_Chi_Minh` tự động khi khởi động.
4. **Firewall:** Đảm bảo port **8080** (HTTP) và **8081** (WebSocket) không bị firewall chặn khi chạy đa máy.