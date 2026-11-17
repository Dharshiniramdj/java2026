public class UnsafeBooking {
    private final boolean[] booked = new boolean[10];
    private int bookingsMade = 0;
    public void book(int seat) {
        if (!booked[seat]) {
            Thread.yield();
            booked[seat] = true;
            bookingsMade++;
        }
    }
    public static void main(String[] args) throws InterruptedException {
        UnsafeBooking screen = new UnsafeBooking();
        Thread[] users = new Thread[100];
        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10;
            users[i] = new Thread(() -> screen.book(seat)); users[i].start();
        }
        for (Thread t : users) t.join();
        System.out.println("Seats sold (unsafe): " + screen.bookingsMade);
    }
}
