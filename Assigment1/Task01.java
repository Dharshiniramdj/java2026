
import java.time.Duration;
import java.time.LocalDateTime;

public class Task01 {
    public static void main(String[] args) {
        String regNo = "TN01AB1234";
        String type = "CAR";
        LocalDateTime entry = LocalDateTime.of(2026, 10, 26, 9, 0);
        LocalDateTime exit = entry.plusMinutes(61L);
        long minutes = Duration.between(entry, exit).toMinutes();
        if (minutes < 0L) {
            throw new IllegalArgumentException("Duration cannot be negative");
        } else {
            long hours = (minutes + 59L) / 60L;
            double var10000;
            switch (type) {
                case "BIKE" -> var10000 = (double)10.0F;
                case "CAR" -> var10000 = (double)30.0F;
                default -> var10000 = (double)50.0F;
            }

            double rate = var10000;
            System.out.println("INPUT : reg=" + regNo + " type=" + type + " entry=" + String.valueOf(entry) + " exit=" + String.valueOf(exit));
            System.out.println("OUTPUT: floor=0 slot=0 ticket=T1001 minutes=" + minutes + " billedHours=" + hours + " fee=Rs " + (double)hours * rate);
            System.out.println("\nCandidate classes : Vehicle, TwoWheeler, Car, ElectricCar, ElectricBike,");
            System.out.println("                    DeliveryVan, ParkingTicket, VisitorPass, ParkingFacility, Chargeable");
            System.out.println("Candidate methods : allocate, release, calculateFee, chargingFee, isValidOn,");
            System.out.println("                    getRegNo, occupancySnapshot");
        }
    }
}
