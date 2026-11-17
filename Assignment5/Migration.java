import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class Migration {
    static final String URL="jdbc:mysql://localhost:3306/java_assignment5";
    static final String USER="root";
    static final String PASSWORD=System.getenv().getOrDefault("A5_DB_PASSWORD", "");
    public static void main(String[] args)throws Exception{
        Path input=Paths.get(args.length>0?args[0]:"passes.csv");
        if(!Files.exists(input)){
            Files.writeString(input,"P1,R1,Arun,12,3500\nBAD,LINE\nP2,R2,Divya,7,3800\n");
        }
        List<String[]> valid=new ArrayList<>();List<String> rejected=new ArrayList<>();
        int lineNo=0;
        for(String line:Files.readAllLines(input)){
            lineNo++;String[] f=line.split(",",-1);
            if(f.length!=5){rejected.add("line "+lineNo+": field count");continue;}
            try{
                if(f[0].isBlank()||f[1].isBlank()||f[2].isBlank()||Integer.parseInt(f[3])<=0||Double.parseDouble(f[4])<0)
                    throw new IllegalArgumentException("invalid value");
                valid.add(f);
            }catch(RuntimeException e){rejected.add("line "+lineNo+": "+e.getMessage());}
        }
        System.out.println("Rejected: "+rejected);
        try(Connection c=DriverManager.getConnection(URL,USER,PASSWORD)){
            int loaded=PassLoader.loadAll(c,valid);
            System.out.println("Loaded: "+loaded);
            DatabaseMetaData m=c.getMetaData();
            System.out.println("DB: "+m.getDatabaseProductName()+" "+m.getDatabaseProductVersion());
            System.out.println("Driver: "+m.getDriverName()+" "+m.getDriverVersion());
            try(ResultSet r=m.getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})){
                while(r.next())System.out.println("Table: "+r.getString("TABLE_NAME"));
            }
        }
    }
}
