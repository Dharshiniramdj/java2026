public class Task03 {
    static String[][] slot = { new String[2], new String[3] };

    static int[] allocate(String reg) {
        if (reg == null) return null;
        for (int f = 0; f < slot.length; f++)
            for (int s = 0; s < slot[f].length; s++)
                if (slot[f][s] == null) { slot[f][s] = reg; return new int[]{f, s}; } // stop at first free
        return null;                                                                 // NO_SPACE
    }

    static boolean release(int f, int s) {
        if (f < 0 || f >= slot.length || s < 0 || s >= slot[f].length) return false; // out of range
        if (slot[f][s] == null) return false;                                        // empty
        slot[f][s] = null;
        return true;
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 6; i++) {
            int[] r = allocate("V" + i);
            System.out.println("allocate V" + i + " -> " + (r == null ? "NO_SPACE (null)" : "floor " + r[0] + " slot " + r[1]));
        }
        System.out.println("release(0,1)  -> " + release(0, 1));
        int[] r = allocate("V7");
        System.out.println("allocate V7   -> floor " + r[0] + " slot " + r[1] + "  (first free reused)");
        System.out.println("release(0,1) again -> " + release(0, 1) + " then release(0,1) -> " + release(0, 1));
        System.out.println("release(9,9)  -> " + release(9, 9) + " (out of range)");
    }
}
