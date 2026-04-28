package com.auction.server.dao;

import com.auction.server.models.ItemDTO;
import java.sql.Connection;
import java.sql.SQLException;

public interface IItemSubDAO {
    // 1. Dành cho luồng thêm sản phẩm
    void insertSubItem(Connection conn, ItemDTO item) throws SQLException;

    // 2. Dành cho luồng lấy dữ liệu (MỚI)
    // Nhận vào các thông tin cơ bản từ bảng cha, DAO con sẽ tự query thêm chi tiết và trả về Object hoàn chỉnh
    ItemDTO fetchSubItem(Connection conn, int itemId, int sellerId, String name, String description, double startingPrice) throws SQLException;
}