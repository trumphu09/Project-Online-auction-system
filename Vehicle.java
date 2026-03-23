public class Vehicle extends Item {
    private final String brand;
    private final int mileage;
    private final String condition;

    public Vehicle(String id, String name, String description, double startingPrice,
                   String brand, int mileage, String condition) {
        super(id, name, description, startingPrice);
        this.brand = brand;
        this.mileage = mileage;
        this.condition = condition;
    }

    @Override
    public void printInfo() {
        System.out.println("--- Phương tiện di chuyển ---");
        System.out.println("Xe: " + brand + " " + getName());
        System.out.println("Tình trạng: " + condition + " | Số km đã đi: " + mileage + " km");
        System.out.println("Giá khởi điểm: $" + getStartingPrice());
    }
}
