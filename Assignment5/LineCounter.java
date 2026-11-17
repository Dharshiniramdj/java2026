import java.io.*;
import java.nio.file.*;
public class LineCounter {
    static long count(Path p, boolean buffered) throws IOException {
        long lines=0; int last=-1; boolean any=false;
        try (InputStream in=buffered ? new BufferedInputStream(Files.newInputStream(p),8192) : Files.newInputStream(p)) {
            int c; while ((c=in.read())!=-1) { any=true; if(c=='\n') lines++; last=c; }
        }
        return lines+(any && last!='\n' ? 1 : 0);
    }
    public static void main(String[] args) throws Exception {
        Path p=Paths.get("attendance.log");
        if(!Files.exists(p)) Files.writeString(p,"present\nabsent\npresent\n");
        long a=System.nanoTime(); long x=count(p,false); long b=System.nanoTime();
        long y=count(p,true); long c=System.nanoTime();
        System.out.println("Unbuffered lines="+x+" ms="+(b-a)/1e6);
        System.out.println("Buffered lines="+y+" ms="+(c-b)/1e6);
    }
}
