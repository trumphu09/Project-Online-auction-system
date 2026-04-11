package controller;

import com.auction.server.models.Auction;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.BidTransaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class GetAuctionDetailAPI extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        JsonObject result = new JsonObject();

        try {
            // 1. Lấy auctionId từ tham số trên URL (ví dụ: ?id=1)
            String idParam = request.getParameter("id");
            if (idParam == null) {
                result.addProperty("success", false);
                result.addProperty("message", "Thiếu tham số id!");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            int auctionId = Integer.parseInt(idParam);

            // 2. Tìm phiên đấu giá trong RAM qua AuctionManager
            Auction auction = AuctionManager.getInstance().getAuctionById(auctionId);

            if (auction != null) {
                result.addProperty("success", true);

                // Đóng gói thông tin cơ bản
                JsonObject data = new JsonObject();
                data.addProperty("auctionId", auction.getId());
                data.addProperty("itemName", auction.getItem().getName());
                data.addProperty("currentPrice", auction.getHighestBid());
                data.addProperty("endTime", auction.getEndTime().toString());
                data.addProperty("status", auction.getStatus().toString());
                data.addProperty("seller", auction.getSeller().getUsername());

                // Lấy lịch sử đặt giá (Bid History)
                JsonArray history = new JsonArray();
                List<BidTransaction> bids = auction.getBidHistory();
                for (BidTransaction bid : bids) {
                    JsonObject b = new JsonObject();
                    b.addProperty("bidder", bid.getBidder().getUsername());
                    b.addProperty("amount", bid.getBidAmount());
                    history.add(b);
                }
                data.add("history", history);

                result.add("data", data);
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "Không tìm thấy phiên đấu giá ID: " + auctionId);
            }

        } catch (Exception e) {
            result.addProperty("success", false);
            result.addProperty("message", "Lỗi: " + e.getMessage());
        }

        response.getWriter().write(gson.toJson(result));
    }
}