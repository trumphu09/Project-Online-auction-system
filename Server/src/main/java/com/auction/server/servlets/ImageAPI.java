package com.auction.server.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ImageAPI extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Thiếu tên file ảnh.\"}");
            return;
        }

        // URL-decode để xử lý các ký tự đặc biệt trong tên file (ví dụ: %20 → space)
        String filename = URLDecoder.decode(pathInfo.substring(1), StandardCharsets.UTF_8);

        // Bảo mật: không cho phép path traversal hoặc directory separator
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"error\":\"Tên file không hợp lệ.\"}");
            return;
        }

        // FIX: truyền thêm servletContext để có thêm đường dẫn fallback
        File imageFile = findImageFile(filename, req);

        System.out.println("[ImageAPI] Request for file: " + filename);
        System.out.println("[ImageAPI] Resolved path: " + (imageFile != null ? imageFile.getAbsolutePath() : "null"));
        System.out.println("[ImageAPI] File exists: " + (imageFile != null && imageFile.exists()));

        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json; charset=UTF-8");
            System.err.println("[ImageAPI] ✗ File not found: " + filename);
            resp.getWriter().write("{\"error\":\"Không tìm thấy ảnh: " + filename + "\"}");
            return;
        }

        String mimeType = getMimeType(filename);
        resp.setContentType(mimeType);
        resp.setContentLengthLong(imageFile.length());
        resp.setHeader("Cache-Control", "public, max-age=3600");

        try (InputStream in = new FileInputStream(imageFile);
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    /**
     * Tìm file ảnh theo thứ tự ưu tiên:
     *  1. System property "auction.upload.dir"   ← ĐỒNG BỘ với ItemDAO (set từ AuctionServer.main)
     *  2. Thư mục uploads/ bên cạnh server JAR
     *  3. Servlet context real path + /uploads/  (dành cho Tomcat WAR deploy)
     *  4. Thư mục hiện tại (user.dir) + /uploads/
     *
     * Thứ tự này phải khớp với thứ tự ưu tiên trong ItemDAO.resolveUploadDir().
     */
    private File findImageFile(String filename, HttpServletRequest req) {

        // --- 1. System property (cách mạnh nhất, được config khi start server từ AuctionServer.main) ---
        String configuredDir = System.getProperty("auction.upload.dir");
        if (configuredDir != null && !configuredDir.isBlank()) {
            File f = new File(configuredDir, filename);
            System.out.println("[ImageAPI] Trying [system property]: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                System.out.println("[ImageAPI] ✓ FOUND at [system property]: " + f.getAbsolutePath());
                return f;
            }
        }

        // --- 2. Bên cạnh JAR (khớp với ItemDAO fallback 1) ---
        try {
            java.net.URL location = getClass().getProtectionDomain()
                    .getCodeSource().getLocation();
            File jarDir = new File(location.toURI()).getParentFile();
            File f = new File(jarDir, "uploads" + File.separator + filename);
            System.out.println("[ImageAPI] Trying [next to JAR]: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                System.out.println("[ImageAPI] ✓ FOUND at [next to JAR]: " + f.getAbsolutePath());
                return f;
            }
        } catch (Exception ignored) {}

        // --- 3. Servlet context real path (Tomcat WAR deploy) ---
        //    Khi deploy WAR, getRealPath("/uploads") trả về thư mục thật trên disk
        if (req != null && getServletContext() != null) {
            String realPath = getServletContext().getRealPath("/uploads");
            if (realPath != null) {
                File f = new File(realPath, filename);
                System.out.println("[ImageAPI] Trying [servlet context]: " + f.getAbsolutePath());
                if (f.exists() && f.isFile()) {
                    System.out.println("[ImageAPI] ✓ FOUND at [servlet context]: " + f.getAbsolutePath());
                    return f;
                }
            }
        }

        // --- 4. Working directory (user.dir) + uploads/ (khớp ItemDAO fallback 2) ---
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            File f = new File(userDir + File.separator + "uploads", filename);
            System.out.println("[ImageAPI] Trying [user.dir]: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                System.out.println("[ImageAPI] ✓ FOUND at [user.dir]: " + f.getAbsolutePath());
                return f;
            }
        }

        // Không tìm thấy ở bất kỳ đâu
        System.err.println("[ImageAPI] ✗ ERROR: file not found in any location for: " + filename);
        System.err.println("[ImageAPI] Tried locations:");
        if (configuredDir != null) System.err.println("  - " + configuredDir + File.separator + filename);
        try {
            java.net.URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            File jarDir = new File(location.toURI()).getParentFile();
            System.err.println("  - " + jarDir.getAbsolutePath() + File.separator + "uploads" + File.separator + filename);
        } catch (Exception ignored) {}
        if (userDir != null) {
            System.err.println("  - " + userDir + File.separator + "uploads" + File.separator + filename);
        }
        return null;
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}