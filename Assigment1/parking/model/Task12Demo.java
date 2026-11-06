package parking.app;
import parking.model.*;
import parking.service.ParkingFacility;
import java.time.LocalDateTime;

/** Task 12: adapted system - 5 uneven floors, new vehicle types, visitor pass, PricingRule alternative. */
public class Task12Demo {
    interface PricingRule { double fee(Vehicle v, int minutes); }   // composition-based alternative

    public static void main(String[] args) {
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 26, 9, 0);
        ParkingFacility pf = new ParkingFacility(3, 5, 2, 4, 1);    // five floors, different capacities
        Vehicle[] fleet = { new Car("C1"), new ElectricBike("EB1"), new DeliveryVan("DV1"),
                new ElectricCar("EC1"), new TwoWheeler("TW1") };
        for (Vehicle v : fleet) System.out.println(pf.allocate(v, t0));
        pf.issuePass("VP9", t0.toLocalDate(), 100);
        System.out.println("Pass VP9 valid today: " + pf.isPassValid("VP9", t0.toLocalDate())
                + ", tomorrow: " + pf.isPassValid("VP9", t0.toLocalDate().plusDays(1)));
        System.out.println("\n" + pf.occupancySnapshot());

        PricingRule flat = (v, m) -> v.calculateFee(m);
        PricingRule peak = (v, m) -> v.calculateFee(m) * 1.5;
        for (Vehicle v : fleet)
            System.out.println(v + " 90 min: flat Rs " + flat.fee(v, 90) + " | peak rule Rs " + peak.fee(v, 90));

        System.out.println("\nReleasing floor 0 slot 0 after 90 min: Rs " + pf.release(0, 0, t0.plusMinutes(90)));
        System.out.println("Collection: Rs " + pf.getCollection());
    }
}
