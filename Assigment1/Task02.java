import java.util.*;
public class Task02 {
    public static void main(String[] args) {
        String[][] slots = new String[3][];          // jagged: floors differ in size
        slots[0] = new String[2]; slots[1] = new String[4]; slots[2] = new String[1];
        int total = 0;
        for (String[] floor : slots) total += floor.length;
        System.out.println("Floors=" + slots.length + " totalSlots=" + total
                + " slotsPerFloor=" + slots[0].length + "," + slots[1].length + "," + slots[2].length);

        slots[1][3] = "KA05XY9";                     // O(1) indexed access
        System.out.println("slots[1][3] = " + slots[1][3]);

        Map<String, String> activeTickets = new HashMap<>();   // O(1) average lookup
        activeTickets.put("KA05XY9", "T1001");
        System.out.println("Ticket for KA05XY9: " + activeTickets.get("KA05XY9"));

        int minutes = 61;
        System.out.println("\nWRONG   minutes/60            = " + minutes / 60 + " hour(s)");
        System.out.println("CORRECT ceiling hours         = " + (minutes / 60 + (minutes % 60 == 0 ? 0 : 1)) + " hour(s)");
        double fee = 37.5;
        System.out.println("WRONG   (int) cast of fee     = " + (int) fee);
        System.out.println("CORRECT keep as double        = " + fee);
    }
}
