import java.io.*;
import java.nio.file.*;
public class BenchmarkFiles {
    static long count(Path p,boolean buffered)throws IOException{
        long lines=0;int b,last=-1;boolean any=false;
        try(InputStream in=buffered?new BufferedInputStream(Files.newInputStream(p)):Files.newInputStream(p)){
            while((b=in.read())!=-1){any=true;if(b=='\n')lines++;last=b;}
        }
        return lines+(any&&last!='\n'?1:0);
    }
    public static void main(String[] args)throws Exception{
        for(int mb:new int[]{1,10,50}){
            Path p=Files.createTempFile("bench", ".txt");
            byte[] block=new byte[8192]; java.util.Arrays.fill(block,(byte)'A');block[8191]='\n';
            try(OutputStream out=Files.newOutputStream(p)){
                long remaining=(long)mb*1024*1024;
                while(remaining>0){int n=(int)Math.min(block.length,remaining);out.write(block,0,n);remaining-=n;}
            }
            for(boolean buffered:new boolean[]{false,true}){
                long t=System.nanoTime();long lines=count(p,buffered);
                System.out.printf("%d MiB buffered=%s lines=%d time=%.2f ms%n",mb,buffered,lines,(System.nanoTime()-t)/1e6);
            }
            Files.deleteIfExists(p);
        }
    }
}
