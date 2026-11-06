package parking.model;
public class DeliveryVan extends Vehicle {
    public DeliveryVan(String regNo) { super(regNo); }
    @Override public double ratePerHour() { return 50.0; }
}
