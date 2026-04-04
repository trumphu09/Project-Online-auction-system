package com.auction.server.models;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {

    // Thuộc tính riêng: Điểm uy tín và Danh sách hàng hóa của người này
    private double total_Rating;
    private final List<Item> myProducts;
    private double accountBalance;
    private int sale_count;

    public Seller(int id, String username, String password, String email) {
        super(id, username, password, email); // Kéo dữ liệu từ lớp User xuống
        this.total_Rating = 5.0; // Mặc định tài khoản mới được 5 sao
        this.myProducts = new ArrayList<>(); // Khởi tạo kho hàng rỗng
        this.accountBalance = 0.0; // Mặc định số dư tài khoản là 0
        this.sale_count = 0;
    }

    public Seller(int id, String username, String password, String email, double total_rating, double account_balance, int sale_count) {
        super(id, username, password, email); 
        this.total_Rating = total_rating; 
        this.myProducts = new ArrayList<>(); 
        this.accountBalance = account_balance; 
        this.sale_count = sale_count;
    }

    // seller co ca total_rating
    public Seller(int id, String username, String password, String email, Double total_rating) {
        super(id, username, password, email); // Kéo dữ liệu từ lớp User xuống
        this.total_Rating = 5.0; // Mặc định tài khoản mới được 5 sao
        this.myProducts = new ArrayList<>(); // Khởi tạo kho hàng rỗng
        this.accountBalance = 0.0; // Mặc định số dư tài khoản là 0
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
        System.out.println("Điểm uy tín: " + total_Rating + " sao");
        System.out.println("Số lượng sản phẩm đang quản lý: " + myProducts.size());
    }
    public void receiveMoney(double amount) {
        this.accountBalance += amount;
        System.out.println("[$] Seller [" + getUsername() + "] vừa nhận được thanh toán: +" + amount + "$. Số dư: " + this.accountBalance + "$");
    }
    
    // getters 
    public double getTotal_Rating() {
        return total_Rating;
    }

    public List<Item> getMyProducts() {
        return myProducts;
    }
    public double getAccountBalance() {
        return accountBalance;
    }

    public int getSale_count() {
        return sale_count;
    }
    
}