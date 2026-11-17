import java.io.*;
public class ManualReport {
    public static void main(String[] args) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("manual_report.txt"));
            int total = 0;
            for (int i = 1; i <= 3; i++) {
                String file = "store" + i + ".txt";
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) total += Integer.parseInt(line);
                reader.close(); reader = null;
            }
            writer.write("Total Sales: " + total);
            System.out.println("Report generated");
        } catch (IOException | NumberFormatException e) { System.out.println("Error: " + e.getMessage()); }
        finally {
            try { if (reader != null) reader.close(); } catch (IOException e) { System.err.println("Reader close failed: " + e); }
            try { if (writer != null) writer.close(); } catch (IOException e) { System.err.println("Writer close failed: " + e); }
        }
        System.out.println("Report generated");
    }
}
