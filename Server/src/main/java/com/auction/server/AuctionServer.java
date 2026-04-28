package com.auction.server;

import com.auction.server.filters.AuthFilter;
import com.auction.server.scheduler.AuctionStatusScheduler;
import com.auction.server.servlets.*;
import com.auction.server.websocket.AuctionWebSocketServer;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.File;

public class AuctionServer {

    private static AuctionWebSocketServer webSocketServer;
    private static AuctionStatusScheduler scheduler;
    private static Tomcat tomcat;

    public static AuctionWebSocketServer getWebSocketServer() {
        return webSocketServer;
    }

    public static void main(String[] args) {
        System.out.println("=== AUCTION SERVER STARTING ===");

        try {
            tomcat = new Tomcat();
            tomcat.setPort(8080);
            tomcat.getConnector();

            Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

            FilterDef authFilterDef = new FilterDef();
            authFilterDef.setFilterName("AuthFilter");
            authFilterDef.setFilterClass(AuthFilter.class.getName());
            ctx.addFilterDef(authFilterDef);

            FilterMap authFilterMap = new FilterMap();
            authFilterMap.setFilterName("AuthFilter");
            authFilterMap.addURLPattern("/api/*");
            ctx.addFilterMap(authFilterMap);

            // Register Servlets
            Tomcat.addServlet(ctx, "LoginAPI", new LoginAPI());
            ctx.addServletMappingDecoded("/api/login", "LoginAPI");
            Tomcat.addServlet(ctx, "LogoutAPI", new LogoutAPI());
            ctx.addServletMappingDecoded("/api/logout", "LogoutAPI");
            Tomcat.addServlet(ctx, "RegisterAPI", new RegisterAPI());
            ctx.addServletMappingDecoded("/api/register", "RegisterAPI");
            Tomcat.addServlet(ctx, "ItemsAPI", new ItemsAPI());
            ctx.addServletMappingDecoded("/api/items", "ItemsAPI");
            Tomcat.addServlet(ctx, "GetItemDetailAPI", new GetItemDetailAPI());
            ctx.addServletMappingDecoded("/api/items/*", "GetItemDetailAPI");
            Tomcat.addServlet(ctx, "SearchItemsAPI", new SearchItemsAPI());
            ctx.addServletMappingDecoded("/api/search/items", "SearchItemsAPI");
            Tomcat.addServlet(ctx, "CategoryAPI", new CategoryAPI());
            ctx.addServletMappingDecoded("/api/categories/*", "CategoryAPI");
            Tomcat.addServlet(ctx, "PlaceBidAPI", new PlaceBidAPI());
            ctx.addServletMappingDecoded("/api/bids", "PlaceBidAPI");
            Tomcat.addServlet(ctx, "GetBidHistoryAPI", new GetBidHistoryAPI());
            ctx.addServletMappingDecoded("/api/items/*/bids", "GetBidHistoryAPI");
            Tomcat.addServlet(ctx, "UserProfileAPI", new UserProfileAPI());
            ctx.addServletMappingDecoded("/api/my/profile/*", "UserProfileAPI");
            Tomcat.addServlet(ctx, "GetMyWonItemsAPI", new GetMyWonItemsAPI());
            ctx.addServletMappingDecoded("/api/my/won-items", "GetMyWonItemsAPI");
            Tomcat.addServlet(ctx, "GetMyItemsForSaleAPI", new GetMyItemsForSaleAPI());
            ctx.addServletMappingDecoded("/api/my/items", "GetMyItemsForSaleAPI");
            Tomcat.addServlet(ctx, "GetMyActiveBidsAPI", new GetMyActiveBidsAPI());
            ctx.addServletMappingDecoded("/api/my/active-bids", "GetMyActiveBidsAPI");
            Tomcat.addServlet(ctx, "AdminUserAPI", new AdminUserAPI());
            ctx.addServletMappingDecoded("/api/admin/users/*", "AdminUserAPI");
            Tomcat.addServlet(ctx, "AdminItemAPI", new AdminItemAPI());
            ctx.addServletMappingDecoded("/api/admin/items/*", "AdminItemAPI");

            tomcat.start();
            System.out.println("✓ [HTTP API] Web Server đang chạy ở cổng 8080!");

            webSocketServer = new AuctionWebSocketServer();
            webSocketServer.startServer();
            
            // Khởi động Scheduler sau khi các server đã chạy
            scheduler = new AuctionStatusScheduler();
            scheduler.start();

            tomcat.getServer().await();

        } catch (Exception e) {
            System.err.println("✗ Lỗi khởi động Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            if (scheduler != null) scheduler.stop();
            if (webSocketServer != null) webSocketServer.stopServer();
            if (tomcat != null) tomcat.stop();
            System.out.println("Server shutdown completely.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}