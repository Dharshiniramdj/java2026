import java.util.*;

class Student {

    int id;
    String name;

    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof Student))
            return false;

        Student s = (Student) obj;

        return this.id == s.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " " + name;
    }
}

public class Task3 {

    public static void main(String[] args) {

        HashSet<Student> students = new HashSet<>();

        students.add(new Student(101, "Arun"));
        students.add(new Student(102, "Divya"));
        students.add(new Student(101, "Arun"));

        System.out.println("Student Attendance Records:");

        for (Student s : students) {
            System.out.println(s);
        }

        System.out.println("Total Unique Students: "
                + students.size());
    }
}