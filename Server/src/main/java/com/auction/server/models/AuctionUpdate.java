package com.auction.server.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * General-purpose update object used for broadcasting auction events.
 * It contains a "type" (e.g. "NEW_BID", "AUCTION_ENDED") and a payload in "data".
 */
public class AuctionUpdate {
    private final String type;
    private final Object data;
    private final LocalDateTime timestamp;

    public AuctionUpdate(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Backwards-compatible convenience constructor that maps the old 4-field
    // snapshot into a generic payload map.
    public AuctionUpdate(int auctionId, double currentHighestBid, int totalBids, LocalDateTime endTime) {
        this.type = "SNAPSHOT";
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", auctionId);
        payload.put("currentHighestBid", currentHighestBid);
        payload.put("totalBids", totalBids);
        payload.put("endTime", endTime);
        this.data = payload;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() { return type; }
    public Object getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}