class ScannerStation extends Thread {
    private final int station;
    ScannerStation(int station) { this.station = station; }
    public void run() {
        try {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Station " + station + " scanned script " + i);
                Thread.sleep(100);
            }
            System.out.println("Station " + station + " completed");
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
public class ThreadScanner {
    public static void main(String[] args) throws InterruptedException {
        ScannerStation[] stations = new ScannerStation[4];
        for (int i = 0; i < 4; i++) { stations[i] = new ScannerStation(i + 1); stations[i].start(); }
        for (ScannerStation station : stations) station.join();
        System.out.println("Total scripts scanned: 20");
        System.out.println("Station 1 alive: " + stations[0].isAlive());
    }
}
