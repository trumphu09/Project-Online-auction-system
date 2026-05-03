package com.auction.server.factory;

import com.auction.server.dao.IUserSubDAO;
import com.auction.server.dao.BidderSubDAO;
import com.auction.server.dao.SellerSubDAO;

import java.util.HashMap;
import java.util.Map;

public class UserFactory {
    
    private static final Map<String, IUserSubDAO> registry = new HashMap<>();

    static {
        // Đăng ký các DAO xử lý bảng con tự động
        registry.put("BIDDER", new BidderSubDAO());
        registry.put("SELLER", new SellerSubDAO());
    }

    public static void registerUserType(String role, IUserSubDAO subDAO) {
        registry.put(role.toUpperCase(), subDAO);
    }

    public static IUserSubDAO getSubDAO(String role) {
        IUserSubDAO subDAO = registry.get(role.toUpperCase());
        if (subDAO == null) {
            throw new IllegalArgumentException("Không hỗ trợ xử lý Role: " + role);
        }
        return subDAO;
    }
}