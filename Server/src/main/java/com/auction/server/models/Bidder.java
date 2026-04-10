package com.auction.server.models;

public class Bidder extends User {

    private double accountBalance;

    // Constructor 1: Khi TẠO TÀI KHOẢN MỚI
    public Bidder(String username, String password, String email) {
        super(username, password, email);
        this.accountBalance = 0.0;
    }

    // Constructor 2: Khi LẤY DỮ LIỆU TỪ DATABASE (CÓ ID)
    public Bidder(int id, String username, String password, String email) {
        super(id, username, password, email);
        this.accountBalance = 0.0;
    }

    // Constructor 3: CÓ CẢ account_balance
    public Bidder(int id, String username, String password, String email, double accountBalance) {
        super(id, username, password, email);
        this.accountBalance = accountBalance;
    }

    @Override
    public void showDashboard() {
        System.out.println("=== GIAO DIỆN BIDDER ===");
        System.out.println("Tên: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("Số dư hiện tại: $" + accountBalance);
    }

    public void placeBid(double amount) {
        if (amount <= accountBalance) {
            System.out.println("[✓] " + getUsername() + " vừa đặt giá: $" + amount);
        } else {
            System.out.println("[✗] Số dư không đủ để đặt mức giá này!");
        }
    }

    public boolean deductMoney(double amount) {
        if (this.accountBalance >= amount) {
            this.accountBalance -= amount;
            System.out.println("[-] Bidder [" + getUsername() + "] đã thanh toán: -" + amount + "$. Số dư: " + this.accountBalance + "$");
            return true;
        }
        return false;
    }

    public boolean addMoney(double amount) {
        if (amount > 0) {
            this.accountBalance += amount;
            System.out.println("[+] Bidder [" + getUsername() + "] nạp tiền: +" + amount + "$. Số dư: " + this.accountBalance + "$");
            return true;
        }
        return false;
    }

    // Getters
    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }
}