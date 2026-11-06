import java.lang.ref.WeakReference;

/** Task 5: static members and object life cycle. */
public class Task5 {
    static class Ticket {
        private static int nextNumber = 1001;          // shared by every Ticket
        private final int no;
        Ticket() { no = nextNumber++; }
        int getNo() { return no; }
        static int issued() { return nextNumber - 1001; }
    }

    public static void main(String[] args) throws Exception {
        Ticket a = new Ticket(), b = new Ticket(), c = new Ticket();
        System.out.println("Tickets: " + a.getNo() + ", " + b.getNo() + ", " + c.getNo());
        System.out.println("Issued so far (static method): " + Ticket.issued());

        Ticket t = new Ticket();
        WeakReference<Ticket> weak = new WeakReference<>(t);
        System.out.println("Before exit - still referenced: " + (weak.get() != null));
        t = null;                                      // ticket removed, no references remain
        System.gc(); Thread.sleep(100);
        System.out.println("After exit  - garbage collected: " + (weak.get() == null)
                + " (GC timing is not guaranteed)");
    }
}
