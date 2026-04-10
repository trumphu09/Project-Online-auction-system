package com.auction.server.models;

enum PaymentStatus {
    PENDING,    // Chờ xử lý
    COMPLETED,  // Đã hoàn thành
    FAILED      // Thất bại
}