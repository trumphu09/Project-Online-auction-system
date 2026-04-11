package com.auction.server;

import com.auction.server.websocket.AuctionWebSocketServer;
import controller.*;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import java.io.File;

// Import các API của mày vào đây

// import controller.LoginAPI;
// import controller.RegisterAPI;
// import controller.PasswordChangeAPI;

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
            Tomcat.addServlet(ctx, "GetItemsAPI", new GetItemsAPI());
            ctx.addServletMappingDecoded("/api/items", "GetItemsAPI");

            Tomcat.addServlet(ctx, "CreateItemAPI", new CreateItemAPI());
            ctx.addServletMappingDecoded("/api/items/create", "CreateItemAPI");

            Tomcat.addServlet(ctx, "CreateAuctionAPI", new GetItemsAPI());
            ctx.addServletMappingDecoded("/api/items", "GetItemsAPI");

            // Bỏ dấu // đi để nó thành thế này:
            Tomcat.addServlet(ctx, "LoginAPI", new LoginAPI().toString());
            ctx.addServletMappingDecoded("/api/login", "LoginAPI");

            Tomcat.addServlet(ctx, "RegisterAPI", new RegisterAPI().toString());
            ctx.addServletMappingDecoded("/api/register", "RegisterAPI");

            Tomcat.addServlet(ctx, "PlaceBidAPI", new PlaceBidAPI());
            ctx.addServletMappingDecoded("/api/auctions/bid", "PlaceBidAPI");

            Tomcat.addServlet(ctx, "GetAuctionDetailAPI", new GetAuctionDetailAPI());
            ctx.addServletMappingDecoded("/api/auctions/detail", "GetAuctionDetailAPI");
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