package com.auction.server.models;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {

    private static AuctionManager instance;
    private final List<AuctionUpdateListener> updateListeners;

    private AuctionManager() {
        updateListeners = new CopyOnWriteArrayList<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void addUpdateListener(AuctionUpdateListener listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(AuctionUpdateListener listener) {
        updateListeners.remove(listener);
    }

    /**
     * Phương thức công khai để các Service có thể gọi và kích hoạt một thông báo.
     * @param type Loại sự kiện (ví dụ: "NEW_BID", "AUCTION_ENDED")
     * @param data Dữ liệu liên quan đến sự kiện
     */
    public void broadcastUpdate(String type, Object data) {
        AuctionUpdate update = new AuctionUpdate(type, data);
        for (AuctionUpdateListener listener : updateListeners) {
            listener.onAuctionUpdate(update);
        }
    }
}