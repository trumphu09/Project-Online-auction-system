package com.auction.server.models;
import java.util.ArrayList;
import java.util.List;

public class Seller extends com.auction.server.models.User {

    // Thuộc tính riêng: Điểm uy tín và Danh sách hàng hóa của người này
    private double sellerRating;
    private final List<Item> myProducts;

    public Seller(String id, String username, String password, String email) {
        super(id, username, password, email); // Kéo dữ liệu từ lớp User xuống
        this.sellerRating = 5.0; // Mặc định tài khoản mới được 5 sao
        this.myProducts = new ArrayList<>(); // Khởi tạo kho hàng rỗng
    }

    // Hành động riêng (yêu cầu 3.1.2): Thêm sản phẩm vào hệ thống
    public void addProduct(Item item) {
        myProducts.add(item);
        System.out.println("Seller [" + getUsername() + "] vừa thêm sản phẩm: " + item.getName());
    }

    // Ghi đè phương thức rỗng của lớp cha
    @Override
    public void showDashboard() {
        System.out.println("--- Kênh Người Bán: " + getUsername() + " ---");
        System.out.println("Điểm uy tín: " + sellerRating + " sao");
        System.out.println("Số lượng sản phẩm đang quản lý: " + myProducts.size());
    }
}