package com.auction.server.dao;

import com.auction.server.models.ItemDTO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IItemSubDAO {
    void insertSubItem(Connection conn, ItemDTO item) throws SQLException;
    ItemDTO fetchSubItem(Connection conn, ResultSet rs) throws SQLException;
}