public class Electronics extends Item {
    private final int warrantyMonths;
    public Electronics(String id, String name, String description, double startingPrice, int warrantyMonths) {
        super(id, name, description, startingPrice);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void printInfo() {
        System.out.println("Sản phẩm điện tử: " + getName() + " | Bảo hành: " + warrantyMonths + " tháng");
    }
}
