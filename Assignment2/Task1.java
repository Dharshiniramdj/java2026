import java.util.Locale;
public class Task1 {

    static String buildWithString(int tickets, int fields) {
        String result = "";
        for (int t = 1; t <= tickets; t++) {
            for (int f = 1; f <= fields; f++) {
                result = result + "T" + t + "-F" + f + " ";
            }
            result = result + "\n";
        }
        return result;
    }

    static String buildWithStringBuilder(int tickets, int fields) {
        StringBuilder result = new StringBuilder();
        for (int t = 1; t <= tickets; t++) {
            for (int f = 1; f <= fields; f++) {
                result.append("T").append(t).append("-F").append(f).append(' ');
            }
            result.append('\n');
        }
        return result.toString();
    }

    static String buildWithStringBuffer(int tickets, int fields) {
        StringBuffer result = new StringBuffer();
        for (int t = 1; t <= tickets; t++) {
            for (int f = 1; f <= fields; f++) {
                result.append("T").append(t).append("-F").append(f).append(' ');
            }
            result.append('\n');
        }
        return result.toString();
    }

    static void measure(String label, Runnable task) {
        long start = System.nanoTime();
        task.run();
        long elapsed = System.nanoTime() - start;
        System.out.printf(Locale.US, "%-18s %.3f ms%n", label, elapsed / 1_000_000.0);
    }

    public static void main(String[] args) {
        int tickets = args.length > 0 ? Integer.parseInt(args[0]) : 10000;
        int fields = args.length > 1 ? Integer.parseInt(args[1]) : 50;

        // A small correctness check.
        String a = buildWithString(2, 3);
        String b = buildWithStringBuilder(2, 3);
        String c = buildWithStringBuffer(2, 3);
        System.out.println("All approaches produce same output: "
                + (a.equals(b) && b.equals(c)));

        System.out.println("\nWorkload: " + tickets + " tickets, " + fields + " fields each");
        // Keep large String-concatenation workloads moderate: it is intentionally costly.
        if ((long) tickets * fields > 30000) {
            System.out.println("For safety, String concatenation benchmark is limited to 500 tickets.");
            final int safeTickets = 500;
            measure("String (+)", () -> buildWithString(safeTickets, fields));
        } else {
            measure("String (+)", () -> buildWithString(tickets, fields));
        }
        measure("StringBuilder", () -> buildWithStringBuilder(tickets, fields));
        measure("StringBuffer", () -> buildWithStringBuffer(tickets, fields));

        System.out.println("\nRecommendation: StringBuilder for a local ticket being built by one thread.");
        System.out.println("Use StringBuffer only when the same mutable buffer must be synchronized across threads.");
    }
}

