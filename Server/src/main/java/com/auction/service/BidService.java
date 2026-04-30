package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Item;
import java.util.List;

public class BidService {

    private static final BidService instance = new BidService();
    private final BidsDAO bidsDAO;

    private BidService() {
        this.bidsDAO = new BidsDAO();
    }

    public static BidService getInstance() {
        return instance;
    }

    public List<BidTransactionDTO> getBidHistory(int itemId) {
        if (itemId <= 0) {
            return null;
        }
        return bidsDAO.getBidHistoryByItemId(itemId);
    }

    public List<Item> getActiveBids(int userId) {
        if (userId <= 0) {
            return null;
        }
        return bidsDAO.getActiveBidsByUserId(userId);
    }
}