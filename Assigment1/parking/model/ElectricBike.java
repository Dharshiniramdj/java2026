package parking.model;
public class ElectricBike extends Vehicle implements Chargeable {
    public ElectricBike(String regNo) { super(regNo); }
    @Override public double ratePerHour() { return 8.0; }
}
