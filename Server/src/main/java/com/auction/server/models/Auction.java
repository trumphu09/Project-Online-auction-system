package com.auction.server.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Auction extends Entity {

    // 1. CÁC BIẾN TRẠNG THÁI (Đã thêm VOLATILE để đảm bảo tính hiển thị giữa các luồng)
    private final Item item;
    private final User seller;
    private volatile LocalDateTime endTime; 
    private volatile AuctionStatus status;
    private volatile boolean hasExtended = false;
    private final int extensionMinutes = 5;

    // 2. DANH SÁCH AN TOÀN LUỒNG (Sử dụng CopyOnWriteArrayList cho cả hai)
    private final List<BidTransaction> bidHistory;
    private final List<AutoBid> autoBids;

    public Auction(int id, Item item, User seller, LocalDateTime endTime) {
        super(id);
        this.item = item;
        this.seller = seller;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
        
        // Đảm bảo không bao giờ bị lỗi ConcurrentModificationException khi đọc dữ liệu
        this.bidHistory = new CopyOnWriteArrayList<>();
        this.autoBids = new CopyOnWriteArrayList<>(); 
    }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus newStatus) {
        this.status = newStatus;
        System.out.println(">> CẬP NHẬT: Phiên đấu giá [" + this.getId() + "] -> " + this.status);
    }

    // --- LOGIC ĐẶT GIÁ (Người dùng đặt thủ công) ---

    public synchronized boolean placeBid(Bidder bidder, double amount) {
        if (this.status != AuctionStatus.OPEN && this.status != AuctionStatus.RUNNING) {
            System.out.println("Từ chối: Phiên đấu giá không mở.");
            return false;
        }

        double highestBid = getHighestBid();

        if (amount > highestBid && amount >= item.getStartingPrice()) {
            // Ghi nhận giá thủ công
            addBidTransaction(bidder, amount, false);
            
            checkAndExtendAuction();
            
            // Kích hoạt toán tử Auto-bid ngay sau khi có người đặt thủ công
            processAutoBids();
            return true;
        } else {
            System.out.println("Lỗi: Giá đặt ($" + amount + ") phải cao hơn $" + highestBid);
            return false;
        }
    }

    // Hàm Helper dùng chung để ghi nhận lịch sử giá (cho cả thủ công và auto)
    private void addBidTransaction(Bidder bidder, double amount, boolean isAuto) {
        int bidId = bidHistory.size() + 1;
        BidTransaction newBid = new BidTransaction(bidId, bidder, amount);
        bidHistory.add(newBid);
        
        if (isAuto) {
            System.out.println("⚡ AUTO-BID WINNER: [" + bidder.getUsername() + "] chốt giá $" + amount);
        } else {
            System.out.println("\uD83D\uDC64 MANUAL BID: [" + bidder.getUsername() + "] chốt giá $" + amount);
        }
    }

    public double getHighestBid() {
        if (bidHistory.isEmpty()) return 0;
        return bidHistory.get(bidHistory.size() - 1).getBidAmount();
    }

    // --- LOGIC AUTO-BID THÔNG MINH (TÍNH TOÁN TỨC THÌ) ---

    public void addAutoBid(AutoBid autoBid) {
        autoBids.add(autoBid);
        System.out.println("Auto-bid setup: [" + autoBid.getBidder().getUsername() + "] - Max: $" + autoBid.getMaxBidAmount());
    }

    public void removeAutoBid(Bidder bidder) {
        autoBids.removeIf(autoBid -> autoBid.getBidder().equals(bidder));
    }

    private void processAutoBids() {
        if (autoBids.isEmpty()) return;

        double currentHighest = getHighestBid();
        Bidder currentLeader = bidHistory.isEmpty() ? null : bidHistory.get(bidHistory.size() - 1).getBidder();

        // 1. Lọc ra những người cài Auto-bid (có thể đấu giá và không phải người đang dẫn đầu)
        List<AutoBid> activeBidders = new ArrayList<>();
        for (AutoBid ab : autoBids) {
            if (ab.canAutoBid(currentHighest) && !ab.getBidder().equals(currentLeader)) {
                activeBidders.add(ab);
            }
        }

        if (activeBidders.isEmpty()) return;

        // 2. Kịch bản 1: Chỉ có 1 người cài Auto-bid (Họ chỉ cần vượt qua người đặt thủ công 1 bước giá)
        if (activeBidders.size() == 1) {
            AutoBid loneBidder = activeBidders.get(0);
            double nextBid = loneBidder.getNextAutoBidAmount(currentHighest);
            if (nextBid <= loneBidder.getMaxBidAmount()) {
                addBidTransaction(loneBidder.getBidder(), nextBid, true);
            }
            return;
        }

        // 3. Kịch bản 2: Bidding War (Nhiều người cài Auto-bid chọi nhau)
        // Sắp xếp danh sách giảm dần theo giới hạn Max Bid
        activeBidders.sort((a, b) -> Double.compare(b.getMaxBidAmount(), a.getMaxBidAmount()));

        AutoBid winner = activeBidders.get(0); // Người có tiền to nhất
        AutoBid runnerUp = activeBidders.get(1); // Người có tiền to thứ nhì

        // GIẢI QUYẾT TỨC THÌ: Giá thắng = Max của người thua + 1 bước giá của người thắng
        double finalCalculatedPrice = winner.getNextAutoBidAmount(runnerUp.getMaxBidAmount());

        // Đảm bảo không vượt quá Max của chính người thắng
        if (finalCalculatedPrice > winner.getMaxBidAmount()) {
            finalCalculatedPrice = winner.getMaxBidAmount();
        }

        // Vô hiệu hóa người thua và tất cả những người đứng sau
        for (int i = 1; i < activeBidders.size(); i++) {
            activeBidders.get(i).deactivate();
            System.out.println("❌ AUTO-BID: [" + activeBidders.get(i).getBidder().getUsername() + "] đã bị loại vì đạt giới hạn.");
        }

        // Chỉ tạo ĐÚNG 1 transaction cho người chiến thắng cuối cùng
        addBidTransaction(winner.getBidder(), finalCalculatedPrice, true);
    }

    // --- CÁC HÀM TIỆN ÍCH KHÁC ---

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
        System.out.println("⏰ EXTENDED: Phiên [" + getId() + "] gia hạn thêm " + extensionMinutes + " phút!");
    }

    public void processPayment() {
        if (this.getStatus() != AuctionStatus.FINISHED) {
            System.out.println("Chưa thể thanh toán! Phiên đấu giá chưa kết thúc.");
            return;
        }
        if (bidHistory.isEmpty()) {
            System.out.println("Phiên kết thúc không có người mua.");
            this.setStatus(AuctionStatus.CANCELED);
            return;
        }

        BidTransaction winningBid = bidHistory.get(bidHistory.size() - 1);
        Bidder winner = winningBid.getBidder();
        double finalPrice = winningBid.getBidAmount();

        System.out.println("\n=== BẮT ĐẦU THANH TOÁN ===");
        if (winner.deductMoney(finalPrice)) {
            // Ép kiểu sang class Seller của bạn
            // ((Seller) this.getSeller()).receiveMoney(finalPrice);
            this.setStatus(AuctionStatus.PAID);
            System.out.println(">> Giao dịch thành công! Thuộc về: " + winner.getUsername());
        } else {
            System.out.println(">> Lỗi: Người mua không đủ tiền!");
        }
    }

    public List<AutoBid> getAutoBids() { return autoBids; }
    // Giờ đây getBidHistory trả về luôn mảng an toàn, không cần clone nữa
    public List<BidTransaction> getBidHistory() { return bidHistory; }
    
    public Item getItem() { return item; }
    public User getSeller() { return seller; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getAuctionId() { return this.getId(); }
}