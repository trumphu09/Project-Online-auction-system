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
    public ItemDTO fetchSubItem(Connection conn, int itemId, int sellerId, String name, String description, double startingPrice) throws SQLException {
        // Chỉ query bảng con của nó
        String sql = "SELECT * FROM artworks WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Lắp ráp các trường cha (truyền vào) + trường con (vừa query) thành Object hoàn chỉnh
                    return new ArtDTO(itemId, sellerId, name, description, startingPrice,
                            rs.getString("artist"), rs.getInt("creation_year"), rs.getString("material"));
                }
            }
        }
        return null; // Nếu không tìm thấy
    }
}