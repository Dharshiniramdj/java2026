import java.time.LocalDateTime;

/** Task 4: classes, objects, constructors, validation. */
public class Task04 {
    static class Vehicle {
        private final String regNo;
        protected Vehicle(String regNo) {
            if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
            this.regNo = regNo.trim().toUpperCase();
        }
        String getRegNo() { return regNo; }
    }
    static class Car extends Vehicle { Car(String regNo) { super(regNo); } }

    static class ParkingTicket {
        private static int next = 1001;
        private final int no; private final Vehicle vehicle; private final int floor, slot;
        private final LocalDateTime entry;
        ParkingTicket(Vehicle v, int floor, int slot, LocalDateTime entry) {
            if (v == null || entry == null) throw new IllegalArgumentException("null");
            this.no = next++; this.vehicle = v; this.floor = floor; this.slot = slot; this.entry = entry;
        }
        public String toString() { return "T" + no + " " + vehicle.getRegNo() + " f" + floor + "s" + slot + " @" + entry; }
    }

    public static void main(String[] args) {
        Vehicle v = new Car(" tn01ab1234 ");               // reference on stack, object on heap
        ParkingTicket t1 = new ParkingTicket(v, 0, 0, LocalDateTime.of(2026, 10, 26, 9, 0));
        System.out.println(t1);
        try { new Car(""); } catch (IllegalArgumentException e) { System.out.println("Rejected: " + e.getMessage()); }
        // No copy constructor: a copy of t1 would reuse its ticket number. A new ticket always gets a new one.
        ParkingTicket t2 = new ParkingTicket(v, 0, 1, LocalDateTime.of(2026, 10, 26, 9, 5));
        System.out.println(t2);
    }
}
