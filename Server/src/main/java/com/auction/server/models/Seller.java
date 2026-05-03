package com.auction.server.models;
import java.util.ArrayList;
import java.util.List;

public class Seller extends User {

    private double total_Rating;
    private final List<Item> myProducts;
    private double accountBalance;
    private int sale_count;

    public Seller(String username, String password, String email) {
        super(username, password, email, UserRole.SELLER);
        this.total_Rating = 5.0;
        this.myProducts = new ArrayList<>();
        this.accountBalance = 0.0;
        this.sale_count = 0;
    }

    public Seller(int id, String username, String password, String email) {
        super(id, username, password, email, UserRole.SELLER);
        this.total_Rating = 5.0;
        this.myProducts = new ArrayList<>();
        this.accountBalance = 0.0;
        this.sale_count = 0;
    }

    public Seller(int id, String username, String password, String email, double total_rating, double account_balance, int sale_count) {
        super(id, username, password, email, UserRole.SELLER); 
        this.total_Rating = total_rating; 
        this.myProducts = new ArrayList<>(); 
        this.accountBalance = account_balance; 
        this.sale_count = sale_count;
    }

    public void addProduct(Item item) {
        myProducts.add(item);
        System.out.println("Seller [" + getUsername() + "] vừa thêm sản phẩm: " + item.getName());
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Kênh Người Bán: " + getUsername() + " ---");
        System.out.println("Điểm uy tín: " + total_Rating + " sao");
        System.out.println("Số lượng sản phẩm đang quản lý: " + myProducts.size());
    }
    
    public void receiveMoney(double amount) {
        this.accountBalance += amount;
    }
    
    // getters 
    public double getTotal_Rating() { return total_Rating; }
    public List<Item> getMyProducts() { return myProducts; }
    public double getAccountBalance() { return accountBalance; }
    public int getSale_count() { return sale_count; }
}