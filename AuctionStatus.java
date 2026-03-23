// AuctionStatus.java
public enum AuctionStatus {
    OPEN,       // Vừa tạo xong, chờ người vào
    RUNNING,    // Đang trong thời gian trả giá
    FINISHED,   // Đã hết giờ chốt người thắng
    PAID,       // Đã thanh toán
    CANCELED    // Bị hủy bỏ
}