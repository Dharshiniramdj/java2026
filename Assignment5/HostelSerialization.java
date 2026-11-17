import java.io.*;
import java.util.*;
class Resident implements Serializable {
    private static final long serialVersionUID=1L;
    String name, registerNo, phone;
    transient String password;
    Resident(String n,String r,String p,String phone){name=n;registerNo=r;password=p;this.phone=phone;}
    public String toString(){return name+" "+registerNo+" phone="+phone+" password="+password;}
}
class Room implements Serializable {
    private static final long serialVersionUID=1L;
    int number; List<Resident> residents=new ArrayList<>();
    Room(int n){number=n;}
}
class Hostel implements Serializable {
    private static final long serialVersionUID=1L;
    List<Room> rooms=new ArrayList<>();
    transient java.sql.Connection connection;
}
public class HostelSerialization {
    public static void main(String[] args) throws Exception {
        Hostel hostel=new Hostel(); Room r=new Room(101);
        r.residents.add(new Resident("Divya","192311003","secret","9876543210"));
        hostel.rooms.add(r);
        try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream("hostel.ser"))){out.writeObject(hostel);}
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream("hostel.ser"))){
            Hostel restored=(Hostel)in.readObject();
            System.out.println(restored.rooms.get(0).residents.get(0));
        }
    }
}
