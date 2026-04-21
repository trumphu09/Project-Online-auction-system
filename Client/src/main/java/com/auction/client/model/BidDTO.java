package com.auction.client.model;

<<<<<<< HEAD
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BidDTO implements Serializable {

    // Các trường dữ liệu phải khớp chính xác với tên biến trong BidTransactionDTO của Server
    @Expose 
    @SerializedName("id")
    private int id;

    @Expose 
    @SerializedName("bidderId")
    private int bidderId;

    @Expose 
    @SerializedName("bidderUsername")
    private String bidderUsername;

    @Expose 
    @SerializedName("bidAmount")
    private double bidAmount;

    // Hứng dữ liệu thời gian dưới dạng chuỗi (String) để tránh lỗi parse JSON
    @Expose 
    @SerializedName("timestamp")
    private String timestamp;

    // ==========================================
    // Constructor rỗng (Bắt buộc phải có để Gson tự động tạo Object)
    // ==========================================
    public BidDTO() {}

    public BidDTO(int id, int bidderId, String bidderUsername, double bidAmount, String timestamp) {
=======
import java.time.LocalDateTime;

public class BidDTO {
    private int id;
    private int bidderId;
    private String bidderUsername;  // Chỉ username, không phải object
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidDTO(int id, int bidderId, String bidderUsername, 
                            double bidAmount, LocalDateTime timestamp) {
>>>>>>> 71e6ab4a2b9c335e64205860a7a9ead1080a473c
        this.id = id;
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

<<<<<<< HEAD
    // ==========================================
    // Getters & Setters
    // ==========================================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBidderId() { return bidderId; }
    public void setBidderId(int bidderId) { this.bidderId = bidderId; }

    public String getBidderUsername() { return bidderUsername; }
    public void setBidderUsername(String bidderUsername) { this.bidderUsername = bidderUsername; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
=======
    // Getters only
    public int getId() { return id; }
    public int getBidderId() { return bidderId; }
    public String getBidderUsername() { return bidderUsername; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
>>>>>>> 71e6ab4a2b9c335e64205860a7a9ead1080a473c
