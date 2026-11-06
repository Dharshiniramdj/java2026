import java.util.*;public class Task2 {

    public static void main(String[] args) {

        ArrayList<String> arrayList = new ArrayList<>();

        LinkedList<String> linkedList = new LinkedList<>();

        ArrayDeque<String> arrayDeque = new ArrayDeque<>();

        // Adding songs
        arrayList.add("Song A");
        arrayList.add("Song B");
        arrayList.add("Song C");

        linkedList.add("Song A");
        linkedList.add("Song B");
        linkedList.add("Song C");

        arrayDeque.add("Song A");
        arrayDeque.add("Song B");
        arrayDeque.add("Song C");

        System.out.println("ArrayList: " + arrayList);
        System.out.println("LinkedList: " + linkedList);
        System.out.println("ArrayDeque: " + arrayDeque);

        // Insertion
        arrayList.add(1, "New Song");
        linkedList.add(1, "New Song");

        // Deletion
        arrayList.remove("Song B");
        linkedList.remove("Song B");

        // Deque operations
        arrayDeque.addFirst("First Song");
        arrayDeque.addLast("Last Song");

        arrayDeque.removeFirst();
        arrayDeque.removeLast();

        System.out.println("\nAfter Operations:");

        System.out.println("ArrayList: " + arrayList);
        System.out.println("LinkedList: " + linkedList);
        System.out.println("ArrayDeque: " + arrayDeque);
    }
}