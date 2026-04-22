# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF9E0F?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![SparkJava](https://img.shields.io/badge/SparkJava-000000?style=for-the-badge)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge)

Dự án Hệ thống Đấu giá Trực tuyến là bài tập lớn môn Lập trình nâng cao. Hệ thống áp dụng kiến trúc Client-Server, sử dụng JavaFX cho giao diện người dùng, SparkJava cho RESTful API, WebSocket cho tính năng cập nhật giá thời gian thực (Real-time), và MySQL để quản lý dữ liệu.

---

## 1. Khả năng áp dụng lý thuyết đã học
Dự án được xây dựng dựa trên các nền tảng kiến thức cốt lõi của môn học:

* **Tư duy Hướng đối tượng (OOP):**
    * **Kế thừa & Đa hình:** Xây dựng các lớp trừu tượng `Item` và `User`. Sử dụng đa hình để xử lý chung các loại sản phẩm (`Art`, `Electronics`, `Vehicle`) và vai trò người dùng (`Bidder`, `Seller`, `Admin`).
    * **Đóng gói (Encapsulation):** Mọi thuộc tính của các thực thể đều được bảo vệ bằng `private` và truy cập qua `Getter/Setter`. Các logic nghiệp vụ phức tạp được giấu trong các lớp `Service`.
* **Mẫu thiết kế (Design Patterns):**
    * **Factory Pattern:** Sử dụng `ItemFactory` để tự động phân luồng và lắp ráp các đối tượng `Item` từ nhiều bảng dữ liệu con trong MySQL dựa trên chuỗi `Category`.
    * **DAO Pattern (Data Access Object):** Tách biệt hoàn toàn logic truy vấn CSDL (SQL) khỏi logic nghiệp vụ (Service), đảm bảo nguyên tắc Single Responsibility.
    * **Singleton Pattern:** Áp dụng cho các lớp quản lý tập trung như `DatabaseConnection`, `AuctionManager`, và các `Service` để tiết kiệm bộ nhớ và tránh xung đột tài nguyên.
    * **Observer Pattern:** Triển khai qua `WebSocket` và `AuctionUpdateListener` để phát sóng (broadcast) trạng thái đấu giá ngay lập tức tới tất cả Client đang kết nối.
    * **DTO (Data Transfer Object):** Sử dụng các lớp DTO để che giấu thông tin nhạy cảm và tối ưu hóa việc truyền/nhận JSON giữa Client và Server.
* **Cơ sở dữ liệu & Transaction:** Thiết kế cơ sở dữ liệu chuẩn hóa, sử dụng Khóa ngoại (Foreign Keys) với `ON DELETE CASCADE`. Áp dụng cơ chế **Transaction (Rollback/Commit)** để đảm bảo an toàn tuyệt đối cho các nghiệp vụ thanh toán (`PaymentDAO`) và đăng bán sản phẩm đa bảng (`ItemDAO`).
* **Xử lý Đa luồng (Multithreading):**
    * **Server:** Sử dụng `ExecutorService` trong `AuctionManager` để xử lý đồng thời nhiều luồng đặt giá (Concurrent Bidding) mà không bị "thắt cổ chai".
    * **Client:** Áp dụng `Platform.runLater()` để đẩy dữ liệu từ luồng mạng (Network Thread) lên luồng giao diện (UI Thread) an toàn trong JavaFX.

---

## 2. Tiến độ hiện tại (Đạt 90%)

* **Database:** Đã hoàn thiện Schema MySQL với 10 bảng (Class Table Inheritance).
* **Backend (Server):** * Đã hoàn thiện toàn bộ hệ thống DAO, Services.
    * Xây dựng thành công `AuctionManager` xử lý tự động đấu giá.
    * Mở cổng WebSocket chạy độc lập cho Real-time Bidding.
* **Client (UI):** Dựng xong toàn bộ màn hình bằng JavaFX (Login, Product, BiddingRoom, Seller/Admin Dashboard).
* **Giao tiếp (API):** Hoàn thiện bộ Model/DTO đồng nhất giữa Client và Server, hỗ trợ Serialize JSON qua Gson. Đã xây dựng khung xử lý `ApiClient` ở Client.

---

## 3. Kế hoạch hoàn thiện (Giai đoạn nước rút)

* **Tích hợp API:** Khởi chạy `SparkJava` trên Server để mở các API Endpoint. Nối `ApiClient` ở giao diện JavaFX vào các API này để xóa bỏ hoàn toàn dữ liệu ảo (Mock Data).
* **Ghép nối WebSocket:** Hoàn thiện luồng Đặt giá (Bid) tại màn hình `BiddingRoom`. Đảm bảo giá nhảy trực tiếp trên màn hình của tất cả những người đang xem.
* **Kiểm thử tích hợp (Integration Testing):** Kiểm tra các ngoại lệ (Exception) khi nhập sai dữ liệu, mất kết nối mạng, hoặc thao tác đa luồng đồng thời.

---

## 4. Khó khăn đã giải quyết

* **Kiến trúc Database:** Quyết định sử dụng mô hình Class Table Inheritance (Bảng cha `items`, bảng con `artworks`, `vehicles`...) kết hợp với Factory Pattern để giải quyết bài toán Đa hình (Polymorphism) khi lưu trữ dữ liệu.
* **Đồng bộ Transaction:** Xử lý thành công việc khóa dòng (`FOR UPDATE`) trong luồng Đặt giá (`BidsDAO`) và Thanh toán (`PaymentDAO`) để chống xung đột khi có nhiều người thao tác cùng lúc.
* **Bất đồng bộ giao diện:** Khắc phục lỗi `Not on FX application thread` khi nhận tín hiệu từ WebSocket.

