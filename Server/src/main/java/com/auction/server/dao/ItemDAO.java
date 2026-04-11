package com.auction.server.dao;
import com.auction.server.models.Item;
import com.auction.server.models.AuctionStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    // TODO: Thêm các phương thức để thao tác với bảng Item trong Database
    // add item, get item by id, update item, delete item, list all items are bidding, etc.

    // addItem(Item item) - Thêm một mặt hàng mới vào cơ sở dữ liệu
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (seller_id, name, description, starting_price, current_max_price, status, category, start_time, end_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getSellerId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());
            pstmt.setDouble(5, item.getStartingPrice()); // Mới tạo thì giá max = giá khởi điểm
            pstmt.setString(6, item.getStatus().name()); // Sử dụng enum.name()
            pstmt.setString(7, item.getCategory());
            pstmt.setTimestamp(8, Timestamp.valueOf(item.getStartTime()));
            pstmt.setTimestamp(9, Timestamp.valueOf(item.getEndTime()));

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    // lay item theo id
    public Item getItemById(int itemId) {
        String sql = "SELECT * FROM items WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs); // Gọi hàm phụ trợ 
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    // Hàm phụ trợ để chuyển đổi ResultSet thành Item
    private Item mapRowToItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int sellerId = rs.getInt("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");
        double currentMaxPrice = rs.getDouble("current_max_price");
        java.time.LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        java.time.LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
        String statusStr = rs.getString("status");
        AuctionStatus status = AuctionStatus.valueOf(statusStr);
        int highestBidderId = rs.getInt("highest_bidder_id");
        String category = rs.getString("category");
        
        // Tạo item dựa trên category - sử dụng constructor thứ 2 của Item
        Item item = new Item(id, sellerId, name, description, startingPrice, startTime, endTime, category) {
            @Override
            public void printInfo() {
                System.out.println("Item: " + name);
            }
        };
        
        item.setHighestBidderId(highestBidderId);
        item.setCurrentMaxPrice(currentMaxPrice);
        item.setStatus(status);
        return item;
    }

    // thay doi trang thai cua item
    public boolean updateStatus(int id, AuctionStatus newStatus){
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        Connection conn  = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(2,id);
            pstmt.setString(1, newStatus.name());

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }catch(SQLException e){
            return false;
        }
    }

    public boolean updateCurrentMaxPrice(int itemId, double newPrice, int bidderId){
        String sql = "UPDATE items SET current_max_price = ?, highest_bidder_id = ? WHERE id = ?"+
                        "AND current_max_price < ? AND status = 'RUNNING'";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, bidderId);
            pstmt.setInt(3, itemId);
            pstmt.setDouble(4, newPrice);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }catch(SQLException e){
            return false;
        }
    }
    // Import cái này ở đầu file ItemDAO.java nếu chưa có
    // import java.util.List;
    // import java.util.ArrayList;

    // Hàm lấy danh sách tất cả sản phẩm
    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        // Lấy tất cả, sắp xếp theo ID giảm dần (đồ mới thêm sẽ lên đầu)
        String sql = "SELECT * FROM items ORDER BY id DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Duyệt qua từng dòng trong Database
            while (rs.next()) {
                // Tận dụng lại luôn cái hàm mapRowToItem thần thánh của bạn mày!
                Item item = mapRowToItem(rs);
                itemList.add(item);
            }
        } catch (SQLException e) {
            System.err.println("✗ [Lỗi lấy danh sách Item] " + e.getMessage());
        }
        return itemList;
    }

}
