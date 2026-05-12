package com.auction.client.service;

/**
 * Interface lắng nghe các sự kiện đấu giá realtime từ WebSocket Server.
 *
 * Implement interface này trong BiddingRoomController để nhận cập nhật trực tiếp
 * khi có người đặt giá hoặc phiên kết thúc mà không cần reload trang.
 */
public interface BidUpdateListener {

  /**
   * Gọi khi server broadcast giá mới (type: "NEW_BID" hoặc "PRICE_UPDATE").
   *
   * @param auctionId       ID phiên đấu giá bị ảnh hưởng
   * @param newPrice        Giá mới vừa được chốt
   * @param bidderUsername  Tên người vừa đặt giá (có thể null với PRICE_UPDATE)
   */
  void onNewBid(int auctionId, double newPrice, String bidderUsername);

  /**
   * Gọi khi phiên đấu giá kết thúc (type: "AUCTION_ENDED").
   *
   * @param auctionId  ID phiên đấu giá vừa đóng
   * @param status     Trạng thái cuối: "FINISHED" hoặc "CANCELED"
   */
  void onAuctionEnded(int auctionId, String status);

  /**
   * Gọi khi kết nối WebSocket được thiết lập thành công.
   */
  void onConnected();

  /**
   * Gọi khi mất kết nối WebSocket.
   *
   * @param reason  Lý do mất kết nối
   */
  void onDisconnected(String reason);
}