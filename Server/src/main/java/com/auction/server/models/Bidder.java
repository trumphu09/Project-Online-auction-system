package com.auction.server.models;

public class Bidder extends User {

    private double accountBalance;

    public Bidder(String username, String password, String email) {
        super(username, password, email, UserRole.BIDDER);
        this.accountBalance = 0.0;
    }

    public Bidder(int id, String username, String password, String email) {
        super(id, username, password, email, UserRole.BIDDER);
        this.accountBalance = 0.0;
    }

    public Bidder(int id, String username, String password, String email, double accountBalance) {
        super(id, username, password, email, UserRole.BIDDER);
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
            return true;
        }
        return false;
    }

    public boolean addMoney(double amount) {
        if (amount > 0) {
            this.accountBalance += amount;
            return true;
        }
        return false;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }
}