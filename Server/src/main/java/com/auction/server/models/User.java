package com.auction.server.models;
// Dùng 'abstract' vì ta không bao giờ tạo ra một "User" chung chung.
// Ta chỉ tạo ra Bidder, Seller hoặc Admin.
public abstract class User extends Entity {

    // ĐÓNG GÓI (Encapsulation): Mọi dữ liệu phải là 'private'
    // Không ai ở bên ngoài được phép gọi thẳng user.username để sửa.
    private String username;
    private String password;
    private final String email;
    private boolean isActive;

    // ==========================================
    // CONSTRUCTOR 1: Dùng khi TẠO TÀI KHOẢN MỚI (Từ Giao diện)
    // Không cho phép truyền ID vào đây!
    // ==========================================
    public User(String username, String password, String email) {
        super(); // Gọi constructor trống của Entity (ID sẽ mặc định là 0)
        this.username = username;
        this.password = password;
        this.email = email;
        this.isActive = true; // Mặc định tài khoản mới là active
    }

    // ==========================================
    // CONSTRUCTOR 2: Dùng khi LẤY DỮ LIỆU TỪ DATABASE LÊN (Tầng DAO dùng)
    // Phải có ID chuẩn từ MySQL truyền vào!
    // ==========================================
    public User(int id, String username, String password, String email) {
        super(id); // Chuyền thẳng cái ID xịn này lên cho class cha Entity giữ
        this.username = username;
        this.password = password;
        this.email = email;
        this.isActive = true; 
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

    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // TRỪU TƯỢNG (Abstraction): Một phương thức rỗng, ép các lớp con phải tự viết nội dung
    public abstract void showDashboard();
    public void setUsername(String newUsername) {
        this.username = newUsername;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean active) {
        this.isActive = active;
    }
}