package com.auction.server.dao;
import com.auction.server.models.AuctionStatus;
import com.auction.server.models.Art;
import java.sql.*;
import java.time.LocalDateTime;
public class ArtworkDAO {
    ItemDAO itemDAO = new ItemDAO();
    // add new artwork to database
    public boolean addArtworkItem(Art artwork){
        boolean itemAdded = itemDAO.addItem(artwork); // id se tu tang trong ItemDAO
        if (itemAdded){
            String sql = "INSERT INTO artworks (item_id, artist, creation_year, material ) VALUES (?, ?, ?, ?)";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                pstmt.setInt(1, artwork.getId()); // Dùng ID đã được tạo trong ItemDAO
                pstmt.setString(2,artwork.getArtist());
                pstmt.setInt(3, artwork.getCreationYear());
                pstmt.setString(4, artwork.getMaterial());
                int rowsInserted = pstmt.executeUpdate();
                return rowsInserted > 0;
            }catch(SQLException e){
                return false;
            }
        }
        return false;
    }

    // them san pham bang thu cong
    public boolean addArtworkItem(int id, int sellerId, String name, String description, double startingPrice,double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
                                  String artist, int creation_year, String material){
        Art artwork = new Art(id, sellerId,name, description, startingPrice,currentPrice, startTime, endTime, status, highestBidderId,
                   artist,  creation_year,  material);
        return addArtworkItem(artwork);
    }

    // get artwork by id
    public Art getArtworkItemById(int itemId){
        String sql = "SELECT i.*, a.artist, a.creation_year, a.material FROM items i INNER JOIN artworks a ON i.id = a.id WHERE i.id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    Art artwork = new Art(
                        rs.getInt("id"),
                        rs.getInt("seller_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("starting_price"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("artist"),
                        rs.getInt("creation_year"), 
                        rs.getString("material")
                        
                    );
                    artwork.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                    artwork.setStatus(AuctionStatus.valueOf(rs.getString("status")));
             
                    return artwork; 
                }   
            }
            return null;
        }catch(SQLException e){
            return null;
        }
    }
}
