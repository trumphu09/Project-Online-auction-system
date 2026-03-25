package com.auction.server.models;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class Auction {
    private final String auctionId;

    // Các đối tượng liên kết
    private final Item item;
    private final User seller;
    private final LocalDateTime endTime;

    // Tích hợp Enum thay vì String
    private AuctionStatus status;

    // Danh sách lưu lịch sử đặt giá
    private final List<BidTransaction> bidHistory;

    // Constructor: Khởi tạo phiên đấu giá
    public Auction(String auctionId, Item item, User seller, LocalDateTime endTime) {
        this.auctionId = auctionId;
        this.item = item;
        this.seller = seller;
        this.endTime = endTime;

        // Mặc định khi vừa tạo ra, trạng thái sẽ là OPEN
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new ArrayList<>();
    }

    // --- GETTER & SETTER CHO ENUM ---

    public com.auction.server.models.AuctionStatus getStatus() {
        return status;
    }

    // Set trạng thái giờ đây không cần if-else dài dòng kiểm tra lỗi chính tả nữa
    public void setStatus(com.auction.server.models.AuctionStatus newStatus) {
        this.status = newStatus;
        System.out.println(">> CẬP NHẬT: Phiên đấu giá [" + this.auctionId + "] đã chuyển trạng thái thành -> " + this.status);
    }

    // --- LOGIC ĐẶT GIÁ ---

    public boolean placeBid(Bidder bidder, double amount) {
        // Ưu điểm của Enum: Dùng dấu == hoặc != để so sánh rất nhanh và an toàn
        if (this.status != AuctionStatus.OPEN && this.status != AuctionStatus.RUNNING) {
            System.out.println("Từ chối: Phiên đấu giá không mở. Trạng thái hiện tại: " + this.status);
            return false;
        }

        double highestBid = getHighestBid();

        // Kiểm tra xem tiền đặt có lớn hơn giá hiện tại và lớn hơn giá khởi điểm không
        if (amount > highestBid && amount >= item.getStartingPrice()) {
            BidTransaction newBid = new BidTransaction(bidder, amount);
            bidHistory.add(newBid); // Lưu vào lịch sử
            System.out.println("Thành công! [" + bidder.getUsername() + "] đã chốt giá: $" + amount);
            return true;
        } else {
            System.out.println("Lỗi: Giá đặt ($" + amount + ") phải cao hơn giá hiện tại ($" + highestBid + ").");
            return false;
        }
    }

    // Lấy giá cao nhất hiện tại
    public double getHighestBid() {
        if (bidHistory.isEmpty()) {
            return 0; // Trả về 0 nếu chưa ai đặt giá
        }
        // Trả về số tiền của giao dịch cuối cùng trong danh sách
        return bidHistory.get(bidHistory.size() - 1).getBidAmount();
    }

    // Thêm hàm lấy Item để sau này tiện in thông tin ra màn hình
    public Item getItem() {
        return item;
    }

    public User getSeller() {
        return seller;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getAuctionId() {
        return auctionId;
    }
}