package com.auction.server.scheduler;

import com.auction.server.AuctionServer;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.Item;
import com.auction.server.models.UserDTO;
import com.auction.server.websocket.AuctionWebSocketServer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionStatusScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();

    public void start() {
        // Chạy công việc sau 0 giây ban đầu, và lặp lại sau mỗi 10 giây
        scheduler.scheduleAtFixedRate(this::checkAuctionStatuses, 0, 10, TimeUnit.SECONDS);
        System.out.println("✓ [SCHEDULER] Bộ đếm thời gian kiểm tra trạng thái đấu giá đã bắt đầu.");
    }

    private void checkAuctionStatuses() {
        try {
            AuctionWebSocketServer webSocketServer = AuctionServer.getWebSocketServer();
            if (webSocketServer == null) {
                return; // Chưa sẵn sàng
            }

            // 1. Cập nhật PENDING -> RUNNING
            List<Item> startedItems = itemDAO.updatePendingItemsToRunning();
            for (Item item : startedItems) {
                System.out.println("Scheduler: Item " + item.getId() + " has started.");
                webSocketServer.broadcastAuctionStarted(item);
            }

            // 2. Cập nhật RUNNING -> ENDED
            List<Item> endedItems = itemDAO.updateRunningItemsToEnded();
            for (Item item : endedItems) {
                System.out.println("Scheduler: Item " + item.getId() + " has ended.");
                UserDTO winner = (item.getHighestBidderId() > 0) ? userDAO.getUserById(item.getHighestBidderId()) : null;
                webSocketServer.broadcastAuctionEnded(item, winner);
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình chạy của Scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        scheduler.shutdown();
        System.out.println("[SCHEDULER] Đã dừng bộ đếm thời gian.");
    }
}