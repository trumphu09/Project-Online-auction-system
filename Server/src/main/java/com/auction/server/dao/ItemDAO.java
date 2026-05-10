package com.auction.server.dao;

import com.auction.server.models.ItemDTO;
import com.auction.server.models.ArtDTO;
import com.auction.server.models.AuctionStatus;
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

        String findSql =
                "SELECT a.id AS auction_id, a.item_id " +
                "FROM auctions a " +
                "WHERE a.status = 'OPEN' AND a.start_time <= NOW()";

        String updateSql = "UPDATE auctions SET status = 'RUNNING' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement findStmt = conn.prepareStatement(findSql);
                ResultSet rs = findStmt.executeQuery()) {

                while (rs.next()) {
                    int auctionId = rs.getInt("auction_id");
                    int itemId = rs.getInt("item_id");

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, auctionId);
                        if (updateStmt.executeUpdate() > 0) {
                            ItemDTO item = getItemById(itemId);
                            if (item != null) {
                                item.setAuctionId(auctionId);
                                item.setStatus("RUNNING");
                                updatedItems.add(item);
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

        String findSql =
                "SELECT a.id AS auction_id, a.item_id " +
                "FROM auctions a " +
                "WHERE a.status = 'RUNNING' AND a.end_time <= NOW()";

        String updateSql = "UPDATE auctions SET status = 'FINISHED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement findStmt = conn.prepareStatement(findSql);
                ResultSet rs = findStmt.executeQuery()) {

                while (rs.next()) {
                    int auctionId = rs.getInt("auction_id");
                    int itemId = rs.getInt("item_id");

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, auctionId);
                        if (updateStmt.executeUpdate() > 0) {
                            ItemDTO item = getItemById(itemId);
                            if (item != null) {
                                item.setAuctionId(auctionId);
                                item.setStatus("FINISHED");
                                updatedItems.add(item);
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
        // 1. XỬ LÝ LƯU FILE ẢNH VÀO HỆ THỐNG
        if (item.getBase64Image() != null) {
            try {
                // Tạo thư mục uploads nếu chưa có
                String uploadDir = "uploads/";
                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // Đặt tên file duy nhất (dùng timestamp để không bị trùng)
                String fileName = System.currentTimeMillis() + "_" + item.getImagePath();
                String fullPath = uploadDir + fileName;

                // Giải mã Base64 và ghi ra file
                byte[] imageBytes = java.util.Base64.getDecoder().decode(item.getBase64Image());
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(fullPath)) {
                    fos.write(imageBytes);
                }

                // CẬP NHẬT ĐƯỜNG DẪN MỚI (Đường dẫn tương đối trong hệ thống)
                item.setImagePath(new java.io.File(fullPath).getAbsolutePath()); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. LƯU VÀO DATABASE (SQL giữ nguyên nhưng image_path giờ là đường dẫn của Server)
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String sqlItem = "INSERT INTO items (seller_id, name, description, starting_price, category, image_path) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
                pstmtItem.setInt(1, item.getSellerId());
                pstmtItem.setString(2, item.getName());
                pstmtItem.setString(3, item.getDescription());
                pstmtItem.setDouble(4, item.getStartingPrice());
                pstmtItem.setString(5, item.getCategory());
                pstmtItem.setString(6, item.getImagePath());

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
            
            // 3. TẠO PHIÊN ĐẤU GIÁ TRONG BẢNG AUCTIONS
            // Bổ sung seller_id và end_time vào câu lệnh SQL
            String sqlAuction =
                    "INSERT INTO auctions " +
                    "(item_id, seller_id, current_max_price, price_step, start_time, end_time, status, has_extended, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmtAuction = conn.prepareStatement(sqlAuction)) {
                pstmtAuction.setInt(1, item.getId());
                pstmtAuction.setInt(2, item.getSellerId());
                pstmtAuction.setDouble(3, item.getStartingPrice());
                pstmtAuction.setDouble(4, item.getPriceStep() > 0 ? item.getPriceStep() : 1000);

                if (item.getStartTime() != null && !item.getStartTime().isEmpty()) {
                    pstmtAuction.setTimestamp(5, java.sql.Timestamp.valueOf(item.getStartTime()));
                } else {
                    pstmtAuction.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
                }

                if (item.getEndTime() != null && !item.getEndTime().isEmpty()) {
                    pstmtAuction.setTimestamp(6, java.sql.Timestamp.valueOf(item.getEndTime()));
                } else {
                    pstmtAuction.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis() + 7L * 24 * 3600 * 1000));
                }

                pstmtAuction.setString(7, AuctionStatus.OPEN.name());
                pstmtAuction.setBoolean(8, false);
                pstmtAuction.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));

                pstmtAuction.executeUpdate();
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

            // 1. items
            String sqlItem = "UPDATE items SET name = ?, description = ?, starting_price = ?, category = ?, image_path = ? WHERE id = ?";
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) {
                pstmtItem.setString(1, item.getName());
                pstmtItem.setString(2, item.getDescription());
                pstmtItem.setDouble(3, item.getStartingPrice());
                pstmtItem.setString(4, item.getCategory());
                pstmtItem.setString(5, item.getImagePath());
                pstmtItem.setInt(6, item.getId());
                pstmtItem.executeUpdate();
            }

            // 2. bảng con
            if (item instanceof ArtDTO) {
                ArtDTO art = (ArtDTO) item;
                String sqlArt = "UPDATE artworks SET artist = ?, creation_year = ?, material = ? WHERE item_id = ?";
                try (PreparedStatement pstmtArt = conn.prepareStatement(sqlArt)) {
                    pstmtArt.setString(1, art.getArtist());
                    pstmtArt.setInt(2, art.getCreationYear());
                    pstmtArt.setString(3, art.getMaterial());
                    pstmtArt.setInt(4, art.getId());
                    pstmtArt.executeUpdate();
                }
            } else if (item instanceof ElectronicsDTO) {
                ElectronicsDTO elec = (ElectronicsDTO) item;
                String sqlElec = "UPDATE electronics SET warranty_months = ? WHERE item_id = ?";
                try (PreparedStatement pstmtElec = conn.prepareStatement(sqlElec)) {
                    pstmtElec.setInt(1, elec.getWarrantyMonths());
                    pstmtElec.setInt(2, elec.getId());
                    pstmtElec.executeUpdate();
                }
            } else if (item instanceof VehicleDTO) {
                VehicleDTO v = (VehicleDTO) item;
                String sqlVeh = "UPDATE vehicles SET brand = ?, mileage = ?, condition_state = ? WHERE item_id = ?";
                try (PreparedStatement pstmtVeh = conn.prepareStatement(sqlVeh)) {
                    pstmtVeh.setString(1, v.getBrand());
                    pstmtVeh.setInt(2, v.getMileage());
                    pstmtVeh.setString(3, v.getCondition());
                    pstmtVeh.setInt(4, v.getId());
                    pstmtVeh.executeUpdate();
                }
            }

            // 3. auctions: chỉ update các trường seller có thể sửa
            String sqlAuction =
                    "UPDATE auctions SET price_step = ?, start_time = ?, end_time = ? WHERE item_id = ?";

            try (PreparedStatement pstmtAuction = conn.prepareStatement(sqlAuction)) {
                pstmtAuction.setDouble(1, item.getPriceStep() > 0 ? item.getPriceStep() : 1000);

                if (item.getStartTime() != null && !item.getStartTime().trim().isEmpty()) {
                    pstmtAuction.setTimestamp(2, java.sql.Timestamp.valueOf(item.getStartTime().trim()));
                } else {
                    pstmtAuction.setNull(2, java.sql.Types.TIMESTAMP);
                }

                if (item.getEndTime() != null && !item.getEndTime().trim().isEmpty()) {
                    pstmtAuction.setTimestamp(3, java.sql.Timestamp.valueOf(item.getEndTime().trim()));
                } else {
                    pstmtAuction.setNull(3, java.sql.Types.TIMESTAMP);
                }

                pstmtAuction.setInt(4, item.getId());
                pstmtAuction.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }

    public ItemDTO getItemById(int id) {
        String sql =
                "SELECT i.id AS item_id, i.seller_id, i.name, i.description, i.starting_price, " +
                "i.category, i.image_path, i.created_at, " +
                "a.id AS auction_id, a.current_max_price, a.highest_bidder_id, a.price_step, " +
                "a.start_time, a.end_time, a.status " +
                "FROM items i " +
                "LEFT JOIN auctions a ON a.item_id = i.id " +
                "WHERE i.id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String category = rs.getString("category");
                    IItemSubDAO subDAO = categoryToDAORegistry.get(category.toUpperCase());
                    if (subDAO != null) {
                        ItemDTO item = subDAO.fetchSubItem(conn, rs);
                        if (item != null) {
                            int auctionId = rs.getInt("auction_id");
                            if (!rs.wasNull()) {
                                item.setAuctionId(auctionId);
                                item.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                                item.setHighestBidderId(rs.getInt("highest_bidder_id"));
                                item.setPriceStep(rs.getDouble("price_step"));
                                item.setStatus(rs.getString("status"));

                                java.sql.Timestamp st = rs.getTimestamp("start_time");
                                java.sql.Timestamp et = rs.getTimestamp("end_time");
                                item.setStartTime(st != null ? st.toString() : null);
                                item.setEndTime(et != null ? et.toString() : null);
                            }
                        }
                        return item;
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

        String sql =
                "SELECT i.id AS item_id, i.seller_id, i.name, i.description, i.starting_price, " +
                "i.category, i.image_path, i.created_at, " +
                "a.id AS auction_id, a.current_max_price, a.highest_bidder_id, a.price_step, " +
                "a.start_time, a.end_time, a.status " +
                "FROM items i " +
                "LEFT JOIN auctions a ON i.id = a.item_id " +
                "ORDER BY i.created_at DESC LIMIT ? OFFSET ?";

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
                            int auctionId = rs.getInt("auction_id");
                            if (!rs.wasNull()) {
                                fullItem.setAuctionId(auctionId);
                                fullItem.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                                fullItem.setHighestBidderId(rs.getInt("highest_bidder_id"));
                                fullItem.setPriceStep(rs.getDouble("price_step"));
                                fullItem.setStatus(rs.getString("status"));

                                java.sql.Timestamp st = rs.getTimestamp("start_time");
                                java.sql.Timestamp et = rs.getTimestamp("end_time");
                                fullItem.setStartTime(st != null ? st.toString() : null);
                                fullItem.setEndTime(et != null ? et.toString() : null);
                            }
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

        String sql =
                "SELECT i.id AS item_id, i.seller_id, i.name, i.description, i.starting_price, " +
                "i.category, i.image_path, i.created_at, " +
                "a.id AS auction_id, a.current_max_price, a.highest_bidder_id, a.price_step, " +
                "a.start_time, a.end_time, a.status " +
                "FROM items i " +
                "LEFT JOIN auctions a ON a.item_id = i.id " +
                "WHERE UPPER(i.category) = ? " +
                "ORDER BY i.created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.toUpperCase());
            pstmt.setInt(2, limit);
            pstmt.setInt(3, (page - 1) * limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ItemDTO item = mapRowToItemDTO(rs);
                    if (item != null) list.add(item);
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

        String sql =
                "SELECT i.*, " +
                "       auc.id AS auction_id, " +
                "       auc.status AS status, " +
                "       auc.current_max_price, " +
                "       auc.highest_bidder_id, " +
                "       auc.price_step, " +
                "       auc.start_time, " +
                "       auc.end_time, " +
                "       e.warranty_months, " +
                "       v.brand, v.mileage, v.condition_state, " +
                "       a.artist, a.creation_year, a.material " +
                "FROM items i " +
                "LEFT JOIN auctions auc ON i.id = auc.item_id " +
                "LEFT JOIN electronics e ON i.id = e.item_id " +
                "LEFT JOIN vehicles v ON i.id = v.item_id " +
                "LEFT JOIN artworks a ON i.id = a.item_id " +
                "WHERE i.seller_id = ? " +
                "ORDER BY i.created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sellerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    ItemDTO item;

                    if ("ELECTRONICS".equalsIgnoreCase(category)) {
                        ElectronicsDTO e = new ElectronicsDTO();
                        e.setWarrantyMonths(rs.getInt("warranty_months"));
                        item = e;
                    } else if ("VEHICLE".equalsIgnoreCase(category)) {
                        VehicleDTO v = new VehicleDTO();
                        v.setBrand(rs.getString("brand"));
                        v.setMileage(rs.getInt("mileage"));
                        v.setCondition(rs.getString("condition_state"));
                        item = v;
                    } else if ("ART".equalsIgnoreCase(category) || "ARTWORK".equalsIgnoreCase(category)) {
                        ArtDTO art = new ArtDTO();
                        art.setArtist(rs.getString("artist"));
                        art.setCreationYear(rs.getInt("creation_year"));
                        art.setMaterial(rs.getString("material"));
                        item = art;
                    } else {
                        item = new ItemDTO();
                    }

                    item.setId(rs.getInt("id"));
                    item.setSellerId(rs.getInt("seller_id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setStartingPrice(rs.getDouble("starting_price"));
                    item.setCategory(category);
                    item.setImagePath(rs.getString("image_path"));
                    item.setCreatedAt(rs.getString("created_at"));

                    int auctionId = rs.getInt("auction_id");
                    if (!rs.wasNull()) {
                        item.setAuctionId(auctionId);
                        item.setStatus(rs.getString("status"));
                        item.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                        item.setHighestBidderId(rs.getInt("highest_bidder_id"));
                        item.setPriceStep(rs.getDouble("price_step"));

                        java.sql.Timestamp st = rs.getTimestamp("start_time");
                        java.sql.Timestamp et = rs.getTimestamp("end_time");
                        item.setStartTime(st != null ? st.toString() : null);
                        item.setEndTime(et != null ? et.toString() : null);
                    }

                    list.add(item);
                }
            }
        } catch (Exception e) {
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

    // LOGIC: Nếu thời gian hiện tại cách end_time dưới 5 phút, tự động cộng thêm 5 phút vào end_time
    public boolean extendAuctionTimeIfNeeded(int auctionId) {
        String sql = "UPDATE auctions SET end_time = DATE_ADD(end_time, INTERVAL 5 MINUTE) " +
                     "WHERE id = ? AND TIMESTAMPDIFF(MINUTE, NOW(), end_time) <= 5";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, auctionId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu thực sự đã được gia hạn
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        /**
     * FIX: Tìm kiếm sản phẩm theo từ khóa trong tên hoặc mô tả.
     * Hàm này được gọi từ ItemService.searchItems() và ItemController.handleSearchItems().
     *
     * SQL JOIN với bảng auctions để lấy thêm thông tin phiên đấu giá.
     */
    public List<ItemDTO> searchItemsByKeyword(String keyword) {
        List<ItemDTO> result = new ArrayList<>();

        String sql =
                "SELECT i.id AS item_id, i.seller_id, i.name, i.description, " +
                "i.starting_price, i.category, i.image_path, i.created_at, " +
                "a.id AS auction_id, a.current_max_price, a.highest_bidder_id, a.price_step, " +
                "a.start_time, a.end_time, a.status " +
                "FROM items i " +
                "LEFT JOIN auctions a ON a.item_id = i.id " +
                "WHERE LOWER(i.name) LIKE ? OR LOWER(i.description) LIKE ? " +
                "ORDER BY i.id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            pstmt.setString(1, likeKeyword);
            pstmt.setString(2, likeKeyword);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ItemDTO item = mapRowToItemDTO(rs);
                    if (item != null) result.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi searchItemsByKeyword: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<ItemDTO> getItemsByCategory(String category) {
        List<ItemDTO> result = new ArrayList<>();

        String sql =
                "SELECT i.id AS item_id, i.seller_id, i.name, i.description, " +
                "i.starting_price, i.category, i.image_path, i.created_at, " +
                "a.id AS auction_id, a.current_max_price, a.highest_bidder_id, a.price_step, " +
                "a.start_time, a.end_time, a.status " +
                "FROM items i " +
                "LEFT JOIN auctions a ON a.item_id = i.id " +
                "WHERE UPPER(i.category) = ? " +
                "ORDER BY i.id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ItemDTO item = mapRowToItemDTO(rs);
                    if (item != null) result.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getItemsByCategory: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
 
    /**
     * Helper: Chuyển một dòng ResultSet thành ItemDTO.
     * Tái sử dụng cho searchItemsByKeyword và getItemsByCategory.
     *
     * LƯU Ý: Nếu trong ItemDAO của bạn đã có phương thức tương tự (ví dụ mapRow, mapItem...),
     * hãy dùng lại thay vì tạo phương thức này để tránh trùng lặp.
     */
    private ItemDTO mapRowToItemDTO(ResultSet rs) {
        try {
            ItemDTO item = new ItemDTO();
            item.setId(rs.getInt("item_id"));
            item.setSellerId(rs.getInt("seller_id"));
            item.setName(rs.getString("name"));
            item.setDescription(rs.getString("description"));
            item.setStartingPrice(rs.getDouble("starting_price"));
            item.setCategory(rs.getString("category"));

            String rawPath = rs.getString("image_path");
            item.setImagePath(extractFilename(rawPath));

            item.setCreatedAt(rs.getString("created_at"));

            int auctionId = rs.getInt("auction_id");
            if (!rs.wasNull()) {
                item.setAuctionId(auctionId);
                item.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                item.setHighestBidderId(rs.getInt("highest_bidder_id"));
                item.setPriceStep(rs.getDouble("price_step"));
                item.setStatus(rs.getString("status"));

                java.sql.Timestamp st = rs.getTimestamp("start_time");
                java.sql.Timestamp et = rs.getTimestamp("end_time");
                item.setStartTime(st != null ? st.toString() : null);
                item.setEndTime(et != null ? et.toString() : null);
            }

            return item;
        } catch (SQLException e) {
            System.err.println("Lỗi mapRowToItemDTO: " + e.getMessage());
            return null;
        }
    }
 
    // Lấy tên file từ đường dẫn tuyệt đối.
    private String extractFilename(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) return null;
        int lastSlash = Math.max(fullPath.lastIndexOf('/'), fullPath.lastIndexOf('\\'));
        return lastSlash >= 0 ? fullPath.substring(lastSlash + 1) : fullPath;
    }
}