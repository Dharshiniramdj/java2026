package parking.model;
public interface Chargeable {
    double CHARGE_RATE_PER_KWH = 12.0;
    default double chargingFee(int kWh) {
        if (kWh < 0) throw new IllegalArgumentException("kWh cannot be negative");
        return kWh * CHARGE_RATE_PER_KWH;
    }
}
