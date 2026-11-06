package parking.app;
import parking.model.*;
import parking.service.ParkingFacility;
import java.time.*;

public class ParkingDemo {
    static int pass = 0, fail = 0;
    static void check(String id, String what, boolean ok) {
        System.out.println(id + " " + (ok ? "PASS" : "FAIL") + " - " + what);
        if (ok) pass++; else fail++;
    }

    public static void main(String[] args) {
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 26, 9, 0);

        // T1
        ParkingFacility pf = new ParkingFacility(2, 3, 1, 2, 2);   // 5 floors, jagged, 10 slots
        ParkingTicket tk = pf.allocate(new Car("TN01AB1234"), t0);
        check("T1", "first free slot -> " + tk, tk.getFloor() == 0 && tk.getSlot() == 0);

        // T2
        ParkingFacility small = new ParkingFacility(1, 1);
        small.allocate(new Car("A1"), t0); small.allocate(new Car("A2"), t0);
        check("T2", "full facility returns null", small.allocate(new Car("A3"), t0) == null);

        // T3
        double fee = pf.release(0, 0, t0.plusMinutes(60));
        check("T3", "slot freed, fee=" + fee, !pf.isOccupied(0, 0) && fee == 30.0);

        // T4
        double before = pf.getCollection();
        boolean ok = pf.release(0, 0, t0) == -1 && pf.release(9, 0, t0) == -1
                && pf.release(0, 99, t0) == -1 && pf.getCollection() == before;
        check("T4", "empty/invalid release returns -1, no state change", ok);

        // T5
        check("T5", "61 min car = 2 units = Rs " + new Car("X1").calculateFee(61),
                new Car("X1").calculateFee(61) == 60.0);

        // T6
        pf.allocate(new TwoWheeler("KA05XY9"), t0);
        boolean rejected = false;
        try { pf.allocate(new TwoWheeler("KA05XY9"), t0); } catch (IllegalStateException e) { rejected = true; }
        check("T6", "duplicate active registration rejected", rejected);

        // T7
        check("T7", "equals() on same regNo", new Car("TN09ZZ1").equals(new ElectricCar("tn09zz1")));

        // T8
        double c = new ElectricCar("EV1").chargingFee(10);
        check("T8", "ElectricCar 10 kWh = Rs " + c, c == 120.0);

        // T9 / T10
        LocalDate d = LocalDate.of(2026, 10, 26);
        pf.issuePass("VP1", d, 100);
        check("T9", "pass valid on issue date", pf.isPassValid("VP1", d));
        check("T10", "pass invalid next day", !pf.isPassValid("VP1", d.plusDays(1)));

        System.out.println("\n-- Polymorphism: fee for 90 minutes --");
        Vehicle[] vs = { new TwoWheeler("P1"), new Car("P2"), new ElectricCar("P3"),
                new ElectricBike("P4"), new DeliveryVan("P5") };
        for (Vehicle v : vs)
            System.out.println(v + " Rs " + v.calculateFee(90) + (v instanceof Chargeable ? " (chargeable)" : ""));

        System.out.println("\n" + pf.occupancySnapshot());
        System.out.println("Collection: Rs " + pf.getCollection());
        System.out.println("Passed " + pass + " / " + (pass + fail));
    }
}
