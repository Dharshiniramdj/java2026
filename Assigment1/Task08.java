import java.time.LocalTime;

/** Task 8: polymorphism - dynamic dispatch and overloading. */
public class Task08 {
    interface Chargeable { double chargingFee(int kWh); }
    static abstract class Vehicle {
        abstract double ratePerHour();
        double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
        // Overload: compile-time polymorphism, chosen by argument list
        double calculateFee(int minutes, LocalTime entry) {
            double f = calculateFee(minutes);
            boolean peak = !entry.isBefore(LocalTime.of(8, 0)) && entry.isBefore(LocalTime.of(10, 0));
            return peak ? f * 1.2 : f;
        }
        public String toString() { return getClass().getSimpleName(); }
    }
    static class TwoWheeler extends Vehicle { double ratePerHour() { return 10; } }
    static class Car extends Vehicle { double ratePerHour() { return 30; } }
    static class ElectricCar extends Car implements Chargeable {
        double ratePerHour() { return 25; }
        public double chargingFee(int kWh) { return kWh * 12; }
    }

    public static void main(String[] args) {
        Vehicle[] vs = { new TwoWheeler(), new Car(), new ElectricCar() };
        for (Vehicle v : vs) {                                  // runtime type decides the method
            System.out.println(v + " 60 min = Rs " + v.calculateFee(60));
            if (v instanceof Chargeable c) System.out.println("   charging 10 kWh = Rs " + c.chargingFee(10));
        }
        System.out.println("Overload, peak 09:00: Car 60 min = Rs " + vs[1].calculateFee(60, LocalTime.of(9, 0)));
        System.out.println("Overload, off-peak 14:00: Car 60 min = Rs " + vs[1].calculateFee(60, LocalTime.of(14, 0)));
    }
}
