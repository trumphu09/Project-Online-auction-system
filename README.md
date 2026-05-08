# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF9E0F?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![SparkJava](https://img.shields.io/badge/SparkJava-000000?style=for-the-badge)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge)
![HikariCP](https://img.shields.io/badge/HikariCP-Connection_Pooling-00B4AB?style=for-the-badge)

Dự án Hệ thống Đấu giá Trực tuyến là bài tập lớn môn Lập trình nâng cao. Hệ thống áp dụng kiến trúc Client-Server, sử dụng JavaFX cho giao diện người dùng, SparkJava cho RESTful API, WebSocket cho tính năng cập nhật giá thời gian thực (Real-time), và MySQL để quản lý dữ liệu.

---

## 1. Khả năng áp dụng lý thuyết & Kiến trúc phần mềm
Dự án được xây dựng dựa trên các nền tảng kiến thức cốt lõi và các tiêu chuẩn khắt khe trong thiết kế phần mềm:

* **Tư duy Hướng đối tượng (OOP) & SOLID:**
    * **Tuân thủ tuyệt đối OCP (Open/Closed Principle):** Hệ thống khởi tạo và truy vấn dữ liệu được thiết kế dạng "Plug-and-Play". Khi cần bán thêm loại sản phẩm mới (ví dụ: Bất động sản), chỉ cần đăng ký Class mới vào hệ thống mà **không cần sửa đổi** bất kỳ câu lệnh `switch-case` hay `if-else` nào ở các lớp lõi.
    * **Đa hình (Polymorphism):** Xử lý chung các loại sản phẩm (`Art`, `Electronics`, `Vehicle`) và vai trò người dùng (`Bidder`, `Seller`, `Admin`) qua các Interface/Abstract Class.
* **Mẫu thiết kế (Design Patterns) nâng cao:**
    * **Factory & Registry Pattern:** Sử dụng `ItemFactory` kết hợp với Bộ đăng ký (Registry Map) để tự động phân luồng và lắp ráp các đối tượng `Item` một cách linh hoạt (Dynamic Instantiation).
    * **Strategy Pattern:** Lớp `ItemDAO` đóng vai trò Context, ủy quyền việc Ghi/Đọc dữ liệu bảng con cho các chiến lược `IItemSubDAO` tương ứng (`ArtDAO`, `VehicleDAO`), giúp mã nguồn sạch và dễ bảo trì.
    * **DAO Pattern (Data Access Object):** Tách biệt hoàn toàn logic SQL khỏi logic nghiệp vụ.
    * **Singleton Pattern:** Quản lý tập trung `DatabaseConnection`, `AuctionManager`.
    * **Observer Pattern:** Phát sóng (broadcast) trạng thái đấu giá ngay lập tức tới tất cả Client qua `WebSocket`.
* **Cơ sở dữ liệu & Quản lý kết nối (Connection Pooling):** * Thiết kế CSDL mô hình **Class Table Inheritance** với 10 bảng chuẩn hóa cao (tránh NULL thừa).
    * Áp dụng thư viện **HikariCP** để quản lý Connection Pooling, khắc phục lỗi thắt cổ chai, hỗ trợ đa luồng xử lý đồng thời hàng ngàn truy vấn (High Concurrency).
    * Quản lý **Transaction (Rollback/Commit)** chặt chẽ cùng kỹ thuật khóa dòng (`FOR UPDATE`) để chống xung đột luồng tranh mua, tranh thanh toán.

---

## 2. Tiến độ hiện tại (Đạt 90%)

* **Database & Tầng DAO:**
    * Hoàn thiện Schema MySQL (10 bảng) và toàn bộ các lớp DAO.
    * Đã refactor thành công tầng DAO chuẩn OCP. Áp dụng cấu trúc `try-with-resources` nghiêm ngặt để chống rò rỉ bộ nhớ (Memory Leak).
* **Backend (Server):** * Xây dựng thành công `AuctionManager` (dùng `ExecutorService`) xử lý tự động đấu giá đa luồng.
    * Mở cổng WebSocket chạy độc lập cho Real-time Bidding.
* **Client (UI):** Dựng xong toàn bộ màn hình bằng JavaFX (Login, Product, BiddingRoom, Seller/Admin Dashboard).
* **Giao tiếp (API):** Hoàn thiện bộ Model/DTO đồng nhất. 

---

## 3. Kế hoạch hoàn thiện (Giai đoạn nước rút)

* **Tích hợp API:** Khởi chạy `SparkJava` trên Server. Nối `ApiClient` ở giao diện JavaFX vào các API này để thay thế hoàn toàn dữ liệu Mock tĩnh.
* **Ghép nối WebSocket:** Hoàn thiện luồng giao diện cập nhật tiền trực tiếp tại phòng đấu giá (`BiddingRoom`).

---

## 4. Phân công công việc

| STT | Họ và tên | Vai trò | Trách nhiệm chính |
| :---: | :--- | :--- | :--- |
| 1 | **Thiện** | API Developer | Xây dựng REST API (SparkJava), xử lý JSON (Gson), thiết kế các Endpoints giao tiếp. |
| 2 | **Toàn** | Backend / Logic | Xây dựng WebSocket Server, quản lý phiên đấu giá (`AuctionManager`), xử lý đa luồng. |
| 3 | **Thành** | Database Architect | Thiết kế MySQL, thiết kế tầng DAO chuẩn OCP, cấu hình HikariCP, quản lý Transaction SQL. |
| 4 | **Đại** | Client / UI | Thiết kế giao diện JavaFX, xử lý luồng sự kiện UI bất đồng bộ, tích hợp `ApiClient`. |

---

## 5. Sơ đồ Thiết kế Lớp (UML Class Diagram - Core Architecture)

Sơ đồ dưới đây minh họa kiến trúc OCP và các Mẫu thiết kế được áp dụng tại tầng xử lý lõi của Server.

```mermaid
classDiagram
    %% ==========================================
    %% 1. TẦNG CLIENT - JAVAFX (Giao diện & Xử lý bất đồng bộ)
    %% ==========================================
    class BiddingRoomController {
        <<JavaFX UI>>
        -lblPrice: Label
        -btnBid: Button
        +handleBidAction()
        +updateUIRafely() "Dùng Platform.runLater()"
    }

    class ApiClient {
        <<HTTP Client>>
        +postRequest(url, jsonBody) Response
        +getRequest(url) Response
    }

    class WebSocketClient {
        <<Real-time>>
        +connectToServer()
        +sendBidAmount(amount)
        +onMessageReceived(data)
    }

    BiddingRoomController --> ApiClient : Gọi API (Login, Lấy đồ)
    BiddingRoomController --> WebSocketClient : Truyền giá trực tiếp
    WebSocketClient ..> BiddingRoomController : Gọi updateUIRafely()

    %% ==========================================
    %% 2. TẦNG MẠNG (Giao thức truyền tải)
    %% ==========================================
    class Internet {
        <<Network Layer>>
        +HTTP_REST_API
        +WebSocket_WSS
    }
    ApiClient --|> Internet : JSON (Port 8080)
    WebSocketClient --|> Internet : Binary/Text (Port 8081)

    %% ==========================================
    %% 3. TẦNG SERVER - ROUTER & REAL-TIME
    %% ==========================================
    class SparkJavaRouter {
        <<API Gateway>>
        +post("/api/login", LoginHandler)
        +post("/api/items", ItemHandler)
    }

    class AuctionWebSocketServer {
        <<Observer>>
        -connectedClients: List
        +onOpen()
        +onMessage()
        +broadcastToAll()
    }

    Internet --|> SparkJavaRouter : Request
    Internet --|> AuctionWebSocketServer : Socket Stream

    %% ==========================================
    %% 4. TẦNG BUSINESS LOGIC & FACTORY (Chuẩn OCP)
    %% ==========================================
    class AuctionManager {
        <<Singleton>>
        -executor: ExecutorService "Thread Pool"
        +processBidRequest() "Xử lý kẹt xe đa luồng"
    }

    class ItemFactory {
        <<Registry Pattern>>
        -registry: Map~String, Class~
        +registerItemType()
        +getClassByCategory()
    }

    SparkJavaRouter --> ItemFactory : Map JSON -> Object
    AuctionWebSocketServer --> AuctionManager : Ném giá vào hàng đợi

    %% ==========================================
    %% 5. TẦNG DAO & DATABASE (Lõi thao tác dữ liệu)
    %% ==========================================
    class DatabaseConnection {
        <<Singleton>>
        -dataSource: HikariDataSource
        +getInstance()
        +getConnection() Connection
    }

    class BidsDAO {
        +executeBid(auctionId, userId, amount)
        "Dùng FOR UPDATE & Rollback"
    }

    class ItemDAO {
        +addItem(ItemDTO)
        +getAllItems()
    }

    class IItemSubDAO {
        <<Strategy Interface>>
        +insertSubItem(conn, item)
        +fetchSubItem()
    }
    
    class ArtDAO
    class VehicleDAO
    class ElectronicsDAO

    %% Ráp nối DAO
    ItemDAO o-- IItemSubDAO : Phân nhánh OCP
    IItemSubDAO <|.. ArtDAO : Implements
    IItemSubDAO <|.. VehicleDAO : Implements
    IItemSubDAO <|.. ElectronicsDAO : Implements

    AuctionManager --> BidsDAO : Ghi lịch sử & trừ tiền
    SparkJavaRouter --> ItemDAO : Lưu sản phẩm mới

    %% Tất cả DAO đều phải mượn ống nước từ HikariCP
    BidsDAO ..> DatabaseConnection : mượn Connection
    ItemDAO ..> DatabaseConnection : mượn Connection
    IItemSubDAO ..> DatabaseConnection : dùng chung Connection cha

    %% ==========================================
    %% 6. TẦNG VẬT LÝ (Database)
    %% ==========================================
    class MySQL {
        <<Database>>
        +Table_Users
        +Table_Items
        +Table_Artworks
        +Table_Bids
    }

    DatabaseConnection --> MySQL : HikariCP (Max 50 Connections)
