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
    public ItemDTO fetchSubItem(Connection conn, int itemId, int sellerId, String name, String description, double startingPrice) throws SQLException {
        String sql = "SELECT * FROM electronics WHERE item_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Ráp dữ liệu bảng cha và cột warranty_months của bảng con
                    return new ElectronicsDTO(
                            itemId, sellerId, name, description, startingPrice,
                            rs.getInt("warranty_months")
                    );
                }
            }
        }
        return null; // Trả về null nếu không tìm thấy dữ liệu mapping
    }
}