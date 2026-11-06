package parking.app;
import parking.model.*;
import parking.service.ParkingFacility;
import java.time.LocalDateTime;
public class TestRunner {
    public static void main(String[] args) {
        ParkingDemo.main(args);                                  // T1-T10 + polymorphism
        int n = 2000;
        ParkingFacility pf = new ParkingFacility(n);
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 26, 9, 0);
        long s = System.nanoTime();
        for (int i = 0; i < n; i++) pf.allocate(new Car("V" + i), t0);
        double allocMs = (System.nanoTime() - s) / 1e6;
        s = System.nanoTime();
        for (int i = 0; i < n; i++) pf.release(0, i, t0.plusMinutes(30));
        double relMs = (System.nanoTime() - s) / 1e6;
        System.out.println("\nMeasured on this machine (varies per run):");
        System.out.println(n + " allocations (O(S) scan each): " + allocMs + " ms");
        System.out.println(n + " releases (O(1) each):         " + relMs + " ms");
    }
}
