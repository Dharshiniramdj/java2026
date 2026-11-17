import java.nio.file.*;
import java.util.*;
public class AdversarialInputDemo {
    static boolean valid(String line){
        String[] f=line.split(",",-1);
        if(f.length!=5||f[0].isBlank()||f[1].isBlank()||f[2].isBlank())return false;
        try{return Integer.parseInt(f[3])>0 && Double.parseDouble(f[4])>=0;}
        catch(NumberFormatException e){return false;}
    }
    public static void main(String[] args)throws Exception{
        String crafted="' OR '1'='1";
        System.out.println("Crafted test input: "+crafted);
        System.out.println("Unsafe query example: SELECT * FROM users WHERE id='"+crafted+"' AND pwd='x'");
        System.out.println("Safe query template: SELECT * FROM users WHERE id=? AND pwd=?");
        for(String row:new String[]{"P1,R1,Arun,12,3500","P2,broken","P3,R3,Divya,7,abc",""})
            System.out.println("Row valid="+valid(row)+" : "+row);
        System.out.println("For real JDBC results, run the prepared/unsafe versions on a disposable local test DB.");
    }
}
