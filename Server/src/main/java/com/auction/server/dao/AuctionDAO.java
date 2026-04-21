package com.auction.server.dao;
import com.auction.server.models.Auction;
import com.auction.server.models.AuctionDataDTO;
import com.auction.server.models.AuctionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AuctionDAO {
    public int createAuction(int itemId,int sellerId,LocalDateTime endtime){
        String sql = "INSERT INTO auctions (item_id, seller_id, end_time,status,has_extended,created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try(Connection conn=DatabaseConnection.getInstance().getConnection()){
            PreparedStatement pst=conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setInt(1, itemId);
            pst.setInt(2, sellerId);
            pst.setTimestamp(3,Timestamp.valueOf(endtime));
            pst.setString(4, AuctionStatus.OPEN.name());
            pst.setBoolean(5, false);

            int affectedRows=pst.executeUpdate();
            if(affectedRows>0){
                try(ResultSet generatedKeys=pst.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo phiên đấu giá :"+e.getMessage());
        }
        return -1;
    }

    public boolean updateAuctionStatus(int auctionId,AuctionStatus newStatus){
        String sql="UPDATE auctions SET status=? WHERE id=?";
        try(Connection conn=DatabaseConnection.getInstance().getConnection()){
            PreparedStatement pst=conn.prepareStatement(sql);
            pst.setString(1, newStatus.name());
            pst.setInt(2, auctionId);
            return pst.executeUpdate()>0;
        }catch(SQLException e){
            System.err.println("Lỗi cập nhật trạng thái đấu giá :"+e.getMessage());
        }
        return false;
    }

    public boolean updateEndTimeAndExtension(int auctionId, LocalDateTime newEndTime, boolean hasExtended) {
        String sql = "UPDATE auctions SET end_time = ?, has_extended = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(newEndTime));
            pst.setBoolean(2, hasExtended);
            pst.setInt(3, auctionId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật gia hạn đấu giá: " + e.getMessage());
            return false;
        }
    }

    public AuctionDataDTO getAuctionDataById(int auctionId) {
        String sql = "SELECT item_id, seller_id, end_time, status, has_extended FROM auctions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, auctionId);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Đọc dữ liệu từ DB
                    int itemId = rs.getInt("item_id");
                    int sellerId = rs.getInt("seller_id");
                    // Chuyển ngược từ SQL Timestamp về Java LocalDateTime
                    LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime(); 
                    AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));
                    boolean hasExtended = rs.getBoolean("has_extended");
                    
                    // Giả sử bạn có một class AuctionDataDTO để truyền dữ liệu cho nhanh
                    return new AuctionDataDTO(sellerId, sellerId, sellerId, endTime, status, hasExtended);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm phiên đấu giá: " + e.getMessage());
        }
        return null; // Không tìm thấy
    }

}
