package parking.model;

public abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank())
            throw new IllegalArgumentException("Registration number required");
        this.regNo = regNo.trim().toUpperCase();
    }

    public final String getRegNo() { return regNo; }

    public abstract double ratePerHour();

    /** Every started hour is charged in full (61 min -> 2 hours). */
    public double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative");
        int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
        return hours * ratePerHour();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        return regNo.equals(((Vehicle) o).regNo);
    }
    @Override public int hashCode() { return regNo.hashCode(); }
    @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
}
