package com.auction.client.service;

public interface BidUpdateListener {

  void onNewBid(int auctionId, double newPrice, String bidderUsername);

  default void onNewBid(int auctionId, double newPrice, String bidderUsername, String newEndTime) {
    onNewBid(auctionId, newPrice, bidderUsername);
  }

  void onAuctionEnded(int auctionId, String status);

  void onConnected();

  void onDisconnected(String reason);

  default void onAutoBidNotice(int auctionId, int userId, String message) {}
}