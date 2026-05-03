package com.auction.server.dao;

import com.auction.server.models.ElectronicsDTO;
import com.auction.server.models.ItemDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ElectronicsDAO implements IItemSubDAO {

    @Override
    public void insertSubItem(Connection conn, ItemDTO item) throws SQLException {
        ElectronicsDTO elec = (ElectronicsDTO) item;
        String sql = "INSERT INTO electronics (item_id, warranty_months) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, elec.getId());
            pstmt.setInt(2, elec.getWarrantyMonths());
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

        String sql = "SELECT * FROM electronics WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs2 = pstmt.executeQuery()) {
                if (rs2.next()) {
                    return new ElectronicsDTO(itemId, sellerId, name, description, startingPrice,
                            rs2.getInt("warranty_months"));
                }
            }
        }
        return null;
    }
}