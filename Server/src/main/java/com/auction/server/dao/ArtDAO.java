package com.auction.server.dao;

import com.auction.server.models.ArtDTO;
import com.auction.server.models.ItemDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtDAO implements IItemSubDAO {
    @Override
    public void insertSubItem(Connection conn, ItemDTO item) throws SQLException {
        ArtDTO art = (ArtDTO) item;
        String sql = "INSERT INTO artworks (item_id, artist, creation_year, material) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, art.getId());
            pstmt.setString(2, art.getArtist());
            pstmt.setInt(3, art.getCreationYear());
            pstmt.setString(4, art.getMaterial());
            pstmt.executeUpdate();
        }
    }

    @Override
    public ItemDTO fetchSubItem(Connection conn, ResultSet rs) throws SQLException {
        // Đọc cột của bảng items từ ResultSet 'rs' (dòng hiện tại)
        int itemId = rs.getInt("id");
        int sellerId = rs.getInt("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");

        // Query bảng con artworks
        String sql = "SELECT * FROM artworks WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs2 = pstmt.executeQuery()) {
                if (rs2.next()) {
                    return new ArtDTO(itemId, sellerId, name, description, startingPrice,
                            rs2.getString("artist"), rs2.getInt("creation_year"), rs2.getString("material"));
                }
            }
        }
        return null;
    }
}