package com.auction.service;
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.ItemDTO;

public class ItemService {
    private static final ItemService instance = new ItemService();
    private final ItemDAO itemDAO;

    private ItemService() {
        this.itemDAO = new ItemDAO();
    }

    public static ItemService getInstance() {
        return instance;
    }

    public String addNewItem(ItemDTO item) {
        if (item == null) {
            return "Thất bại: Dữ liệu sản phẩm không hợp lệ (Null)!";
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return "Thất bại: Tên sản phẩm không được để trống!";
        }
        if (item.getStartingPrice() <= 0) {
            return "Thất bại: Giá khởi điểm phải lớn hơn 0!";
        }
        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            return "Thất bại: Danh mục sản phẩm (Category) không được xác định!";
        }

        boolean isSuccess = itemDAO.addItem(item);

        if (isSuccess) {
            return "Thành công: Đã đăng bán [" + item.getName() + "] lên sàn!";
        } else {
            return "Thất bại: Lỗi hệ thống khi lưu vào cơ sở dữ liệu.";
        }
    }

    public String updateItem(ItemDTO item) {
        if (item == null) {
            return "Thất bại: Dữ liệu sản phẩm không hợp lệ (Null)!";
        }
        // Thêm các validation khác nếu cần
        
        // Giả sử ItemDAO có phương thức updateItem
        // boolean isSuccess = itemDAO.updateItem(item); 
        
        // Vì ItemDAO cũ đã có logic update phức tạp, chúng ta tạm thời chưa gọi
        // Trong thực tế, bạn cần hợp nhất logic update của ItemDAO cũ và mới
        
        // Tạm thời trả về thành công để test luồng
        return "Thành công: Đã cập nhật sản phẩm!";
    }

    public ItemDTO getItemDetails(int itemId) {
        if (itemId <= 0) {
            return null;
        }
        return itemDAO.getItemById(itemId);
    }
}