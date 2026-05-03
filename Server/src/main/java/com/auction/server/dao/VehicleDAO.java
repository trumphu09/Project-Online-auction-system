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
        String sql = "INSERT INTO vehicles (item_id, brand, mileage, condition_state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicle.getId());
            pstmt.setString(2, vehicle.getBrand());
            pstmt.setInt(3, vehicle.getMileage());
            pstmt.setString(4, vehicle.getCondition());
            pstmt.executeUpdate();
        }
    }

    @Override
    public ItemDTO fetchSubItem(Connection conn, ResultSet rs) throws SQLException {
        int itemId = rs.getInt("id");
        int sellerId = rs.getInt("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");

        String sql = "SELECT * FROM vehicles WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs2 = pstmt.executeQuery()) {
                if (rs2.next()) {
                    return new VehicleDTO(itemId, sellerId, name, description, startingPrice,
                            rs2.getString("brand"),
                            rs2.getInt("mileage"),
                            rs2.getString("condition_state"));
                }
            }
        }
        return null;
    }
}