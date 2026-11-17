import java.util.*;
interface Dao<T,K> {
    void insert(T value);
    Optional<T> findById(K key);
    List<T> findAll();
    void update(T value);
    void delete(K key);
}
class Student {
    final int id; final String name; final String course;
    Student(int id,String name,String course){this.id=id;this.name=name;this.course=course;}
    public String toString(){return id+" "+name+" "+course;}
}
class StudentDao implements Dao<Student,Integer> {
    private final Map<Integer,Student> store=new HashMap<>();
    public void insert(Student s){if(store.putIfAbsent(s.id,s)!=null)throw new IllegalArgumentException("Duplicate ID");}
    public Optional<Student> findById(Integer id){return Optional.ofNullable(store.get(id));}
    public List<Student> findAll(){return new ArrayList<>(store.values());}
    public void update(Student s){if(!store.containsKey(s.id))throw new NoSuchElementException();store.put(s.id,s);}
    public void delete(Integer id){store.remove(id);}
}
public class DaoDemo {
    public static void main(String[] args){
        Dao<Student,Integer> dao=new StudentDao();
        dao.insert(new Student(101,"Arun","AI&DS"));
        System.out.println(dao.findById(101).orElse(null));
        System.out.println(dao.findAll());
    }
}
