package com.auction.client;

import java.util.TimeZone;

public class Main {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        System.out.println(java.time.ZoneId.systemDefault());
        System.out.println(java.util.TimeZone.getDefault());
        System.out.println(java.time.LocalDateTime.now());
        // Gọi hàm main của MainApp để kích hoạt giao diện JavaFX
        MainApp.main(args);
    }
}