package com.auction.server;


import com.auction.server.websocket.AuctionWebSocketServer;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import java.io.File;

// Import các API của mày vào đây
import com.auction.server.servlets.GetItemsAPI;
import com.auction.server.servlets.LoginAPI;
import com.auction.server.servlets.RegisterAPI;
import com.auction.server.servlets.GetItemDetailAPI;
import com.auction.server.servlets.PlaceBidAPI;

public class AuctionServer {

    private static AuctionWebSocketServer webSocketServer;
    private static Tomcat tomcat;

    public static void main(String[] args) {
        System.out.println("=== AUCTION SERVER STARTING ===");

        try {
            // -----------------------------------------------------
            // 1. KHỞI ĐỘNG HTTP SERVER (TOMCAT) Ở CỔNG 8080 CHO API
            // -----------------------------------------------------
            tomcat = new Tomcat();
            tomcat.setPort(8080);
            tomcat.getConnector(); // Kích hoạt cổng 8080

            // Tạo một ngữ cảnh (Context) ảo
            Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

            // ĐĂNG KÝ CÁC API VÀO HỆ THỐNG

            // API Đăng nhập & Đăng ký
            Tomcat.addServlet(ctx, "LoginAPI", new LoginAPI());
            ctx.addServletMappingDecoded("/api/login", "LoginAPI");
            Tomcat.addServlet(ctx, "RegisterAPI", new RegisterAPI());
            ctx.addServletMappingDecoded("/api/register", "RegisterAPI");

            // API Lấy danh sách sản phẩm <-- THÊM 2 DÒNG NÀY
            Tomcat.addServlet(ctx, "GetItemsAPI", new GetItemsAPI());
            ctx.addServletMappingDecoded("/api/items", "GetItemsAPI");

            // API Lấy chi tiết MỘT sản phẩm <-- THÊM 2 DÒNG NÀY
            // Dấu * sẽ khớp với bất kỳ ID nào phía sau
            Tomcat.addServlet(ctx, "GetItemDetailAPI", new GetItemDetailAPI());
            ctx.addServletMappingDecoded("/api/items/*", "GetItemDetailAPI");

            // API Đặt giá
            Tomcat.addServlet(ctx, "PlaceBidAPI", new PlaceBidAPI());
            ctx.addServletMappingDecoded("/api/bids", "PlaceBidAPI");

            // Mày có thể bỏ comment (xóa dấu //) mấy dòng dưới nếu đã code xong mấy API này
            // Tomcat.addServlet(ctx, "LoginAPI", new LoginAPI());
            // ctx.addServletMappingDecoded("/api/login", "LoginAPI");
            // Tomcat.addServlet(ctx, "RegisterAPI", new RegisterAPI());
            // ctx.addServletMappingDecoded("/api/register", "RegisterAPI");

            // Bật Tomcat
            tomcat.start();
            System.out.println("✓ [HTTP API] Web Server đang chạy ở cổng 8080!");

            // -----------------------------------------------------
            // 2. KHỞI ĐỘNG WEBSOCKET SERVER CHO ĐẤU GIÁ REALTIME
            // -----------------------------------------------------
            webSocketServer = new AuctionWebSocketServer();
            webSocketServer.startServer();
            System.out.println("✓ [WEBSOCKET] Đã sẵn sàng cho đấu giá Realtime!");

            // Lệnh này giúp Server giữ mạng, không bị tắt đột ngột
            tomcat.getServer().await();

        } catch (Exception e) {
            System.err.println("✗ Lỗi khởi động Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            if (webSocketServer != null) webSocketServer.stopServer();
            if (tomcat != null) tomcat.stop();
            System.out.println("Server shutdown completely.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}