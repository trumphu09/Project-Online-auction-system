package com.auction.server.dao;

import com.auction.server.models.ItemDTO;
import com.auction.server.models.ArtDTO;
import com.auction.server.models.ElectronicsDTO;
import com.auction.server.models.VehicleDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDAO {

    private final Map<Class<? extends ItemDTO>, IItemSubDAO> classToDAORegistry = new HashMap<>();
    private final Map<String, IItemSubDAO> categoryToDAORegistry = new HashMap<>();

    public ItemDAO() {
        registerDAO(ArtDTO.class, "ART", new ArtDAO());
        registerDAO(ElectronicsDTO.class, "ELECTRONICS", new ElectronicsDAO());
        registerDAO(VehicleDTO.class, "VEHICLE", new VehicleDAO());
    }

    private void registerDAO(Class<? extends ItemDTO> clazz, String category, IItemSubDAO dao) {
        classToDAORegistry.put(clazz, dao);
        categoryToDAORegistry.put(category.toUpperCase(), dao);
    }

    public List<ItemDTO> updatePendingItemsToRunning() {
        List<ItemDTO> updatedItems = new ArrayList<>();
        String findSql = "SELECT * FROM items WHERE status = 'PENDING' AND start_time <= NOW()";
        String updateSql = "UPDATE items SET status = 'RUNNING' WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement findStmt = conn.prepareStatement(findSql);
                 ResultSet rs = findStmt.executeQuery()) {
                while (rs.next()) {
                    IItemSubDAO subDAO = categoryToDAORegistry.get(rs.getString("category").toUpperCase());
                    if (subDAO != null) {
                        ItemDTO item = subDAO.fetchSubItem(conn, rs);
                        if (item != null) {
                             try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setInt(1, item.getId());
                                if (updateStmt.executeUpdate() > 0) {
                                    updatedItems.add(item);
                                }
                            }
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedItems;
    }

    public List<ItemDTO> updateRunningItemsToEnded() {
        List<ItemDTO> updatedItems = new ArrayList<>();
        String findSql = "SELECT i.*, a.highest_bidder_id, a.current_max_price FROM items i JOIN auctions a ON i.id = a.item_id WHERE a.status = 'RUNNING' AND a.end_time <= NOW()";
        String updateSql = "UPDATE auctions SET status = 'ENDED' WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement findStmt = conn.prepareStatement(findSql);
                 ResultSet rs = findStmt.executeQuery()) {
                while (rs.next()) {
                    IItemSubDAO subDAO = categoryToDAORegistry.get(rs.getString("category").toUpperCase());
                    if (subDAO != null) {
                        ItemDTO item = subDAO.fetchSubItem(conn, rs);
                        if (item != null) {
                             try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                updateStmt.setInt(1, item.getId()); // Assuming auctionId is same as itemId for simplicity
                                if (updateStmt.executeUpdate() > 0) {
                                    updatedItems.add(item);
                                }
                            }
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedItems;
    }

    public boolean addItem(ItemDTO item) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sqlItem = "INSERT INTO items (seller_id, name, description, starting_price, category) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
                pstmtItem.setInt(1, item.getSellerId());
                pstmtItem.setString(2, item.getName());
                pstmtItem.setString(3, item.getDescription());
                pstmtItem.setDouble(4, item.getStartingPrice());
                pstmtItem.setString(5, item.getCategory());
                pstmtItem.executeUpdate();

                try (ResultSet rs = pstmtItem.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1));
                    } else {
                        throw new SQLException("Lỗi: Không lấy được ID của sản phẩm sau khi insert.");
                    }
                }
            }

            IItemSubDAO subDAO = classToDAORegistry.get(item.getClass());
            if (subDAO != null) {
                subDAO.insertSubItem(conn, item);
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean updateItem(ItemDTO item) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sqlItem = "UPDATE items SET name = ?, description = ?, starting_price = ? WHERE id = ? AND seller_id = ?";
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
                pstmtItem.setString(1, item.getName());
                pstmtItem.setString(2, item.getDescription());
                pstmtItem.setDouble(3, item.getStartingPrice());
                pstmtItem.setInt(4, item.getId());
                pstmtItem.setInt(5, item.getSellerId());
                
                int rowsUpdated = pstmtItem.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Không tìm thấy sản phẩm hoặc bạn không phải chủ sở hữu.");
                }
            }

            IItemSubDAO subDAO = classToDAORegistry.get(item.getClass());
            if (subDAO != null) {
                // subDAO.updateSubItem(conn, item);
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public ItemDTO getItemById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String category = rs.getString("category");
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        return subDAO.fetchSubItem(conn, rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ItemDTO> getAllItems(int page, int limit) {
        List<ItemDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            pstmt.setInt(2, (page - 1) * limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        ItemDTO fullItem = subDAO.fetchSubItem(conn, rs);
                        if (fullItem != null) {
                            list.add(fullItem);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) FROM items";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM items WHERE category IS NOT NULL AND category != '' ORDER BY category";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<ItemDTO> getItemsByCategory(String category, int page, int limit) {
        List<ItemDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE category = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, (page - 1) * limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        ItemDTO fullItem = subDAO.fetchSubItem(conn, rs);
                        if (fullItem != null) {
                            list.add(fullItem);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getCategoryItemCount(String category) {
        String sql = "SELECT COUNT(*) FROM items WHERE category = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<ItemDTO> getItemsBySellerId(int sellerId) {
        List<ItemDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sellerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        ItemDTO fullItem = subDAO.fetchSubItem(conn, rs);
                        if (fullItem != null) {
                            list.add(fullItem);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ItemDTO> getWonItemsByUserId(int userId) {
        List<ItemDTO> list = new ArrayList<>();
        String sql = "SELECT i.* FROM items i JOIN auctions a ON i.id = a.item_id WHERE a.highest_bidder_id = ? AND a.status IN ('FINISHED', 'PAID') ORDER BY a.end_time DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        ItemDTO fullItem = subDAO.fetchSubItem(conn, rs);
                        if (fullItem != null) {
                            list.add(fullItem);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}