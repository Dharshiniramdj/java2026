
package parking.app;
import parking.model.Vehicle;
import parking.service.ParkingLot;
public class Main {
    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot();
        Vehicle v = new Vehicle("TN01AB1234");
        System.out.println("Parked: " + lot.park(v));
        System.out.println("Reg no: " + v.getRegNo());
        System.out.println("Free slots: " + lot.freeSlots());
        try {
            new Vehicle("");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
        Integer a = 1000, b = 1000;
        System.out.println("a == b      : " + (a == b));
        System.out.println("a.equals(b) : " + a.equals(b));
    }
}
