package parking.model;
import java.time.LocalDate;

public class VisitorPass {
    private final String passNo;
    private final LocalDate issueDate;
    private final double fee;

    public VisitorPass(String passNo, LocalDate issueDate, double fee) {
        if (passNo == null || passNo.isBlank() || issueDate == null || fee < 0)
            throw new IllegalArgumentException("Invalid pass");
        this.passNo = passNo; this.issueDate = issueDate; this.fee = fee;
    }
    public String getPassNo() { return passNo; }
    public double getFee() { return fee; }
    public boolean isValidOn(LocalDate d) { return issueDate.equals(d); }
}
