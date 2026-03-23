public class Bidder extends User {

    private final double accountBalance;

    public Bidder(String id, String username, String password, String email, double accountBalance) {
        super(id, username, password, email);
        this.accountBalance = accountBalance;
    }

    @Override
    public void showDashboard() {
        System.out.println("Hiển thị giao diện mua hàng cho Bidder: " + getUsername());
        System.out.println("Số dư hiện tại: $" + accountBalance);
    }

    public void placeBid(double amount) {
        if (amount <= accountBalance) {
            System.out.println(getUsername() + " vừa đặt giá: $" + amount);
        } else {
            System.out.println("Số dư không đủ để đặt mức giá này!");
        }
    }
}