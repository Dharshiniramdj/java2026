import java.util.*;

/** Task 7: overriding, @Override, final, equals/hashCode. */
public class Task07 {
    static abstract class Vehicle {
        private final String regNo;
        Vehicle(String r) { regNo = r.trim().toUpperCase(); }
        final String getRegNo() { return regNo; }            // final: identity cannot be changed
        abstract double ratePerHour();
        double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
        @Override public boolean equals(Object o) { return o instanceof Vehicle v && regNo.equals(v.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }
    static class Car extends Vehicle { Car(String r) { super(r); } @Override double ratePerHour() { return 30; } }
    static class DeliveryVan extends Vehicle {
        DeliveryVan(String r) { super(r); }
        @Override double ratePerHour() { return 50; }
        @Override double calculateFee(int minutes) { return super.calculateFee(minutes) + 20; } // specialised: loading charge
    }

    public static void main(String[] args) {
        System.out.println("Car 61 min = Rs " + new Car("A1").calculateFee(61));
        System.out.println("Van 61 min = Rs " + new DeliveryVan("V1").calculateFee(61) + " (override adds Rs 20)");
        Vehicle x = new Car("tn09zz1"), y = new Car("TN09ZZ1");
        System.out.println("equals: " + x.equals(y) + ", same hashCode: " + (x.hashCode() == y.hashCode()));
        Set<Vehicle> set = new HashSet<>(List.of(x));
        System.out.println("HashSet contains equal object: " + set.contains(y));
        System.out.println("== on references: " + (x == y));
    }
}
