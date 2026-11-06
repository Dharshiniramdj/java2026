/** Task 9: abstraction and interfaces. */
public class Task09 {
    interface Chargeable { double chargingFee(int kWh); }
    interface Washable { default String wash() { return "washed"; } }

    static abstract class Vehicle {                           // abstract: no single correct rate
        abstract double ratePerHour();
        public String toString() { return getClass().getSimpleName(); }
    }
    static class Car extends Vehicle { double ratePerHour() { return 30; } }
    // one superclass, several interfaces
    static class ElectricCar extends Car implements Chargeable, Washable {
        public double chargingFee(int kWh) { return kWh * 12; }
    }
    static class ElectricBike extends Vehicle implements Chargeable {
        double ratePerHour() { return 8; }
        public double chargingFee(int kWh) { return kWh * 12; }
    }

    public static void main(String[] args) {
        // new Vehicle();   <- would not compile: Vehicle is abstract
        Vehicle[] vs = { new Car(), new ElectricCar(), new ElectricBike() };
        for (Vehicle v : vs) {
            System.out.print(v + " rate Rs " + v.ratePerHour());
            if (v instanceof Chargeable c) System.out.print(" | charge 10 kWh Rs " + c.chargingFee(10));
            if (v instanceof Washable w) System.out.print(" | " + w.wash());
            System.out.println();
        }
    }
}
