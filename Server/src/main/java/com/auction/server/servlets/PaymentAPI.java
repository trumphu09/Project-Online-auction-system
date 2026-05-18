package com.auction.server.servlets;

import com.auction.controller.PaymentController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class PaymentAPI extends HttpServlet {

    private final PaymentController paymentController = new PaymentController();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        
        String jsonResponse = paymentController.handleExecutePayment(jsonRequest);

        // LUÔN TRẢ VỀ HTTP 200 ĐỂ CLIENT NHẬN ĐƯỢC RESPONSE
        // Phân biệt thành công/lỗi bằng JSON status field, không dùng HTTP status code
        resp.setStatus(HttpServletResponse.SC_OK);
        
        resp.getWriter().write(jsonResponse);
    }
}