package com.auction.server.dao;
import com.auction.server.models.AuctionStatus;
import com.auction.server.models.Electronics;
import java.sql.*;
import java.time.LocalDateTime;
public class ElectronicsDAO {
    private ItemDAO itemDAO;
    // them mot san pham moi vao itemsdatabase va them vao bang electronics
    public boolean addElectronicsItem(Electronics electronics){
        boolean itemAdded = itemDAO.addItem(electronics); // id se tu tang trong ItemDAO
        if (itemAdded){
            String sql = "INSERT INTO electronics (id, warranty) VALUES (?, ?)";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setInt(1, electronics.getId()); // Dùng ID đã được tạo trong ItemDAO
                pstmt.setInt(2, electronics.getWarrantyMonths());

                int rowsInserted = pstmt.executeUpdate();
                return rowsInserted > 0;
            }catch(SQLException e){
                return false;
            }
        }
        return false;
    }

    // them san pham bang thu cong
    public boolean addElectronicsItem(int id, int sellerId, String name, String description, double startingPrice,double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
                                      int warrantyMonths){
        Electronics electronics = new Electronics(id, sellerId,name, description, startingPrice,currentPrice, startTime, endTime, status, highestBidderId,
                   warrantyMonths);
        return addElectronicsItem(electronics);
    }

    // lay thong tin cua electronics tu id
    public Electronics getElectronicsItemById(int itemId){
        String sql = "SELECT i.*, e.warranty FROM items i INNER JOIN electronics e ON i.id = e.id WHERE i.id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    Electronics elec = new Electronics(
                        rs.getInt("id"),
                        rs.getInt("seller_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("starting_price"),
                        rs.getInt("warranty"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                    );
                    elec.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                    elec.setStatus(AuctionStatus.valueOf(rs.getString("status")));
                    elec.setHighestBidderId(rs.getInt("highest_bidder_id"));
                    return elec; 
                }   
            }
            return null;
        }catch(SQLException e){
            return null;
        }
    }
}
