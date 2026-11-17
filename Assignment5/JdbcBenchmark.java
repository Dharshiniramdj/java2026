import java.sql.*;
import java.util.*;
public class JdbcBenchmark {
    static final String URL="jdbc:mysql://localhost:3306/java_assignment5";
    static final String USER="root";
    static final String PASSWORD=System.getenv().getOrDefault("A5_DB_PASSWORD", "");
    static List<String[]> records(int n){
        List<String[]> rows=new ArrayList<>();
        for(int i=0;i<n;i++)rows.add(new String[]{"P"+i,"R"+i,"Student"+i,"7","3800"});
        return rows;
    }
    static void clear(Connection c)throws SQLException{
        try(Statement s=c.createStatement()){s.executeUpdate("DELETE FROM bus_pass");}
    }
    static int insert(Connection c,List<String[]> rows,int mode)throws SQLException{
        if(mode==2)return PassLoader.loadAll(c,rows);
        int n=0;
        if(mode==0){
            try(Statement s=c.createStatement()){
                for(String[] r:rows){
                    String sql="INSERT INTO bus_pass VALUES ('"+r[0]+"','"+r[1]+"','"+r[2]+"',"+r[3]+","+r[4]+")";
                    n+=s.executeUpdate(sql);
                }
            }
        }else{
            try(PreparedStatement p=c.prepareStatement("INSERT INTO bus_pass VALUES (?,?,?,?,?)")){
                for(String[] r:rows){
                    p.setString(1,r[0]);p.setString(2,r[1]);p.setString(3,r[2]);
                    p.setInt(4,Integer.parseInt(r[3]));p.setDouble(5,Double.parseDouble(r[4]));
                    n+=p.executeUpdate();
                }
            }
        }
        return n;
    }
    public static void main(String[] args)throws Exception{
        try(Connection c=DriverManager.getConnection(URL,USER,PASSWORD)){
            DatabaseMetaData meta=c.getMetaData();
            System.out.println(meta.getDatabaseProductName()+" driver "+meta.getDriverVersion());
            for(int size:new int[]{10,1000,10000,100000}){
                List<String[]> rows=records(size);
                for(int mode=0;mode<3;mode++){
                    clear(c);long start=System.nanoTime();int count=insert(c,rows,mode);
                    System.out.printf("size=%d mode=%d inserted=%d ms=%.2f%n",size,mode,count,(System.nanoTime()-start)/1e6);
                }
            }
        }
    }
}
