package com.auction.server.models;

public enum PaymentStatus {
    PENDING,    // Chờ xử lý
    COMPLETED,  // Đã hoàn thành
    FAILED      // Thất bại
}