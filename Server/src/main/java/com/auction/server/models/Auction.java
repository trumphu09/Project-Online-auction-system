package com.auction.server.models;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Auction extends Entity {

    // Các đối tượng liên kết
    private final Item item;
    private final User seller;
    private LocalDateTime endTime;

    // Tích hợp Enum thay vì String
    private AuctionStatus status;

    // Danh sách lưu lịch sử đặt giá
    private final List<BidTransaction> bidHistory;

    // Auto-bidding support
    private final List<AutoBid> autoBids;

    // Auction extension settings
    private final int extensionMinutes = 5; // Gia hạn 5 phút nếu có bid trong 5 phút cuối
    private boolean hasExtended = false;

    // Constructor: Khởi tạo phiên đấu giá
    public Auction(int id, Item item, User seller, LocalDateTime endTime) {
        super(id);
        this.item = item;
        this.seller = seller;
        this.endTime = endTime;

        // Mặc định khi vừa tạo ra, trạng thái sẽ là OPEN
        this.status = AuctionStatus.OPEN;
        this.bidHistory = new ArrayList<>();
        this.autoBids = new CopyOnWriteArrayList<>(); // Thread-safe cho concurrent access
    }



    public com.auction.server.models.AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(com.auction.server.models.AuctionStatus newStatus) {
        this.status = newStatus;
        System.out.println(">> CẬP NHẬT: Phiên đấu giá [" + this.getId() + "] đã chuyển trạng thái thành -> " + this.status);
    }

    // --- LOGIC ĐẶT GIÁ ---

    public synchronized boolean placeBid(Bidder bidder, double amount) {
        // Ưu điểm của Enum: Dùng dấu == hoặc != để so sánh rất nhanh và an toàn
        if (this.status != AuctionStatus.OPEN && this.status != AuctionStatus.RUNNING) {
            System.out.println("Từ chối: Phiên đấu giá không mở. Trạng thái hiện tại: " + this.status);
            return false;
        }

        double highestBid = getHighestBid();

        // Kiểm tra xem tiền đặt có lớn hơn giá hiện tại và lớn hơn giá khởi điểm không
        if (amount > highestBid && amount >= item.getStartingPrice()) {
            int bidId = bidHistory.size() + 1; // Tạo ID dựa trên số lượng bids
            BidTransaction newBid = new BidTransaction(bidId, bidder, amount);
            bidHistory.add(newBid); // Lưu vào lịch sử
            System.out.println("Thành công! [" + bidder.getUsername() + "] đã chốt giá: $" + amount);

            // Auction extension logic
            checkAndExtendAuction();

            // Trigger auto-bids
            processAutoBids();

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

    // Auto-bidding methods
    public void addAutoBid(AutoBid autoBid) {
        autoBids.add(autoBid);
        System.out.println("Auto-bid added for [" + autoBid.getBidder().getUsername() + "] - Max: $" + autoBid.getMaxBidAmount());
    }

    public void removeAutoBid(Bidder bidder) {
        autoBids.removeIf(autoBid -> autoBid.getBidder().equals(bidder));
        System.out.println("Auto-bid removed for [" + bidder.getUsername() + "]");
    }

    private void processAutoBids() {
        double currentHighest = getHighestBid();

        for (AutoBid autoBid : autoBids) {
            if (autoBid.canAutoBid(currentHighest)) {
                double nextBidAmount = autoBid.getNextAutoBidAmount(currentHighest);
                if (nextBidAmount > currentHighest) {
                    // Create automatic bid
                    int bidId = bidHistory.size() + 1;
                    BidTransaction autoBidTransaction = new BidTransaction(bidId, autoBid.getBidder(), nextBidAmount);
                    bidHistory.add(autoBidTransaction);
                    System.out.println("AUTO-BID: [" + autoBid.getBidder().getUsername() + "] tự động đặt giá: $" + nextBidAmount);

                    // Update current highest for next auto-bid processing
                    currentHighest = nextBidAmount;

                    // Check if max bid reached
                    if (nextBidAmount >= autoBid.getMaxBidAmount()) {
                        autoBid.deactivate();
                        System.out.println("AUTO-BID: [" + autoBid.getBidder().getUsername() + "] đã đạt giới hạn tối đa!");
                    }
                }
            }
        }
    }

    // Auction extension methods
    private void checkAndExtendAuction() {
        if (!hasExtended && isNearEndTime()) {
            extendAuction();
        }
    }

    private boolean isNearEndTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(endTime.minusMinutes(extensionMinutes)) && now.isBefore(endTime);
    }

    private void extendAuction() {
        endTime = endTime.plusMinutes(extensionMinutes);
        hasExtended = true;
        System.out.println("AUCTION EXTENDED: Phiên đấu giá [" + getId() + "] được gia hạn thêm " + extensionMinutes + " phút!");
    }

    public List<AutoBid> getAutoBids() {
        return new ArrayList<>(autoBids);
    }

    public List<BidTransaction> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }
    // Viết thêm hàm này vào class Auction
    public void processPayment() {
        // 1. Kiểm tra xem phiên đấu giá đã kết thúc chưa
        if (this.getStatus() != AuctionStatus.FINISHED) {
            System.out.println("Chưa thể thanh toán! Phiên đấu giá chưa kết thúc.");
            return;
        }

        // 2. Tìm người trả giá cao nhất
        if (bidHistory.isEmpty()) {
            System.out.println("Phiên đấu giá kết thúc mà không có ai mua.");
            this.setStatus(AuctionStatus.CANCELED);
            return;
        }

        // Lấy ra giao dịch cuối cùng (giá cao nhất)
        BidTransaction winningBid = bidHistory.get(bidHistory.size() - 1);
        Bidder winner = winningBid.getBidder();
        double finalPrice = winningBid.getBidAmount();

        // 3. Thực hiện chuyển tiền
        System.out.println("\n=== BẮT ĐẦU XỬ LÝ THANH TOÁN ===");
        if (winner.deductMoney(finalPrice)) {
            // Nếu trừ tiền người mua thành công -> Chuyển tiền cho người bán
            ((Seller) this.getSeller()).receiveMoney(finalPrice);
            
            // 4. Chuyển trạng thái sang PAID (Hoàn tất)
            this.setStatus(AuctionStatus.PAID);
            System.out.println(">> Giao dịch thành công! Sản phẩm thuộc về: " + winner.getUsername());
        } else {
            System.out.println(">> Giao dịch thất bại: Người mua không đủ tiền thanh toán!");
        }
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

    public int getAuctionId() {
        return this.getId(); // Kế thừa từ lớp Entity
    }
}

