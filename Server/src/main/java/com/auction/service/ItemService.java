package com.auction.service;
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.ItemDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemService {
    private static final ItemService instance = new ItemService();
    private final ItemDAO itemDAO;

    private ItemService() {
        this.itemDAO = new ItemDAO();
    }

    public static ItemService getInstance() {
        return instance;
    }

    public Map<String, Object> getAllItems(int page, int limit) {
        List<ItemDTO> items = itemDAO.getAllItems(page, limit);
        int totalItems = itemDAO.getTotalItemCount();

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("totalItems", totalItems);
        response.put("totalPages", (int) Math.ceil((double) totalItems / limit));
        response.put("currentPage", page);
        return response;
    }

    public Map<String, Object> searchItems(String keyword, int page, int limit) {
        // Tương tự getAllItems, cần các phương thức tương ứng trong ItemDAO
        return new HashMap<>();
    }

    public List<String> getAllCategories() {
        return itemDAO.getAllCategories();
    }

    public Map<String, Object> getItemsByCategory(String category, int page, int limit) {
        // Tương tự getAllItems, cần các phương thức tương ứng trong ItemDAO
        return new HashMap<>();
    }

    public List<ItemDTO> getItemsBySeller(int sellerId) {
        return itemDAO.getItemsBySellerId(sellerId);
    }

    public List<ItemDTO> getWonItems(int userId) {
        return itemDAO.getWonItemsByUserId(userId);
    }

    public String addNewItem(ItemDTO item) {
        if (item == null) return "Thất bại: Dữ liệu sản phẩm không hợp lệ!";
        if (item.getName() == null || item.getName().trim().isEmpty()) return "Thất bại: Tên sản phẩm không được để trống!";
        if (item.getStartingPrice() <= 0) return "Thất bại: Giá khởi điểm phải lớn hơn 0!";
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) return "Thất bại: Danh mục không được xác định!";

        boolean isSuccess = itemDAO.addItem(item);
        return isSuccess ? "Thành công: Đã đăng bán!" : "Thất bại: Lỗi hệ thống.";
    }

    public String updateItem(ItemDTO item) {
        if (item == null) return "Thất bại: Dữ liệu sản phẩm không hợp lệ!";
        // Thêm các validation khác nếu cần

        boolean isSuccess = itemDAO.updateItem(item);
        return isSuccess ? "Thành công: Đã cập nhật sản phẩm!" : "Thất bại: Lỗi hệ thống hoặc bạn không có quyền.";
    }

    public String deleteItem(int itemId) {
        if (itemId <= 0) return "Thất bại: ID sản phẩm không hợp lệ.";
        boolean isSuccess = itemDAO.deleteItem(itemId);
        return isSuccess ? "Thành công: Đã xóa sản phẩm." : "Thất bại: Không tìm thấy sản phẩm.";
    }

    public ItemDTO getItemDetails(int itemId) {
        if (itemId <= 0) return null;
        return itemDAO.getItemById(itemId);
    }
}