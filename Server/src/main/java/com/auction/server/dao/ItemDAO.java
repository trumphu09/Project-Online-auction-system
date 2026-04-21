package com.auction.server.dao;

import com.auction.server.models.*;
import java.sql.*;

public class ItemDAO {

    // ==============================================
    // 1. THÊM SẢN PHẨM MỚI (XỬ LÝ BẢNG CHA & CON)
    // ==============================================
    public boolean addItem(ItemDTO item) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        try {
            conn.setAutoCommit(false); 

            // Bước 1: Lưu bảng items (Cha)
            String sqlParent = "INSERT INTO items (seller_id, name, description, starting_price, category) VALUES (?, ?, ?, ?, ?)";
            int generatedItemId = -1;

            try (PreparedStatement pstParent = conn.prepareStatement(sqlParent, Statement.RETURN_GENERATED_KEYS)) {
                pstParent.setInt(1, item.getSellerId());
                pstParent.setString(2, item.getName());
                pstParent.setString(3, item.getDescription());
                pstParent.setDouble(4, item.getStartingPrice());
                pstParent.setString(5, item.getCategory());
                
                if (pstParent.executeUpdate() > 0) {
                    try (ResultSet rs = pstParent.getGeneratedKeys()) {
                        if (rs.next()) generatedItemId = rs.getInt(1);
                    }
                }
            }

            if (generatedItemId == -1) {
                conn.rollback(); return false; 
            }

            // Bước 2: Lưu bảng con
            boolean childInsertSuccess = false;

            if (item instanceof ArtDTO) {
                ArtDTO art = (ArtDTO) item;
                String sqlChild = "INSERT INTO artworks (item_id, artist, creation_year, material) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstChild = conn.prepareStatement(sqlChild)) {
                    pstChild.setInt(1, generatedItemId);
                    pstChild.setString(2, art.getArtist());
                    pstChild.setInt(3, art.getCreationYear());
                    pstChild.setString(4, art.getMaterial());
                    childInsertSuccess = pstChild.executeUpdate() > 0;
                }
            } else if (item instanceof ElectronicsDTO) {
                ElectronicsDTO elec = (ElectronicsDTO) item;
                String sqlChild = "INSERT INTO electronics (item_id, warranty_months) VALUES (?, ?)";
                try (PreparedStatement pstChild = conn.prepareStatement(sqlChild)) {
                    pstChild.setInt(1, generatedItemId);
                    pstChild.setInt(2, elec.getWarrantyMonths());
                    childInsertSuccess = pstChild.executeUpdate() > 0;
                }
            } else if (item instanceof VehicleDTO) {
                VehicleDTO veh = (VehicleDTO) item;
                String sqlChild = "INSERT INTO vehicles (item_id, brand, mileage, condition_state) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstChild = conn.prepareStatement(sqlChild)) {
                    pstChild.setInt(1, generatedItemId);
                    pstChild.setString(2, veh.getBrand());
                    pstChild.setInt(3, veh.getMileage());
                    pstChild.setString(4, veh.getCondition());
                    childInsertSuccess = pstChild.executeUpdate() > 0;
                }
            }

            // Bước 3: Chốt
            if (childInsertSuccess) {
                conn.commit(); return true;
            } else {
                conn.rollback(); return false;
            }

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // ==============================================
    // 2. LẤY THÔNG TIN SẢN PHẨM BẰNG ID (DÙNG LEFT JOIN)
    // ==============================================
    public ItemDTO getItemById(int itemId) {
        // Lệnh LEFT JOIN thần thánh: Gộp cả 4 bảng lại làm 1 để tìm kiếm
        String sql = "SELECT i.*, " +
                     "a.artist, a.creation_year, a.material, " +
                     "e.warranty_months, " +
                     "v.brand, v.mileage, v.condition_state " +
                     "FROM items i " +
                     "LEFT JOIN artworks a ON i.id = a.item_id " +
                     "LEFT JOIN electronics e ON i.id = e.item_id " +
                     "LEFT JOIN vehicles v ON i.id = v.item_id " +
                     "WHERE i.id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Lấy dữ liệu chung của bảng Cha
                    int sellerId = rs.getInt("seller_id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    double startingPrice = rs.getDouble("starting_price");
                    String category = rs.getString("category");

                    // Trả về đúng đối tượng DTO con dựa vào category
                    if ("ART".equals(category)) {
                        return new ArtDTO(itemId, sellerId, name, description, startingPrice,
                                rs.getString("artist"), rs.getInt("creation_year"), rs.getString("material"));
                    } 
                    else if ("ELECTRONICS".equals(category)) {
                        return new ElectronicsDTO(itemId, sellerId, name, description, startingPrice,
                                rs.getInt("warranty_months"));
                    } 
                    else if ("VEHICLE".equals(category)) {
                        return new VehicleDTO(itemId, sellerId, name, description, startingPrice,
                                rs.getString("brand"), rs.getInt("mileage"), rs.getString("condition_state"));
                    }
                    else {
                        // Nếu là Item chung chung không có bảng con
                        return new ItemDTO(itemId, sellerId, name, description, startingPrice, category);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm sản phẩm: " + e.getMessage());
        }
        return null;
    }
}