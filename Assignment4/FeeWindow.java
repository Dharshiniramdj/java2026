import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class FeeService {
    String pay(String registration, String text) throws Exception {
        if (registration.trim().isEmpty()) throw new IllegalArgumentException("Registration number required");
        double amount;
        try { amount = Double.parseDouble(text.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a valid amount"); }
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        Thread.sleep(500); // simulate remote confirmation, never on EDT
        return String.format("Receipt: %s paid INR %.2f", registration, amount);
    }
}
public class FeeWindow extends JFrame {
    private final JTextField reg = new JTextField(14), amount = new JTextField(10);
    private final JButton pay = new JButton("Pay"), clear = new JButton("Clear"), receipt = new JButton("Receipt");
    private final JLabel status = new JLabel("Ready");
    private String lastReceipt;
    public FeeWindow() {
        super("Student Fee Payment");
        JPanel fields = new JPanel(new GridLayout(2,2,6,6));
        fields.add(new JLabel("Register No:")); fields.add(reg);
        fields.add(new JLabel("Amount (INR):")); fields.add(amount);
        JPanel buttons = new JPanel(); buttons.add(pay); buttons.add(clear); buttons.add(receipt);
        setLayout(new BorderLayout(8,8)); add(fields,BorderLayout.NORTH);
        add(buttons,BorderLayout.CENTER); add(status,BorderLayout.SOUTH);
        pay.addActionListener(e -> payFee());
        clear.addActionListener(e -> { reg.setText(""); amount.setText(""); status.setText("Cleared"); });
        receipt.addActionListener(e -> JOptionPane.showMessageDialog(this,
                lastReceipt == null ? "No payment yet" : lastReceipt));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); pack(); setLocationRelativeTo(null);
    }
    private void payFee() {
        final String r=reg.getText(), a=amount.getText(); pay.setEnabled(false); status.setText("Processing...");
        new SwingWorker<String,Void>() {
            protected String doInBackground() throws Exception { return new FeeService().pay(r,a); }
            protected void done() {
                pay.setEnabled(true);
                try { lastReceipt=get(); status.setText("Payment successful"); JOptionPane.showMessageDialog(FeeWindow.this,lastReceipt); }
                catch (Exception ex) { Throwable cause=ex.getCause()==null?ex:ex.getCause(); status.setText("Payment failed"); JOptionPane.showMessageDialog(FeeWindow.this,cause.getMessage()); }
            }
        }.execute();
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true)); }
}
