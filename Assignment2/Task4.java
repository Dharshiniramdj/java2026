import java.util.*;

public class Task4 {

    public static void main(String[] args) {

        HashMap<Integer, String> hashMap =
                new HashMap<>();

        TreeMap<Integer, String> treeMap =
                new TreeMap<>();

        LinkedHashMap<Integer, String> linkedMap =
                new LinkedHashMap<>();

        // Insert medicine details
        hashMap.put(103, "Paracetamol");
        hashMap.put(101, "Dolo");
        hashMap.put(102, "Cetirizine");

        treeMap.putAll(hashMap);
        linkedMap.put(103, "Paracetamol");
        linkedMap.put(101, "Dolo");
        linkedMap.put(102, "Cetirizine");

        System.out.println("HashMap:");
        System.out.println(hashMap);

        System.out.println("\nTreeMap:");
        System.out.println(treeMap);

        System.out.println("\nLinkedHashMap:");
        System.out.println(linkedMap);

        // Searching
        System.out.println("\nMedicine with code 101:");

        System.out.println(hashMap.get(101));

        // Removing
        hashMap.remove(102);

        System.out.println("\nAfter Removing Medicine:");
        System.out.println(hashMap);
    }
}