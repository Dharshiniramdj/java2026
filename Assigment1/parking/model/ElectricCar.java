package parking.model;
public class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }
    @Override public double ratePerHour() { return 25.0; } // discounted parking rate
}
