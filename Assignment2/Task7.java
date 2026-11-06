public class Task7 {

    public static void main(String[] args) {

        int tickets = 1000;
        int fields = 50;

        // Original approach using String
        long start = System.nanoTime();

        String result = "";

        for (int i = 1; i <= tickets; i++) {

            for (int j = 1; j <= fields; j++) {

                result = result + "Ticket "
                        + i + " Field " + j + "\n";
            }
        }

        long end = System.nanoTime();

        long stringTime = end - start;

        System.out.println("String Output Length: "
                + result.length());

        System.out.println("String Execution Time: "
                + stringTime + " ns");


        // Optimized approach using StringBuilder
        start = System.nanoTime();

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= tickets; i++) {

            for (int j = 1; j <= fields; j++) {

                sb.append("Ticket ")
                        .append(i)
                        .append(" Field ")
                        .append(j)
                        .append("\n");
            }
        }

        end = System.nanoTime();

        long builderTime = end - start;

        System.out.println("\nStringBuilder Output Length: "
                + sb.length());

        System.out.println("StringBuilder Execution Time: "
                + builderTime + " ns");

        System.out.println("\nPerformance Comparison:");

        System.out.println("String: "
                + stringTime + " ns");

        System.out.println("StringBuilder: "
                + builderTime + " ns");

        System.out.println("\nOptimization completed.");
    }
}