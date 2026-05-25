# 📡 API Usage Guide – Online Auction System

Tài liệu mô tả toàn bộ REST API và WebSocket của hệ thống đấu giá trực tuyến,
được trích xuất trực tiếp từ source code các Servlet.

---

## Mục lục

1. [Thông tin chung](#1-thông-tin-chung)
2. [Xác thực & Phân quyền](#2-xác-thực--phân-quyền-authfilter)
3. [Auth APIs](#3-auth-apis)
4. [Item APIs](#4-item-apis)
5. [Bid APIs](#5-bid-apis)
6. [Auto-Bid API](#6-auto-bid-api)
7. [My Account APIs](#7-my-account-apis)
8. [Payment API](#8-payment-api)
9. [Watchlist API](#9-watchlist-api)
10. [Seller Rating API](#10-seller-rating-api)
11. [Admin APIs](#11-admin-apis)
12. [Image API](#12-image-api)
13. [WebSocket – Realtime Updates](#13-websocket--realtime-updates)
14. [Quy ước Response JSON](#14-quy-ước-response-json)
15. [Bảng tổng hợp tất cả Endpoint](#15-bảng-tổng-hợp-tất-cả-endpoint)

---

## 1. Thông tin chung

| Thông tin | Giá trị |
|---|---|
| Base URL (HTTP) | `http://localhost:8080` |
| Base URL (WebSocket) | `ws://localhost:8081` |
| Content-Type | `application/json; charset=UTF-8` |
| Xác thực | Session-based (HTTP Cookie `JSESSIONID`) |
| Timezone | `Asia/Ho_Chi_Minh` |

> **Lưu ý:** Tất cả API (trừ những API public) đều yêu cầu đã đăng nhập trước.
> Cookie `JSESSIONID` được tự động quản lý bởi `java.net.http.HttpClient` phía Client.

---

## 2. Xác thực & Phân quyền (AuthFilter)

`AuthFilter` áp dụng cho tất cả URL khớp `/api/*`.

### Các endpoint **không** cần đăng nhập (public)

| Endpoint | Điều kiện |
|---|---|
| `POST /api/login` | Luôn public |
| `POST /api/register` | Luôn public |
| `GET /api/items` | Chỉ method GET |
| `GET /api/items/{itemId}` | Luôn public |
| `GET /api/items/{itemId}/bids` | Luôn public |
| `GET /api/search/items` | Luôn public |
| `GET /api/categories/*` | Luôn public |
| `GET /api/images/{filename}` | Luôn public |

### Các endpoint yêu cầu quyền đặc biệt

| Endpoint | Role yêu cầu |
|---|---|
| `POST /api/items` (đăng sản phẩm) | `SELLER` hoặc `ADMIN` |
| `GET/POST/PUT /api/admin/*` | `ADMIN` |

### Lỗi xác thực

```json
// 401 Unauthorized – chưa đăng nhập
{ "status": "error", "message": "Bạn cần đăng nhập để thực hiện chức năng này." }

// 403 Forbidden – không đủ quyền
{ "status": "error", "message": "Bạn không có quyền thực hiện hành động này." }
```

---

## 3. Auth APIs

### 3.1 Đăng nhập

```
POST /api/login
```

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response thành công (200 OK):**
```json
{
  "status": "success",
  "data": {
    "userId": 1,
    "role": "BIDDER"
  }
}
```
> Server đồng thời tạo `HttpSession`, set Cookie `JSESSIONID` (TTL: 30 phút) và lưu `userId`, `userRole` vào session.

**Response thất bại (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Email hoặc mật khẩu không đúng."
}
```

---

### 3.2 Đăng xuất

```
POST /api/logout
```

Không cần request body. Server gọi `session.invalidate()`.

**Response (200 OK – luôn trả 200):**
```json
{
  "status": "success",
  "message": "Đã đăng xuất thành công."
}
```

---

### 3.3 Đăng ký tài khoản

```
POST /api/register
```

**Request body:**
```json
{
  "username": "nguyen_van_a",
  "email": "nva@example.com",
  "password": "securePass123",
  "role": "BIDDER"
}
```

> Giá trị `role` hợp lệ: `"BIDDER"`, `"SELLER"`, `"ADMIN"`

**Response thành công (201 Created):**
```json
{
  "status": "success",
  "message": "Đăng ký thành công."
}
```

**Response thất bại (409 Conflict – email đã tồn tại):**
```json
{
  "status": "error",
  "message": "Email đã được sử dụng."
}
```

---

## 4. Item APIs

### 4.1 Lấy danh sách sản phẩm (có phân trang)

```
GET /api/items?page=1&limit=10
```

**Query parameters:**

| Tham số | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `page` | int | 1 | Số trang (bắt đầu từ 1) |
| `limit` | int | 10 | Số sản phẩm mỗi trang |

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "itemId": 1,
      "name": "Xe máy Honda Wave",
      "description": "Xe cũ ít sử dụng",
      "startingPrice": 5000000.0,
      "category": "VEHICLE",
      "imagePath": "uuid_filename.jpg",
      "sellerId": 3,
      "auctionId": 12,
      "currentMaxPrice": 6500000.0,
      "auctionStatus": "RUNNING",
      "startTime": "2026-05-25T10:00:00",
      "endTime": "2026-05-25T12:00:00"
    }
  ],
  "totalItems": 42,
  "currentPage": 1,
  "totalPages": 5
}
```

---

### 4.2 Lấy chi tiết sản phẩm

```
GET /api/items/{itemId}
```

**Path parameter:** `itemId` – ID của sản phẩm (số nguyên dương)

**Response (200 OK) – ví dụ category VEHICLE:**
```json
{
  "status": "success",
  "data": {
    "itemId": 1,
    "name": "Xe máy Honda Wave",
    "description": "Xe cũ ít sử dụng",
    "startingPrice": 5000000.0,
    "category": "VEHICLE",
    "imagePath": "uuid_filename.jpg",
    "sellerId": 3,
    "auctionId": 12,
    "currentMaxPrice": 6500000.0,
    "auctionStatus": "RUNNING",
    "startTime": "2026-05-25T10:00:00",
    "endTime": "2026-05-25T12:00:00",
    "priceStep": 200000.0,
    "brand": "Honda",
    "model": "Wave Alpha",
    "year": 2020,
    "mileage": 15000.0,
    "condition": "Tốt"
  }
}
```

> **Lưu ý:** Trường đặc thù theo category:
> - `VEHICLE`: `brand`, `model`, `year`, `mileage`, `condition`
> - `ART`: `artist`, `medium`, `dimensions`, `yearCreated`
> - `ELECTRONICS`: `brand`, `model`, `specifications`, `warranty`

**Response (404 Not Found):**
```json
{ "status": "error", "message": "Không tìm thấy sản phẩm." }
```

---

### 4.3 Lấy lịch sử đặt giá của một sản phẩm

```
GET /api/items/{itemId}/bids
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "bidId": 101,
      "bidderId": 5,
      "bidderName": "Nguyen Van A",
      "bidAmount": 6500000.0,
      "bidTime": "2026-05-25T10:45:00"
    },
    {
      "bidId": 98,
      "bidderId": 7,
      "bidderName": "Tran Thi B",
      "bidAmount": 6200000.0,
      "bidTime": "2026-05-25T10:30:00"
    }
  ]
}
```

---

### 4.4 Tìm kiếm sản phẩm

```
GET /api/search/items?q=honda&page=1&limit=10
```

**Query parameters:**

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `q` | string | Không | Từ khóa tìm kiếm (tên sản phẩm) |
| `page` | int | Không | Số trang (mặc định: 1) |
| `limit` | int | Không | Số kết quả/trang (mặc định: 10) |

**Response:** Tương tự endpoint `GET /api/items`.

---

### 4.5 Lọc sản phẩm theo danh mục

```
GET /api/categories/{categoryName}/items?page=1&limit=10
```

**Path parameter `categoryName`:** `VEHICLE`, `ART`, `ELECTRONICS`, `GENERAL`

**Response:** Tương tự endpoint `GET /api/items`.

---

### 4.6 Lấy danh sách danh mục

```
GET /api/categories/
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": ["VEHICLE", "ART", "ELECTRONICS", "GENERAL"]
}
```

---

### 4.7 Đăng bán sản phẩm mới

```
POST /api/items
```
> 🔐 **Yêu cầu role:** `SELLER` hoặc `ADMIN`

**Request body (ví dụ VEHICLE):**
```json
{
  "name": "Xe máy Honda Wave Alpha 2020",
  "description": "Xe gia đình, ít sử dụng, bảo dưỡng đầy đủ",
  "startingPrice": 5000000,
  "priceStep": 200000,
  "category": "VEHICLE",
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgAB...",
  "startTime": "2026-05-26T08:00:00",
  "endTime": "2026-05-26T20:00:00",
  "brand": "Honda",
  "model": "Wave Alpha",
  "year": 2020,
  "mileage": 15000,
  "condition": "Tốt"
}
```

> `imageBase64` là ảnh được encode base64 từ Client (phần header `data:image/...;base64,` có thể có hoặc không).
> Server sẽ decode và lưu vào thư mục `uploads/` với tên file dạng UUID.

**Response (201 Created):**
```json
{
  "status": "success",
  "message": "Đăng sản phẩm thành công.",
  "data": { "itemId": 15 }
}
```

---

### 4.8 Cập nhật sản phẩm

```
PUT /api/items/{itemId}
```
> 🔐 **Yêu cầu:** Đã đăng nhập, là chủ sản phẩm (sellerId phải khớp với session userId)

**Request body:** Tương tự `POST /api/items`, có thể gửi chỉ các trường cần thay đổi.

**Response (200 OK):**
```json
{ "status": "success", "message": "Cập nhật sản phẩm thành công." }
```

---

## 5. Bid APIs

### 5.1 Đặt giá thủ công

```
POST /api/bids
```
> 🔐 **Yêu cầu:** Đã đăng nhập. `userId` lấy từ session.

**Request body:**
```json
{
  "auctionId": 12,
  "bidAmount": 6700000
}
```

**Response thành công (200 OK):**
```json
{
  "status": "success",
  "message": "Đặt giá thành công.",
  "data": {
    "newPrice": 6700000.0,
    "bidderId": 5,
    "bidTime": "2026-05-25T11:00:00"
  }
}
```
> Sau khi thành công, Server tự động:
> 1. Gọi `AuctionManager.notifyUpdate()` → WebSocket broadcast giá mới đến tất cả Client trong phòng
> 2. Gọi `BidService.triggerAutoBid()` → kích hoạt chuỗi Auto-Bid nếu có

**Response thất bại (400 Bad Request):**
```json
{ "status": "error", "message": "Số dư không đủ để đặt giá." }
```

```json
{ "status": "error", "message": "Giá đặt phải cao hơn giá hiện tại ít nhất 200000 VND (bước giá)." }
```

```json
{ "status": "error", "message": "Phiên đấu giá đã kết thúc." }
```

---

### 5.2 Lấy lịch sử đặt giá của bản thân

```
GET /api/my/active-bids
```
> 🔐 **Yêu cầu:** Đã đăng nhập. Trả về danh sách phiên đấu giá mà user đang tham gia.

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "auctionId": 12,
      "itemName": "Xe máy Honda Wave",
      "myLastBid": 6700000.0,
      "currentMaxPrice": 6700000.0,
      "endTime": "2026-05-25T12:00:00",
      "isHighestBidder": true
    }
  ]
}
```

---

## 6. Auto-Bid API

Auto-Bid tự động đặt giá thay cho người dùng khi bị người khác vượt qua, cho đến khi đạt `max_amount`.

### 6.1 Bật Auto-Bid

```
POST /api/autobid
```
> 🔐 **Yêu cầu:** Đã đăng nhập.

**Request body:**
```json
{
  "auction_id": 12,
  "max_amount": 10000000,
  "price_step": 200000
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `auction_id` | int | ✅ | ID phiên đấu giá |
| `max_amount` | double | ✅ | Giá tối đa sẵn sàng trả (phải > 0) |
| `price_step` | double | Không | Bước giá mỗi lần auto (mặc định: 50000) |

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "Đã bật auto-bid và hệ thống đã tự đẩy giá nếu đủ điều kiện.",
  "data": {
    "active": true,
    "auto_bid_executed": true
  }
}
```

> `auto_bid_executed: false` nghĩa là đã lưu cấu hình thành công nhưng hiện tại giá của bạn đang là cao nhất nên chưa cần đấu tự động.

---

### 6.2 Kiểm tra trạng thái Auto-Bid

```
GET /api/autobid?auction_id=12
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "active": true,
    "max_amount": 10000000.0,
    "price_step": 200000.0
  }
}
```

Nếu chưa bật Auto-Bid:
```json
{
  "status": "success",
  "data": {
    "active": false,
    "max_amount": null,
    "price_step": null
  }
}
```

---

### 6.3 Tắt Auto-Bid

```
DELETE /api/autobid?auction_id=12
```

**Response (200 OK):**
```json
{ "status": "success", "message": "Đã tắt auto-bid." }
```

**Response (500) – nếu không tắt được:**
```json
{ "status": "error", "message": "Không tắt được auto-bid." }
```

---

## 7. My Account APIs

### 7.1 Xem thông tin cá nhân

```
GET /api/my/profile
```
> 🔐 **Yêu cầu:** Đã đăng nhập.

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "userId": 5,
    "username": "nguyen_van_a",
    "email": "nva@example.com",
    "role": "BIDDER",
    "isActive": true,
    "accountBalance": 15000000.0,
    "totalRating": 4.5,
    "saleCount": 3
  }
}
```

---

### 7.2 Đổi mật khẩu

```
PUT /api/my/profile
```
> 🔐 **Yêu cầu:** Đã đăng nhập.

**Request body:**
```json
{
  "oldPassword": "oldPass123",
  "newPassword": "newSecurePass456"
}
```

**Response (200 OK):**
```json
{ "status": "success", "message": "Đổi mật khẩu thành công." }
```

**Response (400):**
```json
{ "status": "error", "message": "Mật khẩu cũ không đúng." }
```

---

### 7.3 Nạp tiền vào tài khoản

```
POST /api/my/deposit
```
> 🔐 **Yêu cầu:** Đã đăng nhập, role `BIDDER`.

**Request body:**
```json
{
  "amount": 5000000
}
```

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "Nạp tiền thành công.",
  "data": {
    "newBalance": 20000000.0
  }
}
```

---

### 7.4 Danh sách sản phẩm đã thắng

```
GET /api/my/won-items
```
> 🔐 **Yêu cầu:** Đã đăng nhập, role `BIDDER`.

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "itemId": 3,
      "itemName": "Tranh sơn dầu",
      "auctionId": 8,
      "finalPrice": 2500000.0,
      "endTime": "2026-05-24T15:00:00",
      "paymentStatus": "PENDING"
    }
  ]
}
```

---

### 7.5 Danh sách sản phẩm đang bán (Seller)

```
GET /api/my/items
```
> 🔐 **Yêu cầu:** Đã đăng nhập, role `SELLER`.

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "itemId": 10,
      "name": "Laptop Dell XPS",
      "category": "ELECTRONICS",
      "startingPrice": 15000000.0,
      "auctionStatus": "RUNNING",
      "currentMaxPrice": 17000000.0,
      "endTime": "2026-05-25T18:00:00"
    }
  ]
}
```

---

### 7.6 Xem thông tin người dùng khác

```
GET /api/users/{userId}
```
> 🔐 **Yêu cầu:** Đã đăng nhập.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Lấy thông tin người dùng thành công",
  "data": {
    "userId": 3,
    "username": "seller_abc",
    "email": "seller@example.com",
    "role": "SELLER",
    "isActive": true,
    "totalRating": 4.2,
    "saleCount": 12
  }
}
```

> ⚠️ **Lưu ý:** Endpoint này dùng format response khác (trường `success` kiểu boolean thay vì `"success"` kiểu string) so với các endpoint khác.

---

## 8. Payment API

### 8.1 Thanh toán sản phẩm đã thắng

```
POST /api/payments
```
> 🔐 **Yêu cầu:** Đã đăng nhập, role `BIDDER`.

**Request body:**
```json
{
  "auctionId": 8
}
```

**Response (200 OK – luôn trả HTTP 200, kiểm tra `status` để biết kết quả):**

Thành công:
```json
{
  "status": "success",
  "message": "Thanh toán thành công. Tiền đã được chuyển cho người bán.",
  "data": {
    "paymentId": 25,
    "amount": 2500000.0,
    "paymentStatus": "COMPLETED"
  }
}
```

Thất bại (số dư không đủ):
```json
{
  "status": "error",
  "message": "Số dư không đủ để thanh toán. Cần 2500000 VND, hiện có 1000000 VND."
}
```

Thất bại (đã thanh toán rồi):
```json
{
  "status": "error",
  "message": "Phiên đấu giá này đã được thanh toán."
}
```

---

## 9. Watchlist API

Theo dõi sản phẩm yêu thích (không liên quan đến đặt giá).

### 9.1 Lấy danh sách theo dõi

```
GET /api/my/watchlist
```
> 🔐 **Yêu cầu:** Đã đăng nhập.

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "itemId": 5,
      "name": "Guitar cổ điển",
      "category": "ART",
      "startingPrice": 3000000.0,
      "currentMaxPrice": 3500000.0,
      "auctionStatus": "RUNNING",
      "endTime": "2026-05-26T10:00:00",
      "imagePath": "uuid_guitar.jpg"
    }
  ]
}
```

---

### 9.2 Thêm sản phẩm vào danh sách theo dõi

```
POST /api/my/watchlist
```
> 🔐 **Yêu cầu:** Đã đăng nhập.

**Request body:**
```json
{
  "itemId": 5
}
```

**Response (200 OK):**
```json
{ "status": "success", "message": "Đã thêm vào danh sách theo dõi." }
```

**Response (nếu đã tồn tại):**
```json
{ "status": "error", "message": "Sản phẩm đã có trong danh sách theo dõi." }
```

---

### 9.3 Xoá sản phẩm khỏi danh sách theo dõi

> ⚠️ **Lưu ý kỹ thuật:** WatchlistAPI hiện chỉ implement `doGet` và `doPost`. Để xoá, Client sử dụng `POST` với flag `remove` hoặc gọi lại `POST` với `itemId` đã có (toggle). Tham khảo `WatchlistController.handleRemoveFromWatchlist()` trong source.

---

## 10. Seller Rating API

### 10.1 Đánh giá người bán sau khi kết thúc phiên

```
POST /api/my/rating-seller
```
> 🔐 **Yêu cầu:** Đã đăng nhập, role `BIDDER`, đã thắng phiên đó.

**Request body:**
```json
{
  "auctionId": 8,
  "sellerId": 3,
  "rating": 5
}
```

| Trường | Kiểu | Giá trị hợp lệ |
|---|---|---|
| `auctionId` | int | ID phiên đấu giá đã kết thúc |
| `sellerId` | int | ID người bán |
| `rating` | int | 1 đến 5 (sao) |

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "Cảm ơn bạn đã đánh giá người bán!"
}
```

**Response (400 – đã đánh giá rồi):**
```json
{
  "status": "error",
  "message": "Bạn đã đánh giá người bán này cho phiên đấu giá này rồi."
}
```

> Database có constraint `UNIQUE(auction_id, bidder_id)` ngăn đánh giá trùng.

---

### 10.2 Kiểm tra đã đánh giá chưa

```
GET /api/my/rating-seller?auctionId=8
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "hasRated": true,
    "rating": 5
  }
}
```

Nếu chưa đánh giá:
```json
{
  "status": "success",
  "data": {
    "hasRated": false
  }
}
```

---

## 11. Admin APIs

> 🔐 **Tất cả Admin API yêu cầu role `ADMIN`.**

### 11.1 Lấy danh sách tất cả người dùng

```
GET /api/admin/users/
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "userId": 1,
      "username": "admin",
      "email": "admin@system.com",
      "role": "ADMIN",
      "isActive": true,
      "accountBalance": 0.0
    },
    {
      "userId": 5,
      "username": "nguyen_van_a",
      "email": "nva@example.com",
      "role": "BIDDER",
      "isActive": true,
      "accountBalance": 15000000.0
    }
  ]
}
```

---

### 11.2 Cập nhật role người dùng

```
PUT /api/admin/users/{userId}
```

**Request body:**
```json
{
  "role": "SELLER"
}
```

**Response (200 OK):**
```json
{ "status": "success", "message": "Cập nhật vai trò thành công." }
```

---

### 11.3 Khóa/Mở khóa tài khoản người dùng

```
POST /api/admin/users/{userId}
```

**Request body:**
```json
{
  "isActive": false
}
```

> `isActive: false` = khóa tài khoản, `true` = mở khóa.

**Response (200 OK):**
```json
{ "status": "success", "message": "Đã cập nhật trạng thái tài khoản." }
```

---

### 11.4 Xóa sản phẩm (Admin)

```
DELETE /api/admin/items/{itemId}
```

**Response (200 OK):**
```json
{ "status": "success", "message": "Đã xóa sản phẩm." }
```

**Response (404 Not Found):**
```json
{ "status": "error", "message": "Không tìm thấy sản phẩm." }
```

---

## 12. Image API

### 12.1 Lấy ảnh sản phẩm

```
GET /api/images/{filename}
```

**Path parameter:** `filename` – tên file ảnh (UUID format, ví dụ: `a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg`)

**Response thành công (200 OK):** Binary image data với Content-Type phù hợp:
- `image/jpeg` – file `.jpg`, `.jpeg`
- `image/png` – file `.png`
- `image/gif` – file `.gif`
- `image/webp` – file `.webp`

Header kèm theo:
```
Cache-Control: public, max-age=3600
Content-Length: <file_size>
```

**Response (404 Not Found):**
```json
{ "error": "Không tìm thấy ảnh: filename.jpg" }
```

**Response (403 Forbidden – path traversal bị chặn):**
```json
{ "error": "Tên file không hợp lệ." }
```

> **Bảo mật:** Server từ chối các filename chứa `..`, `/`, `\` để ngăn path traversal attack.

> **Cách dùng trong JavaFX Client:**
> ```java
> String imageUrl = "http://localhost:8080/api/images/" + item.getImagePath();
> image = new Image(imageUrl);
> ```

---

## 13. WebSocket – Realtime Updates

### Kết nối

```
ws://localhost:8081
```

Client kết nối WebSocket để nhận cập nhật giá theo thời gian thực mà không cần polling.

### Luồng hoạt động

```
[Bidder A đặt giá]
       ↓
  PlaceBidAPI → BidService.placeBid()
       ↓
  AuctionManager.notifyUpdate(AuctionUpdate)
       ↓
  AuctionWebSocketServer.onAuctionUpdate()
       ↓
  broadcastPool.submit(→ gửi JSON đến tất cả WebSocket client)
       ↓
  [Tất cả Client đang kết nối nhận được message]
```

### Format message nhận được từ Server

```json
{
  "auctionId": 12,
  "currentMaxPrice": 6700000.0,
  "highestBidderId": 5,
  "highestBidderName": "Nguyen Van A",
  "endTime": "2026-05-25T12:05:00",
  "status": "RUNNING",
  "bidTime": "2026-05-25T11:00:00"
}
```

> `endTime` có thể bị gia hạn thêm 5 phút nếu bid xảy ra trong vòng 5 phút trước khi kết thúc (tính năng time extension).

### Client-side (JavaFX – `AuctionWebSocketClient.java`)

```java
// Kết nối
client = new AuctionWebSocketClient(new URI("ws://localhost:8081"));
client.connect();

// Nhận message
@Override
public void onMessage(String message) {
    // Parse JSON → cập nhật UI trên JavaFX Application Thread
    Platform.runLater(() -> {
        AuctionUpdate update = gson.fromJson(message, AuctionUpdate.class);
        updateBiddingRoomUI(update);
    });
}
```

---

## 14. Quy ước Response JSON

### Cấu trúc chuẩn (hầu hết endpoint)

```json
{
  "status": "success" | "error",
  "message": "Thông báo (nếu có)",
  "data": { /* Dữ liệu trả về, tùy endpoint */ }
}
```

### Ngoại lệ

| Endpoint | Format khác |
|---|---|
| `GET /api/users/{userId}` | Dùng `"success": true/false` (boolean) thay vì string |
| `POST /api/payments` | Luôn trả HTTP 200, phân biệt kết quả qua `status` field |
| `GET /api/images/{filename}` | Trả binary data, không phải JSON |

### HTTP Status Codes

| Code | Ý nghĩa |
|---|---|
| 200 OK | Thành công |
| 201 Created | Tạo mới thành công (register, POST items) |
| 400 Bad Request | Dữ liệu đầu vào không hợp lệ |
| 401 Unauthorized | Chưa đăng nhập |
| 403 Forbidden | Không đủ quyền |
| 404 Not Found | Tài nguyên không tồn tại |
| 409 Conflict | Trùng lặp dữ liệu (email đã dùng) |
| 500 Internal Server Error | Lỗi phía Server |

---

## 15. Bảng tổng hợp tất cả Endpoint

| Method | Endpoint | Auth | Role | Mô tả |
|---|---|---|---|---|
| POST | `/api/login` | ❌ | Any | Đăng nhập |
| POST | `/api/logout` | ✅ | Any | Đăng xuất |
| POST | `/api/register` | ❌ | Any | Đăng ký tài khoản |
| GET | `/api/items` | ❌ | Any | Danh sách sản phẩm (phân trang) |
| GET | `/api/items/{id}` | ❌ | Any | Chi tiết sản phẩm |
| PUT | `/api/items/{id}` | ✅ | SELLER | Cập nhật sản phẩm |
| GET | `/api/items/{id}/bids` | ❌ | Any | Lịch sử bid của sản phẩm |
| POST | `/api/items` | ✅ | SELLER/ADMIN | Đăng bán sản phẩm mới |
| GET | `/api/search/items?q=` | ❌ | Any | Tìm kiếm sản phẩm |
| GET | `/api/categories/` | ❌ | Any | Danh sách category |
| GET | `/api/categories/{cat}/items` | ❌ | Any | Sản phẩm theo category |
| POST | `/api/bids` | ✅ | BIDDER | Đặt giá thủ công |
| GET | `/api/autobid?auction_id=` | ✅ | BIDDER | Kiểm tra trạng thái Auto-Bid |
| POST | `/api/autobid` | ✅ | BIDDER | Bật Auto-Bid |
| DELETE | `/api/autobid?auction_id=` | ✅ | BIDDER | Tắt Auto-Bid |
| GET | `/api/my/profile` | ✅ | Any | Thông tin cá nhân |
| PUT | `/api/my/profile` | ✅ | Any | Đổi mật khẩu |
| POST | `/api/my/deposit` | ✅ | BIDDER | Nạp tiền |
| GET | `/api/my/won-items` | ✅ | BIDDER | Sản phẩm đã thắng |
| GET | `/api/my/items` | ✅ | SELLER | Sản phẩm đang bán |
| GET | `/api/my/active-bids` | ✅ | BIDDER | Các bid đang hoạt động |
| POST | `/api/payments` | ✅ | BIDDER | Thanh toán sản phẩm |
| GET | `/api/my/watchlist` | ✅ | Any | Danh sách theo dõi |
| POST | `/api/my/watchlist` | ✅ | Any | Thêm vào theo dõi |
| GET | `/api/my/rating-seller?auctionId=` | ✅ | BIDDER | Kiểm tra đã đánh giá chưa |
| POST | `/api/my/rating-seller` | ✅ | BIDDER | Đánh giá người bán |
| GET | `/api/users/{id}` | ✅ | Any | Thông tin người dùng khác |
| GET | `/api/admin/users/` | ✅ | ADMIN | Danh sách tất cả user |
| PUT | `/api/admin/users/{id}` | ✅ | ADMIN | Đổi role user |
| POST | `/api/admin/users/{id}` | ✅ | ADMIN | Khóa/Mở khóa user |
| DELETE | `/api/admin/items/{id}` | ✅ | ADMIN | Xóa sản phẩm |
| GET | `/api/images/{filename}` | ❌ | Any | Lấy ảnh sản phẩm |
| WS | `ws://localhost:8081` | ❌ | Any | WebSocket realtime updates |