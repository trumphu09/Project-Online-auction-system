package com.auction.server.dao;
import com.auction.server.models.AuctionStatus;
import com.auction.server.models.Electronics;
import com.auction.server.models.Vehicle;
import java.sql.*;
import java.time.LocalDateTime;
public class VehicleDAO {
    // add new vehicle item
    ItemDAO itemDAO = new ItemDAO();
    public boolean addVehicleItem(Vehicle vehicle){
        boolean itemAdded = itemDAO.addItem(vehicle); // id se tu tang trong ItemDAO
        if (itemAdded){
            String sql = "INSERT INTO vehicles (item_id,brand,mileage,condition_status ) VALUES (?, ?, ?, ?)";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setInt(1, vehicle.getId()); // Dùng ID đã được tạo trong ItemDAO
                pstmt.setString(2, vehicle.getBrand());
                pstmt.setInt(3, vehicle.getMileage());
                pstmt.setString(4, vehicle.getCondition());
                int rowsInserted = pstmt.executeUpdate();
                return rowsInserted > 0;
            }catch(SQLException e){
                return false;
            }
        }
        return false;
    }

    // add vehicle bang thu cong
    public boolean addVehicleItem(int id, int sellerId, String name, String description, double startingPrice,double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
                                  String brand, int mileage, String condition){
        Vehicle vehicle = new Vehicle(id, sellerId,name, description, startingPrice, currentPrice, startTime, endTime, status, highestBidderId,
                   brand,  mileage,  condition);
        return addVehicleItem(vehicle);
    }

    // get vehicle by id
    public Vehicle getVehicleItemById(int itemId){
        String sql = "SELECT i.*, v.brand, v.mileage, v.condition_status FROM items i INNER JOIN vehicles v ON i.id = v.id WHERE i.id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    Vehicle vi = new Vehicle(
                        rs.getInt("id"),
                        rs.getInt("seller_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("starting_price"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("brand"),
                        rs.getInt("mileage"),
                        rs.getString("condition_status")
                    );
                    vi.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                    vi.setStatus(AuctionStatus.valueOf(rs.getString("status")));
                    vi.setHighestBidderId(rs.getInt("highest_bidder_id"));
                    return vi; 
                }   
            }
            return null;
        }catch(SQLException e){
            return null;
        }
    }
}
