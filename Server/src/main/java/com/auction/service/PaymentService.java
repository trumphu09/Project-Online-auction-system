package com.auction.service;

import com.auction.server.dao.PaymentDAO;
import com.auction.server.models.Payment;
import java.util.List;

public class PaymentService {

    // Áp dụng Singleton Pattern (Chỉ có 1 Kế toán trưởng trong toàn hệ thống)
    private static final PaymentService instance = new PaymentService();
    
    private final PaymentDAO paymentDAO;

    // Private Constructor
    private PaymentService() {
        this.paymentDAO = new PaymentDAO();
    }

    public static PaymentService getInstance() {
        return instance;
    }

    // ==========================================
    // 1. NGHIỆP VỤ: TẠO HÓA ĐƠN MỚI (Khi phiên đấu giá vừa kết thúc)
    // ==========================================
    public String createInvoice(int auctionId, int bidderId, int sellerId, double amount) {
        // Validation: Kiểm tra đầu vào cực kỳ nghiêm ngặt liên quan đến tiền bạc
        if (auctionId <= 0 || bidderId <= 0 || sellerId <= 0) {
            return "Thất bại: Thông tin ID người dùng hoặc ID phiên đấu giá không hợp lệ!";
        }
        
        if (bidderId == sellerId) {
            return "Thất bại: Người bán và người mua không thể là cùng một người!";
        }

        if (amount <= 0) {
            return "Thất bại: Số tiền thanh toán phải lớn hơn 0!";
        }

        // Gọi DAO để tạo hóa đơn
        boolean isSuccess = paymentDAO.createPaymentInvoice(auctionId, bidderId, sellerId, amount);

        if (isSuccess) {
            return "Thành công: Đã tạo hóa đơn chờ thanh toán cho phiên đấu giá #" + auctionId;
        } else {
            return "Thất bại: Lỗi hệ thống khi tạo hóa đơn.";
        }
    }

    // ==========================================
    // 2. NGHIỆP VỤ: XỬ LÝ THANH TOÁN (Chuyển tiền & Chốt đơn)
    // ==========================================
    public String executePayment(int paymentId) {
        if (paymentId <= 0) {
            return "Thất bại: Mã hóa đơn không hợp lệ!";
        }

        // Gọi DAO thực hiện Transaction (Trừ tiền, cộng tiền, đổi trạng thái...)
        boolean isSuccess = paymentDAO.processPayment(paymentId);

        if (isSuccess) {
            return "Thành công: Giao dịch hoàn tất! Tiền đã được chuyển vào ví người bán.";
        } else {
            return "Thất bại: Giao dịch không thành công. Hóa đơn có thể đã được thanh toán hoặc không tồn tại.";
        }
    }

    // ==========================================
    // 3. NGHIỆP VỤ: XEM LỊCH SỬ GIAO DỊCH CỦA MỘT NGƯỜI
    // ==========================================
    public List<Payment> getUserPaymentHistory(int userId) {
        if (userId <= 0) {
            System.err.println("Lỗi nghiệp vụ: ID người dùng không hợp lệ.");
            return null;
        }
        
        return paymentDAO.getPaymentHistoryByUser(userId);
    }
}