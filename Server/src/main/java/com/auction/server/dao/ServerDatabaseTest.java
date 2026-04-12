
package com.auction.server.dao;

import java.time.LocalDateTime;
import java.util.List;

import com.auction.server.dao.*;
import com.auction.server.models.*;

public class ServerDatabaseTest {
    public static void main(String[] args) {
        long time = System.currentTimeMillis();

        UserDAO userDAO = new UserDAO();
        BidderDAO bidderDAO = new BidderDAO();
        SellerDAO sellerDAO = new SellerDAO();
        ItemDAO itemDAO = new ItemDAO();
        ArtworkDAO artworkDAO = new ArtworkDAO();
        ElectronicsDAO electronicsDAO = new ElectronicsDAO();
        VehicleDAO vehicleDAO = new VehicleDAO();
        BidsDAO bidsDAO = new BidsDAO();
        PaymentDAO paymentDAO = new PaymentDAO();
        AdminDAO adminDAO = new AdminDAO();

        System.out.println("=== START FULL DB INTEGRATION TEST ===");

        // 1) Create Seller
        Seller seller = new Seller(0, "seller_" + time, "pass123", "seller" + time + "@test.com");
        boolean sellerCreated = sellerDAO.registerSeller(seller);
        System.out.println("[Seller Created] " + sellerCreated + " | id=" + seller.getId());

        // 2) Create two Bidders
        Bidder bidder1 = new Bidder(0, "bidder1_" + time, "pass123", "b1" + time + "@test.com", 0.0);
        Bidder bidder2 = new Bidder(0, "bidder2_" + time, "pass123", "b2" + time + "@test.com", 0.0);
        boolean b1Created = bidderDAO.registerBidder(bidder1);
        boolean b2Created = bidderDAO.registerBidder(bidder2);
        System.out.println("[Bidder1 Created] " + b1Created + " | id=" + bidder1.getId());
        System.out.println("[Bidder2 Created] " + b2Created + " | id=" + bidder2.getId());

        // 3) Top-up bidders' balances
        boolean b1TopUp = bidderDAO.updateBalance(bidder1.getId(), 1000.0);
        boolean b2TopUp = bidderDAO.updateBalance(bidder2.getId(), 500.0);
        System.out.println("[TopUp Balances] b1=" + b1TopUp + " b2=" + b2TopUp);

        // 4) Add items: artwork, electronics, vehicle
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(30);

        Art art = new Art(0, seller.getId(), "Artwork_" + time, "Nice painting", 100.0, start, end,
                "ArtistName", 2021, "Oil");
        boolean artAdded = artworkDAO.addArtworkItem(art);
        System.out.println("[Artwork Added] " + artAdded + " | id=" + art.getId());

        Electronics elec = new Electronics(0, seller.getId(), "Phone_" + time, "Smartphone", 50.0, 12, start, end);
        boolean elecAdded = electronicsDAO.addElectronicsItem(elec);
        System.out.println("[Electronics Added] " + elecAdded + " | id=" + elec.getId());

        Vehicle veh = new Vehicle(0, seller.getId(), "Car_" + time, "Sedan", 500.0, start, end, "BrandX", 30000, "Good");
        boolean vehAdded = vehicleDAO.addVehicleItem(veh);
        System.out.println("[Vehicle Added] " + vehAdded + " | id=" + veh.getId());

        // 5) Move artwork auction to RUNNING
        boolean statusUpdated = itemDAO.updateStatus(art.getId(), AuctionStatus.RUNNING);
        System.out.println("[Set Artwork RUNNING] " + statusUpdated);

        // 6) Place bids (bidder1 then bidder2)
        boolean bid1 = bidsDAO.executeBid(art.getId(), bidder1.getId(), 150.0);
        System.out.println("[Bidder1 Bid 150] " + bid1);

        boolean bid2 = bidsDAO.executeBid(art.getId(), bidder2.getId(), 200.0);
        System.out.println("[Bidder2 Bid 200] " + bid2);

        // 7) Retrieve bid history
        List<BidTransactionDTO> history = bidsDAO.getBidHistoryByItemId(art.getId());
        System.out.println("[Bid History Size] " + history.size());
        for (BidTransactionDTO h : history) {
            System.out.println("  -> id=" + h.getId() + " bidderId=" + h.getBidderId() + " user=" + h.getBidderUsername()
                    + " amount=" + h.getBidAmount() + " time=" + h.getTimestamp());
        }

        // 8) Create payment invoice for winner (highestBidder)
        // Determine winner via item get (ensure highest_bidder_id set)
        Item dbItem = itemDAO.getItemById(art.getId());
        int winnerId = dbItem.getHighestBidderId();
        double finalPrice = dbItem.getCurrentMaxPrice();
        System.out.println("[Winner] bidderId=" + winnerId + " price=" + finalPrice);

        boolean invoiceCreated = paymentDAO.createPaymentInvoice(art.getId(), winnerId, seller.getId(), finalPrice);
        System.out.println("[Invoice Created] " + invoiceCreated);

        // 9) Find the PENDING payment id and process it
        List<com.auction.server.models.Payment> payments = paymentDAO.getPaymentHistoryByUser(seller.getId());
        Integer pendingPaymentId = null;
        for (com.auction.server.models.Payment p : payments) {
            if (p.getStatus() == PaymentStatus.PENDING) {
                pendingPaymentId = p.getId();
                break;
            }
        }
        if (pendingPaymentId != null) {
            boolean processed = paymentDAO.processPayment(pendingPaymentId);
            System.out.println("[Payment Processed] id=" + pendingPaymentId + " -> " + processed);
        } else {
            System.out.println("[No PENDING payment found for seller]");
        }

        // 10) Admin: list all users (returns DTO objects)
        List<?> allUsers = adminDAO.getAllUsers();
        System.out.println("[All Users Count] " + allUsers.size());

        System.out.println("=== END FULL DB INTEGRATION TEST ===");
    }
}