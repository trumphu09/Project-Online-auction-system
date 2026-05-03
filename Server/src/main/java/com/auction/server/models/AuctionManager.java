package com.auction.server.models;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central manager for broadcasting auction updates and (optionally)
 * keeping a simple in-memory registry of Auction objects.
 */
public class AuctionManager {

    private static volatile AuctionManager instance;
    private final List<AuctionUpdateListener> updateListeners;
    // Optional in-memory auction registry to support AuctionTimer convenience calls
    private final List<Auction> auctions;

    private AuctionManager() {
        this.updateListeners = new CopyOnWriteArrayList<>();
        this.auctions = new CopyOnWriteArrayList<>();
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

    public void broadcastUpdate(String type, Object data) {
        AuctionUpdate update = new AuctionUpdate(type, data);
        for (AuctionUpdateListener listener : updateListeners) {
            try {
                listener.onAuctionUpdate(update);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    // In-memory auction registry helpers
    public List<Auction> getAllAuctions() {
        return auctions;
    }

    public void addAuction(Auction a) { auctions.add(a); }
    public void removeAuction(Auction a) { auctions.remove(a); }

    /**
     * Convenience method: notify listeners that a specific auction changed.
     */
    public void notifyAuctionUpdate(Auction auction) {
        broadcastUpdate("AUCTION_UPDATED", auction);
    }
}