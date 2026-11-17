class ScanTask implements Runnable {
    private final int station;
    ScanTask(int station) { this.station = station; }
    public void run() {
        try {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Station " + station + " scanned script " + i);
                Thread.sleep(100);
            }
            System.out.println("Station " + station + " finished");
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
public class RunnableScanner {
    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[4];
        for (int i = 0; i < 4; i++) { threads[i] = new Thread(new ScanTask(i + 1)); threads[i].start(); }
        for (Thread t : threads) t.join();
        System.out.println("Total scripts: 20");
    }
}
