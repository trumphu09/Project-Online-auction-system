package com.auction.server.dao;

import com.auction.server.models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDAO {
    
    // BỘ ĐĂNG KÝ (REGISTRY)
    // Dùng Class để map khi Insert
    private final Map<Class<? extends ItemDTO>, IItemSubDAO> classToDAORegistry = new HashMap<>();
    // Dùng String để map khi Select (Lấy từ DB lên)
    private final Map<String, IItemSubDAO> categoryToDAORegistry = new HashMap<>();

    public ItemDAO() {
        // Đăng ký các DAO con vào hệ thống
        registerDAO(ArtDTO.class, "ART", new ArtDAO());
        registerDAO(ElectronicsDTO.class, "ELECTRONICS", new ElectronicsDAO());
        registerDAO(VehicleDTO.class, "VEHICLE", new VehicleDAO());
    }

    // Hàm hỗ trợ đăng ký nhanh cho cả 2 Map
    private void registerDAO(Class<? extends ItemDTO> clazz, String category, IItemSubDAO dao) {
        classToDAORegistry.put(clazz, dao);
        categoryToDAORegistry.put(category, dao);
    }

    // ==========================================
    // 1. THÊM SẢN PHẨM (Đã chuẩn OCP)
    // ==========================================
    public boolean addItem(ItemDTO item) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); 

            // Lưu bảng cha
            String sqlItem = "INSERT INTO items (seller_id, name, description, starting_price, category) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
                pstmtItem.setInt(1, item.getSellerId());
                pstmtItem.setString(2, item.getName());
                pstmtItem.setString(3, item.getDescription());
                pstmtItem.setDouble(4, item.getStartingPrice());
                pstmtItem.setString(5, item.getCategory());
                pstmtItem.executeUpdate();

                try (ResultSet rs = pstmtItem.getGeneratedKeys()) {
                    if (rs.next()) item.setId(rs.getInt(1));
                    else throw new SQLException("Lỗi: Không lấy được ID của sản phẩm.");
                }
            }

            // Gọi DAO con lưu bảng con
            IItemSubDAO subDAO = classToDAORegistry.get(item.getClass());
            if (subDAO != null) {
                subDAO.insertSubItem(conn, item); 
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ==========================================
    // 2. LẤY 1 SẢN PHẨM THEO ID (Chuẩn OCP)
    // ==========================================
    public ItemDTO getItemById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int sellerId = rs.getInt("seller_id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    double startingPrice = rs.getDouble("starting_price");
                    String category = rs.getString("category");

                    // Nhờ DAO con query tiếp để ráp thành Object hoàn chỉnh
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        return subDAO.fetchSubItem(conn, id, sellerId, name, description, startingPrice);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getItemById: " + e.getMessage());
        }
        return null;
    }

    // ==========================================
    // 3. LẤY TOÀN BỘ SẢN PHẨM (Chuẩn OCP)
    // ==========================================
    public List<ItemDTO> getAllItems() {
        List<ItemDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                int sellerId = rs.getInt("seller_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double startingPrice = rs.getDouble("starting_price");
                String category = rs.getString("category");

                // Giao việc cho DAO con tương ứng
                IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                if (subDAO != null) {
                    ItemDTO fullItem = subDAO.fetchSubItem(conn, id, sellerId, name, description, startingPrice);
                    if (fullItem != null) {
                        list.add(fullItem);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllItems: " + e.getMessage());
        }
        return list;
    }
}