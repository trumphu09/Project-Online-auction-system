package com.auction.server.dao;

import com.auction.server.models.VehicleDTO;
import com.auction.server.models.ItemDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VehicleDAO implements IItemSubDAO {

    @Override
    public void insertSubItem(Connection conn, ItemDTO item) throws SQLException {
        VehicleDTO vehicle = (VehicleDTO) item;
        // Chú ý: Cột trong DB là condition_state để tránh trùng từ khóa SQL
        String sql = "INSERT INTO vehicles (item_id, brand, mileage, condition_state) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicle.getId());
            pstmt.setString(2, vehicle.getBrand());
            pstmt.setInt(3, vehicle.getMileage());
            pstmt.setString(4, vehicle.getCondition()); // Biến trong Java vẫn là condition
            pstmt.executeUpdate();
        }
    }

    @Override
    public ItemDTO fetchSubItem(Connection conn, int itemId, int sellerId, String name, String description, double startingPrice) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE item_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Ráp dữ liệu cha + 3 cột của bảng con
                    return new VehicleDTO(
                            itemId, sellerId, name, description, startingPrice,
                            rs.getString("brand"), 
                            rs.getInt("mileage"), 
                            rs.getString("condition_state")
                    );
                }
            }
        }
        return null;
    }
}