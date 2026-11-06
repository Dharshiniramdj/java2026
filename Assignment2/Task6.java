import java.util.*;

class Patient {
    String name;
    int severity;
    int arrivalTime;
    Patient(String name, int severity, int arrivalTime) {
        this.name = name;
        this.severity = severity;
        this.arrivalTime = arrivalTime;
    }
    public String toString() {
        return name + " (Severity: " + severity + ")";
    }
}
public class Task6 {

    public static void main(String[] args) {

        PriorityQueue<Patient> queue =
                new PriorityQueue<>(
                        Comparator.comparingInt(
                                (Patient p) -> p.severity
                        ).thenComparingInt(
                                p -> p.arrivalTime
                        )
                );

        queue.add(new Patient("Ravi", 3, 1));
        queue.add(new Patient("Meena", 1, 2));
        queue.add(new Patient("John", 3, 3));
        queue.add(new Patient("Asha", 5, 4));
        queue.add(new Patient("Kumar", 1, 5));
        queue.add(new Patient("Lata", 2, 6));

        System.out.println("Patient Treatment Order:");

        while (!queue.isEmpty()) {

            Patient p = queue.poll();

            System.out.println(p);
        }
    }
}