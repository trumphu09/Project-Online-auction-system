package com.auction.service;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.ItemDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIX: ItemService.searchItems() và getItemsByCategory() trước đây trả về
 * HashMap rỗng {}, khiến client nhận phản hồi không có "status" → báo lỗi.
 *
 * Giờ chúng gọi đúng DAO và trả về dữ liệu thực.
 * (ItemDAO cần có các phương thức searchItems() và getItemsByCategory() — xem bên dưới)
 */
public class ItemService {
    private static final ItemService instance = new ItemService();
    private final ItemDAO itemDAO;

    private ItemService() {
        this.itemDAO = new ItemDAO();
    }

    public static ItemService getInstance() {
        return instance;
    }

    // ==========================================
    // Lấy tất cả sản phẩm (có phân trang)
    // ==========================================
    public Map<String, Object> getAllItems(int page, int limit) {
        List<ItemDTO> items = itemDAO.getAllItems(page, limit);
        int totalItems = itemDAO.getTotalItemCount();

        Map<String, Object> response = new HashMap<>();
        response.put("items", items != null ? items : new ArrayList<>());
        response.put("totalItems", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / limit));
        response.put("currentPage", page);
        return response;
    }

    // ==========================================
    // Tìm kiếm sản phẩm theo từ khóa
    // FIX: Trước đây trả về HashMap rỗng
    // ==========================================
    public List<ItemDTO> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu không có từ khóa, trả về toàn bộ sản phẩm trang đầu
            return itemDAO.getAllItems(1, 50);
        }
        return itemDAO.searchItemsByKeyword(keyword.trim());
    }

    // ==========================================
    // Lọc sản phẩm theo danh mục
    // FIX: Trước đây trả về HashMap rỗng
    // ==========================================
    public List<ItemDTO> getItemsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return itemDAO.getItemsByCategory(category.trim().toUpperCase());
    }

    // Lấy tất cả danh mục
    public List<String> getAllCategories() {
        return itemDAO.getAllCategories();
    }

    // Lấy sản phẩm của Seller
    public List<ItemDTO> getItemsBySeller(int sellerId) {
        return itemDAO.getItemsBySellerId(sellerId);
    }

    // Lấy sản phẩm đã thắng đấu giá
    public List<ItemDTO> getWonItems(int userId) {
        return itemDAO.getWonItemsByUserId(userId);
    }

    // Thêm sản phẩm mới
    public String addNewItem(ItemDTO item) {
        if (item == null) return "Thất bại: Dữ liệu sản phẩm không hợp lệ!";
        if (item.getName() == null || item.getName().trim().isEmpty()) return "Thất bại: Tên sản phẩm không được để trống!";
        if (item.getStartingPrice() <= 0) return "Thất bại: Giá khởi điểm phải lớn hơn 0!";
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) return "Thất bại: Danh mục không được xác định!";

        boolean isSuccess = itemDAO.addItem(item);
        if (isSuccess) {
            AuctionManager.getInstance().broadcastUpdate("NEW_ITEM", item);
            return "Thành công: Đã đăng bán!";
        }
        return "Thất bại: Lỗi hệ thống.";
    }

    // Cập nhật sản phẩm
    public String updateItem(ItemDTO item) {
        if (item == null) return "Thất bại: Dữ liệu sản phẩm không hợp lệ!";

        boolean isSuccess = itemDAO.updateItem(item);
        if (isSuccess) {
            AuctionManager.getInstance().broadcastUpdate("ITEM_UPDATED", item);
            return "Thành công: Đã cập nhật sản phẩm!";
        }
        return "Thất bại: Lỗi hệ thống hoặc bạn không có quyền.";
    }

    // Xóa sản phẩm
    public String deleteItem(int itemId) {
        if (itemId <= 0) return "Thất bại: ID sản phẩm không hợp lệ.";
        boolean isSuccess = itemDAO.deleteItem(itemId);
        return isSuccess ? "Thành công: Đã xóa sản phẩm." : "Thất bại: Không tìm thấy sản phẩm.";
    }

    // Lấy chi tiết sản phẩm
    public ItemDTO getItemDetails(int itemId) {
        if (itemId <= 0) return null;
        return itemDAO.getItemById(itemId);
    }

    // Trong ItemService.java — thêm overload này
    public List<ItemDTO> getItemList(int page, int limit) {
        List<ItemDTO> items = itemDAO.getAllItems(page, limit);
        return items != null ? items : new java.util.ArrayList<>();
    }
}