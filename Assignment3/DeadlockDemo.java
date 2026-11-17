import java.util.concurrent.CountDownLatch;
public class DeadlockDemo {
    static final Object lock1 = new Object(), lock2 = new Object();
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        Thread a = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread A holds Lock 1"); latch.countDown();
                try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                synchronized (lock2) { System.out.println("A completed"); }
            }
        });
        Thread b = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread B holds Lock 2"); latch.countDown();
                try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                synchronized (lock1) { System.out.println("B completed"); }
            }
        });
        a.setDaemon(true); b.setDaemon(true);
        a.start(); b.start(); latch.await();
        System.out.println("Workers are deadlocked; main exits");
    }
}
