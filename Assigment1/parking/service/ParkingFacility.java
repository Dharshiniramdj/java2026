package parking.service;
import parking.model.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ParkingFacility {
    private final Vehicle[][] slots;                       // jagged: floors differ in size
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private final Map<String, VisitorPass> passes = new HashMap<>();
    private double collection;

    public ParkingFacility(int... slotsPerFloor) {
        if (slotsPerFloor.length == 0) throw new IllegalArgumentException("need a floor");
        slots = new Vehicle[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] <= 0) throw new IllegalArgumentException("slots must be > 0");
            slots[f] = new Vehicle[slotsPerFloor[f]];
        }
    }

    /** First-free allocation. Returns null if full; throws if duplicate/invalid. */
    public ParkingTicket allocate(Vehicle v, LocalDateTime entry) {
        if (v == null || entry == null) throw new IllegalArgumentException("null argument");
        if (active.containsKey(v.getRegNo()))
            throw new IllegalStateException("Already parked: " + v.getRegNo());
        for (int f = 0; f < slots.length; f++)
            for (int s = 0; s < slots[f].length; s++)
                if (slots[f][s] == null) {
                    slots[f][s] = v;
                    ParkingTicket t = new ParkingTicket(v, f, s, entry);
                    active.put(v.getRegNo(), t);
                    return t;
                }
        return null;
    }

    /** Returns fee, or -1 if floor/slot invalid or empty. */
    public double release(int floor, int slot, LocalDateTime exit) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length) return -1;
        Vehicle v = slots[floor][slot];
        if (v == null) return -1;
        ParkingTicket t = active.get(v.getRegNo());
        long mins = Math.max(0, Duration.between(t.getEntryTime(), exit).toMinutes());
        double fee = v.calculateFee((int) mins);
        slots[floor][slot] = null;
        active.remove(v.getRegNo());
        collection += fee;
        return fee;
    }

    public VisitorPass issuePass(String passNo, LocalDate date, double fee) {
        VisitorPass p = new VisitorPass(passNo, date, fee);
        passes.put(passNo, p);
        collection += fee;
        return p;
    }
    public boolean isPassValid(String passNo, LocalDate d) {
        VisitorPass p = passes.get(passNo);
        return p != null && p.isValidOn(d);
    }

    public boolean isOccupied(int f, int s) { return slots[f][s] != null; }
    public double getCollection() { return collection; }
    public int activeCount() { return active.size(); }

    public String occupancySnapshot() {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < slots.length; f++) {
            sb.append("Floor ").append(f).append(": ");
            for (Vehicle v : slots[f]) sb.append(v == null ? "[FREE] " : "[" + v.getRegNo() + "] ");
            sb.append('\n');
        }
        return sb.toString();
    }
}
