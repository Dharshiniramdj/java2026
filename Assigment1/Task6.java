/** Task 6: inheritance hierarchy and constructor order. */
public class Task6 {
    interface Chargeable { }
    static abstract class Vehicle {
        Vehicle(String r) { System.out.println("  Vehicle constructor (" + r + ")"); }
        abstract double ratePerHour();
    }
    static class TwoWheeler extends Vehicle { TwoWheeler(String r) { super(r); System.out.println("  TwoWheeler constructor"); } double ratePerHour() { return 10; } }
    static class Car extends Vehicle { Car(String r) { super(r); System.out.println("  Car constructor"); } double ratePerHour() { return 30; } }
    static class ElectricCar extends Car implements Chargeable { ElectricCar(String r) { super(r); System.out.println("  ElectricCar constructor"); } double ratePerHour() { return 25; } }
    static class ElectricBike extends Vehicle implements Chargeable { ElectricBike(String r) { super(r); System.out.println("  ElectricBike constructor"); } double ratePerHour() { return 8; } }
    static class DeliveryVan extends Vehicle { DeliveryVan(String r) { super(r); System.out.println("  DeliveryVan constructor"); } double ratePerHour() { return 50; } }

    public static void main(String[] args) {
        System.out.println("new ElectricCar:");
        Vehicle ev = new ElectricCar("EV1");
        System.out.println("new DeliveryVan:");
        Vehicle van = new DeliveryVan("DV1");
        new TwoWheeler("TW1"); new ElectricBike("EB1");
        System.out.println("\nElectricCar is a Car? " + (ev instanceof Car)
                + " | Chargeable? " + (ev instanceof Chargeable)
                + " | DeliveryVan is a Car? " + (van instanceof Car));
    }
}
