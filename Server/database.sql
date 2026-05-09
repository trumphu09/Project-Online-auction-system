USE defaultdb;

-- 1. Users (chung)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('BIDDER', 'SELLER', 'ADMIN') NOT NULL,
    isActive BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bidders (riêng cho bidder)
CREATE TABLE bidders (
    user_id INT PRIMARY KEY,
    account_balance DOUBLE DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 3. Sellers (riêng cho seller)
CREATE TABLE sellers (
    user_id INT PRIMARY KEY,
    total_rating DOUBLE DEFAULT 0,
    sale_count INT DEFAULT 0,
    account_balance DOUBLE DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 4. Items (sản phẩm)
-- 1. BẢNG CHA: Lưu thông tin cơ bản của mọi mặt hàng
CREATE TABLE items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    starting_price DOUBLE NOT NULL,
    category VARCHAR(50) NOT NULL, -- Sẽ lưu giá trị 'ART', 'ELECTRONICS', hoặc 'VEHICLE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_path VARCHAR(255),    
    
    
    CONSTRAINT fk_items_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================
-- CÁC BẢNG CON (Dùng item_id làm Khóa chính kiêm Khóa ngoại)
-- ==========================================

-- 2. BẢNG CON 1: Nghệ thuật
CREATE TABLE artworks (
    item_id INT PRIMARY KEY,       -- Vừa là ID của bảng này, vừa trỏ về bảng items
    artist VARCHAR(100),
    creation_year INT,
    material VARCHAR(100),
    
    CONSTRAINT fk_art_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- 3. BẢNG CON 2: Đồ điện tử
CREATE TABLE electronics (
    item_id INT PRIMARY KEY,
    warranty_months INT,
    
    CONSTRAINT fk_elec_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- 4. BẢNG CON 3: Xe cộ
CREATE TABLE vehicles (
    item_id INT PRIMARY KEY,
    brand VARCHAR(100),
    mileage INT,
    condition_state VARCHAR(50),   -- Đổi tên thành condition_state để tránh trùng từ khóa SQL
    
    CONSTRAINT fk_vehicle_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- 8. Bids (lịch sử đấu giá)
CREATE TABLE bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    bidder_id INT NOT NULL,
    bid_amount DOUBLE NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id),
    FOREIGN KEY (bidder_id) REFERENCES bidders(user_id)
);

-- 9. Payments (thanh toán)
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,            -- ĐÃ SỬA: Chuyển thành auction_id
    from_bidder_id INT NOT NULL,
    to_seller_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- ĐÃ SỬA: Trỏ khóa ngoại về đúng bảng auctions và users, kèm Xóa dây chuyền
    CONSTRAINT fk_payment_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_bidder FOREIGN KEY (from_bidder_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_seller FOREIGN KEY (to_seller_id) REFERENCES users(id) ON DELETE CASCADE
);
--10.Auctions 
CREATE TABLE auctions(
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    seller_id INT NOT NULL,
    current_max_price DOUBLE DEFAULT 0,
    highest_bidder_id INT DEFAULT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') DEFAULT 'OPEN',
    has_extended TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auctions_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT fk_auctions_seller FOREIGN KEY (seller_id) REFERENCES sellers(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_auctions_highest_bidder FOREIGN KEY (highest_bidder_id) REFERENCES bidders(user_id)
);
--11. Auto_bids: Bảng này giúp đảm bảo 1 người chỉ có 1 mức giá max cho 1 phiên đấu giá, muốn cài giá mới thì "UPDATE" lại
CREATE TABLE IF NOT EXISTS auto_bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auction_id INT NOT NULL,
    user_id INT NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_auto_bid (auction_id, user_id)
);
-- 12. Watchlist (Giỏ hàng / Danh sách theo dõi - Tính năng sán tạo)
CREATE TABLE watchlist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ràng buộc khóa ngoại để đảm bảo tính toàn vẹn dữ liệu
    CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_item FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,

    -- Đảm bảo 1 user không thể thêm 1 item vào giỏ 2 lần
    UNIQUE KEY unique_user_item (user_id, item_id)
);
