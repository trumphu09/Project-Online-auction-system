# 🔨 Dự án: Hệ thống Đấu giá Trực tuyến (Online Auction System)

## 1. Khả năng áp dụng lý thuyết đã học
Dự án áp dụng chặt chẽ các nguyên lý Lập trình Hướng đối tượng (OOP) và Mẫu thiết kế (Design Patterns):
* **Tính Đa hình & Kế thừa (OOP):** Xây dựng lớp trừu tượng `Item`, kế thừa bởi `Art`, `Electronics`, `Vehicle`. Tương tự với `User` (chia thành `Bidder`, `Seller`).
* **DAO Pattern (Data Access Object):** Tách biệt hoàn toàn logic truy xuất cơ sở dữ liệu (`ItemDAO`, `UserDAO`) khỏi logic nghiệp vụ, giúp mã nguồn dễ bảo trì.
* **Singleton Pattern:** Quản lý kết nối Database qua lớp `DatabaseConnection` để tránh quá tải kết nối (Connection Leak).
* **Observer Pattern / Real-time:** Ứng dụng WebSocket để phát sóng (broadcast) trạng thái cập nhật giá thầu tới tất cả các Client theo thời gian thực.
* **DTO Pattern (Data Transfer Object):** Sử dụng `BidderDTO`, `ItemDTO` để che giấu thông tin nhạy cảm (như Password) khi truyền dữ liệu qua mạng.

## 2. Tiến độ hiện tại (Đạt ~75%)
* **Cơ sở dữ liệu:** Đã hoàn thiện Schema MySQL với các bảng được chuẩn hóa, áp dụng Khóa ngoại (Foreign Keys) đầy đủ. Các lớp DAO đã hoàn tất và vượt qua Unit Test.
* **Backend & Mạng:** Đã khởi tạo thành công WebSocket Server lắng nghe cập nhật giá. Đã xây dựng khung xử lý đấu giá đa luồng (AuctionManager).
* **Giao diện Client:** Dựng xong toàn bộ màn hình JavaFX (Đăng nhập, Trang chủ, Phòng đấu giá, Quản lý kho). Bắt đầu tích hợp bắt sự kiện UI.

## 3. Kế hoạch hoàn thiện (Đến hết kỳ)
* **Tuần này:** Triển khai REST API (SparkJava) ở Server và HTTP Client ở phía JavaFX để xóa bỏ dữ liệu giả (Mock Data), kết nối luồng Đăng nhập/Đăng ký thực tế.
* **Tuần sau:** Ghép nối hoàn chỉnh luồng đặt giá (Bid) qua WebSocket. Xử lý đa luồng an toàn trên giao diện (`Platform.runLater`).
* **Giai đoạn cuối:** Kiểm thử tích hợp toàn hệ thống, bắt các lỗi ngoại lệ đầu vào của người dùng.

## 4. Khó khăn gặp phải
* **Bất đồng bộ giao diện:** Gặp khó khăn trong việc cập nhật giao diện JavaFX từ một luồng mạng (Network Thread) của WebSocket gây ra lỗi `Not on FX application thread`.
* **Quản lý Transaction:** Giải quyết bài toán rủi ro dữ liệu khi phải `INSERT` đồng thời vào bảng cha (`items`) và bảng con (`artworks`) mà mạng bị đứt giữa chừng.

## 5. Bảng phân chia công việc

| STT | Họ và Tên | Vai trò | Mô tả công việc chi tiết |
|---|---|---|---|
| 1 | Thiện | **API Developer** | Xây dựng REST API (SparkJava), xử lý JSON (Gson), thiết kế các Endpoints giao tiếp. |
| 2 | Toàn | **Backend / Logic** | Xây dựng WebSocket Server, quản lý phiên đấu giá (AuctionManager), xử lý đa luồng. |
| 3 | Thành | **Database Architect** | Thiết kế MySQL, viết các lớp DAO, tối ưu truy vấn SQL, quản lý Transaction và DTO. |
| 4 | Đại | **Client / UI** | Thiết kế giao diện JavaFX, xử lý luồng sự kiện UI, gọi API (HTTP Client) và hứng WebSocket. |

## 6. Sơ đồ thiết kế lớp (UML Class Diagram)
```mermaid
classDiagram
    %% Tầng Models
    class Item {
        <<abstract>>
        -id: int
        -name: String
        -startingPrice: double
        +getCategory()* String
    }
    class Art {
        -artist: String
        -creationYear: int
    }
    class Electronics {
        -warranty: int
    }
    Item <|-- Art
    Item <|-- Electronics

    %% Tầng DAO
    class DatabaseConnection {
        -instance: DatabaseConnection
        -connection: Connection
        +getInstance() DatabaseConnection
        +getConnection() Connection
    }
    class ItemDAO {
        +addItem(Item) boolean
        +getAllItems() List~Item~
    }
    ItemDAO ..> DatabaseConnection : uses
    ItemDAO ..> Item : manipulates

    %% Tầng Mạng & API
    class AuctionWebSocketServer {
        +onMessage()
        +broadcastPriceUpdate()
    }
    class AuthApiServer {
        +loginEndpoint()
    }

    %% Tầng Client (JavaFX)
    class BiddingRoomController {
        -currentPrice: Label
        +handleBidAction()
    }
    class ApiClient {
        +sendGetRequest()
        +sendPostRequest()
    }
    
    BiddingRoomController ..> ApiClient : calls
    ApiClient ..> AuthApiServer : HTTP JSON
    BiddingRoomController ..> AuctionWebSocketServer : WebSocket