---

## 5. Phân công công việc

| STT | Họ và tên | Vai trò | Trách nhiệm chính |
| :---: | :--- | :--- | :--- |
| 1 | **Thiện** | API Developer | Xây dựng REST API (SparkJava), xử lý JSON (Gson), thiết kế các Endpoints giao tiếp. |
| 2 | **Toàn** | Backend / Logic | Xây dựng WebSocket Server, quản lý phiên đấu giá (`AuctionManager`), xử lý đa luồng, Factory Pattern. |
| 3 | **Thành** | Database Architect | Thiết kế MySQL, viết các lớp DAO, tối ưu truy vấn SQL, quản lý Transaction và Data Flow. |
| 4 | **Đại** | Client / UI | Thiết kế giao diện JavaFX, xử lý luồng sự kiện UI, tích hợp `ApiClient` và hứng tín hiệu WebSocket. |

---

## 6. Sơ đồ Thiết kế Lớp (UML Class Diagram)

Sơ đồ dưới đây mô tả kiến trúc tổng thể của hệ thống, minh họa rõ sự phân tách giữa các Tầng (Layers) và việc áp dụng Mẫu thiết kế (Design Patterns).

```mermaid
classDiagram
    %% ==================================
    %% TẦNG MODELS (Thực thể lõi)
    %% ==================================
    class Entity {
        <<abstract>>
        #id: int
    }

    class User {
        <<abstract>>
        -username: String
        -password: String
        -email: String
        -isActive: boolean
        +showDashboard()*
    }

    class Item {
        <<abstract>>
        -name: String
        -startingPrice: double
        -category: String
        +printInfo()*
    }

    Entity <|-- User
    Entity <|-- Item

    class Bidder {
        -accountBalance: double
        +placeBid(amount)
    }
    class Seller {
        -totalRating: double
        -myProducts: List~Item~
    }
    class Admin {
        -adminRole: String
    }
    User <|-- Bidder
    User <|-- Seller
    User <|-- Admin

    class Art {
        -artist: String
        -creationYear: int
    }
    class Electronics {
        -warrantyMonths: int
    }
    class Vehicle {
        -brand: String
        -mileage: int
    }
    Item <|-- Art
    Item <|-- Electronics
    Item <|-- Vehicle

    class Auction {
        -item: Item
        -seller: User
        -status: AuctionStatus
        -bidHistory: List~BidTransaction~
        +placeBid(bidder, amount) boolean
    }
    Entity <|-- Auction

    %% ==================================
    %% TẦNG FACTORY & DTO (Truyền tải dữ liệu)
    %% ==================================
    class ItemFactory {
        <<Singleton / Static>>
        -registry: Map~String, Class~
        +getClassByCategory(String) Class
    }
    ItemFactory ..> ArtDTO : creates
    ItemFactory ..> ElectronicsDTO : creates

    class ItemDTO {
        <<abstract>>
    }

    %% ==================================
    %% TẦNG DAO (Database Access - MySQL)
    %% ==================================
    class DatabaseConnection {
        <<Singleton>>
        -instance: DatabaseConnection
        -connection: Connection
        +getInstance() DatabaseConnection
    }
    
    class ItemDAO {
        +addItem(ItemDTO) boolean
        +getItemById(int) ItemDTO
        +getAllItems() List~ItemDTO~
    }
    class BidsDAO {
        +executeBid(auctionId, bidderId, amount) boolean
    }
    class PaymentDAO {
        +processPayment(paymentId) boolean
    }
    
    ItemDAO ..> DatabaseConnection : uses
    ItemDAO ..> ItemFactory : utilizes

    %% ==================================
    %% TẦNG SERVICES (Nghiệp vụ cốt lõi)
    %% ==================================
    class AuctionManager {
        <<Singleton>>
        -auctions: List~Auction~
        -auctionExecutor: ExecutorService
        +processBidRequest(...)
    }
    class AuctionService {
        <<Singleton>>
        +placeBid(auctionId, bidderId, amount) String
        +closeAuction(auctionId) String
    }
    AuctionService ..> BidsDAO : delegates

    %% ==================================
    %% TẦNG CONTROLLERS & MẠNG (Server)
    %% ==================================
    class ItemController {
        +handleAddItem(jsonRequest) String
    }
    ItemController ..> ItemService : calls
    ItemController ..> ItemFactory : parsing

    class AuctionWebSocketServer {
        <<Observer>>
        +onMessage()
        +onAuctionUpdate()
    }
    AuctionWebSocketServer ..|> AuctionUpdateListener
    AuctionManager --> AuctionWebSocketServer : notifies

    %% ==================================
    %% TẦNG CLIENT (Giao diện JavaFX)
    %% ==================================
    class ApiClient {
        <<HTTP Client>>
        +post(endpoint, jsonBody) String
        +get(endpoint) String
    }
    class BiddingRoomController {
        +handleBidAction()
    }
    BiddingRoomController ..> ApiClient : REST (HTTP)
    BiddingRoomController ..> AuctionWebSocketServer : WebSocket
