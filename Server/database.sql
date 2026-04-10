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
CREATE TABLE items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    starting_price DOUBLE NOT NULL,
    current_max_price DOUBLE DEFAULT 0,
    highest_bidder_id INT,
    status ENUM('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED') DEFAULT 'OPEN',
    category VARCHAR(50),  -- ELECTRONICS, ART, VEHICLE
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES sellers(user_id),
    FOREIGN KEY (highest_bidder_id) REFERENCES bidders(user_id)
);

-- 5. Electronics
CREATE TABLE electronics (
    item_id INT PRIMARY KEY,
    warranty INT,
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- 6. Artworks
CREATE TABLE artworks (
    item_id INT PRIMARY KEY,
    artist VARCHAR(100),
    creation_year INT,
    material VARCHAR(100),
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- 7. Vehicles
CREATE TABLE vehicles (
    item_id INT PRIMARY KEY,
    brand VARCHAR(100),
    mileage INT,
    condition_status VARCHAR(50),
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- 8. Bids (lịch sử đấu giá)
CREATE TABLE bids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    bidder_id INT NOT NULL,
    bid_amount DOUBLE NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (bidder_id) REFERENCES bidders(user_id)
);

-- 9. Payments (thanh toán)
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    from_bidder_id INT NOT NULL,
    to_seller_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (from_bidder_id) REFERENCES bidders(user_id),
    FOREIGN KEY (to_seller_id) REFERENCES sellers(user_id)
);