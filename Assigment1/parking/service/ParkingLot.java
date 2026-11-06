
package parking.service;

import parking.model.Vehicle;

public class ParkingLot {
    private Vehicle[] slots = new Vehicle[3];

    public boolean park(Vehicle v) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = v;
                return true;
            }
        }
        return false;
    }

    public int freeSlots() {
        int count = 0;
        for (Vehicle v : slots) {
            if (v == null) {
                count++;
            }
        }
        return count;
    }
}
