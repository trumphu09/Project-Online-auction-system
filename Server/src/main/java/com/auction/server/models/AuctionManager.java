package com.auction.server.models;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {

    private static volatile AuctionManager instance;
    private final List<AuctionUpdateListener> updateListeners;

    private AuctionManager() {
        this.updateListeners = new CopyOnWriteArrayList<>();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    public void addUpdateListener(AuctionUpdateListener listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(AuctionUpdateListener listener) {
        updateListeners.remove(listener);
    }

    public void broadcastUpdate(String type, Object data) {
        AuctionUpdate update = new AuctionUpdate(type, data);
        for (AuctionUpdateListener listener : updateListeners) {
            listener.onAuctionUpdate(update);
        }
    }
}