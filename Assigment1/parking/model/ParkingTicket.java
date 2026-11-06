package parking.model;
import java.time.LocalDateTime;

public class ParkingTicket {
    private static int nextNumber = 1001;       // shared by all tickets
    private final String ticketNo;
    private final Vehicle vehicle;
    private final int floor, slot;
    private final LocalDateTime entryTime;

    public ParkingTicket(Vehicle vehicle, int floor, int slot, LocalDateTime entryTime) {
        if (vehicle == null || entryTime == null) throw new IllegalArgumentException("null argument");
        this.ticketNo = "T" + (nextNumber++);
        this.vehicle = vehicle; this.floor = floor; this.slot = slot; this.entryTime = entryTime;
    }
    public String getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    @Override public String toString() {
        return ticketNo + " " + vehicle + " floor " + floor + " slot " + slot;
    }
}
