import java.io.*;
public class SafeReport {
    public static void main(String[] args) {
        int total = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("safe_report.txt"))) {
            for (int i = 1; i <= 3; i++) {
                String file = "store" + i + ".txt";
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) total += Integer.parseInt(line);
                } catch (IOException | NumberFormatException e) {
                    System.err.println("Failed file: " + file); throw e;
                }
            }
            writer.write("Total Sales: " + total);
            System.out.println("Report generated");
        } catch (IOException | NumberFormatException e) { System.out.println("Error: " + e.getMessage()); }
    }
}
