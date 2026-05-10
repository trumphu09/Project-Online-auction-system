package com.auction.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;

/**
 * ImageAPI — Servlet phục vụ ảnh sản phẩm qua HTTP.
 *
 * URL: GET /api/images/{filename}
 * Ví dụ: GET /api/images/1748000000000_myphone.jpg
 *
 * Cần thêm mapping này vào web.xml:
 *
 *   <servlet>
 *       <servlet-name>ImageAPI</servlet-name>
 *       <servlet-class>com.auction.server.servlets.ImageAPI</servlet-class>
 *   </servlet>
 *   <servlet-mapping>
 *       <servlet-name>ImageAPI</servlet-name>
 *       <url-pattern>/api/images/*</url-pattern>
 *   </servlet-mapping>
 *
 * Servlet này giải quyết vấn đề: Server lưu ảnh ở đường dẫn tuyệt đối trên máy server
 * (vd: C:\...\loads\abc.jpg), nhưng client không thể load file đó trực tiếp.
 * Thay vào đó, client sẽ request qua URL HTTP này.
 */
public class ImageAPI extends HttpServlet {

    // Thư mục lưu ảnh — phải khớp với thư mục trong ItemDAO.addItem()
    // ItemDAO lưu vào "uploads/" (relative path từ thư mục chạy server)
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Lấy tên file từ URL: /api/images/{filename}
        String pathInfo = req.getPathInfo(); // ví dụ: "/1748000000000_myphone.jpg"
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu tên file ảnh.\"}");
            return;
        }

        // Xóa dấu "/" đầu tiên để lấy filename
        String filename = pathInfo.substring(1);

        // Ngăn Path Traversal Attack (bảo mật): không cho phép "../" trong tên file
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Tên file không hợp lệ.\"}");
            return;
        }

        // Tìm file ảnh trong thư mục uploads
        // Thử đường dẫn tương đối trước (khi server chạy từ thư mục gốc project)
        File imageFile = findImageFile(filename, req);

        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Không tìm thấy ảnh: " + filename + "\"}");
            return;
        }

        // Xác định MIME type từ extension
        String mimeType = getMimeType(filename);
        resp.setContentType(mimeType);
        resp.setContentLengthLong(imageFile.length());

        // Cache ảnh trong 1 giờ để tăng hiệu suất
        resp.setHeader("Cache-Control", "public, max-age=3600");

        // Gửi file ảnh về client
        try (InputStream in = new FileInputStream(imageFile);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Tìm file ảnh — hỗ trợ nhiều vị trí khác nhau tùy cấu hình server.
     */
    private File findImageFile(String filename, HttpServletRequest req) {
        // 1. Tìm trong thư mục "uploads/" tương đối từ thư mục server đang chạy
        File relative = new File(UPLOAD_DIR, filename);
        if (relative.exists()) return relative;

        // 2. Tìm trong thư mục "uploads/" của web application (Tomcat)
        String realPath = req.getServletContext().getRealPath("/" + UPLOAD_DIR + "/" + filename);
        if (realPath != null) {
            File webappFile = new File(realPath);
            if (webappFile.exists()) return webappFile;
        }

        // 3. Thử đường dẫn từ System property (nếu server set)
        String uploadPath = System.getProperty("auction.upload.dir");
        if (uploadPath != null) {
            File configFile = new File(uploadPath, filename);
            if (configFile.exists()) return configFile;
        }

        return null; // Không tìm thấy
    }

    /**
     * Xác định MIME type dựa theo phần mở rộng của file.
     */
    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream"; // Fallback
    }
}