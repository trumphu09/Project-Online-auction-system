package com.auction.service;
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.ItemDTO;
public class ItemService {
    private static final ItemService instance=new ItemService();

    private final ItemDAO itemDAO;

    private ItemService(){
        this.itemDAO=new ItemDAO();
    }

    public static ItemService getInstance() {
        return instance;
    }

    public String addNewItem(ItemDTO item){
        // Validation 1: Kiểm tra chống rỗng (Null Pointer)
        if (item == null) {
            return "Thất bại: Dữ liệu sản phẩm không hợp lệ (Null)!";
        }
        
        // Validation 2: Kiểm tra các trường cơ bản bắt buộc phải có
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

        // Trả kết quả chữ về cho Controller
        if (isSuccess) {
            return "Thành công: Đã đăng bán [" + item.getName() + "] lên sàn!";
        } else {
            return "Thất bại: Lỗi hệ thống khi lưu vào cơ sở dữ liệu. Vui lòng thử lại sau!";
        }
    }

    public ItemDTO getItemDetails(int itemId) {
        // Validation cơ bản trước khi gọi CSDL
        if (itemId <= 0) {
            System.err.println("Lỗi nghiệp vụ: ID sản phẩm không hợp lệ (" + itemId + ")");
            return null; // Hoặc bạn có thể ném ra Exception tùy thiết kế nhóm
        }
        
        // Giao việc cho DAO (DAO sẽ dùng LEFT JOIN để lấy dữ liệu đa hình)
        return itemDAO.getItemById(itemId);
    }
}
