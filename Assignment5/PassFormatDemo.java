import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
public class PassFormatDemo {
    static String escape(String s) { return s.replace("%", "%25").replace("|", "%7C"); }
    static String unescape(String s) { return s.replace("%7C", "|").replace("%25", "%"); }
    static void add(Path path, String id, String reg, String name, int route, String stop, String valid) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join("|", escape(id), escape(reg), escape(name), Integer.toString(route), escape(stop), escape(valid)));
            w.newLine();
        }
    }
    static Optional<String[]> find(Path path, String id) throws IOException {
        if (!Files.exists(path)) return Optional.empty();
        try (BufferedReader r = Files.newBufferedReader(path)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = line.split("\\|", -1);
                if (f.length != 6) continue;
                try { Integer.parseInt(f[3]); } catch (NumberFormatException e) { continue; }
                if (unescape(f[0]).equals(id)) {
                    for (int i=0;i<f.length;i++) f[i]=unescape(f[i]);
                    return Optional.of(f);
                }
            }
        }
        return Optional.empty();
    }
    public static void main(String[] args) throws Exception {
        Path p=Paths.get("passes.txt"); Files.deleteIfExists(p);
        add(p,"P1001","192311001","Arun",12,"Main Gate","2027-06-30");
        add(p,"P1002","192311003","Divya",7,"Central","2027-06-30");
        System.out.println("Find: "+Arrays.toString(find(p,"P1002").orElse(null)));
        System.out.println("All records:"); Files.lines(p).forEach(System.out::println);
        try (DataOutputStream out=new DataOutputStream(new FileOutputStream("passes.bin"))) {
            out.writeUTF("P1001"); out.writeUTF("192311001"); out.writeUTF("Arun");
            out.writeInt(12); out.writeUTF("Main Gate"); out.writeUTF("2027-06-30");
        }
        try (DataInputStream in=new DataInputStream(new FileInputStream("passes.bin"))) {
            System.out.println("Binary ID: "+in.readUTF());
        }
    }
}
