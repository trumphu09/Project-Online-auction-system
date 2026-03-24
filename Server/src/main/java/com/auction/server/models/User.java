package com.auction.server.models;
// Dùng 'abstract' vì ta không bao giờ tạo ra một "User" chung chung.
// Ta chỉ tạo ra Bidder, Seller hoặc Admin.
public abstract class User {

    // ĐÓNG GÓI (Encapsulation): Mọi dữ liệu phải là 'private'
    // Không ai ở bên ngoài được phép gọi thẳng user.username để sửa.
    private final String id;
    private String username;
    private String password;
    private final String email;

    // Constructor: Dùng để khởi tạo dữ liệu khi tạo đối tượng mới
    public User(String id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // GETTER / SETTER: Cánh cửa duy nhất để đọc hoặc sửa đổi dữ liệu an toàn
    public String getUsername() {
        return username;
    }

    // Ví dụ: Khi đổi mật khẩu, ta có thể kiểm tra độ dài ở đây trước khi lưu
    public void setPassword(String password) {
        if(password.length() >= 6) {
            this.password = password;
        } else {
            System.out.println("Mật khẩu quá ngắn!");
        }
    }

    public String getId() { return id; }
    public String getEmail() { return email; }

    // TRỪU TƯỢNG (Abstraction): Một phương thức rỗng, ép các lớp con phải tự viết nội dung
    public abstract void showDashboard();
    public void setUsername(String newUsername) {
        this.username = newUsername;
    }
}