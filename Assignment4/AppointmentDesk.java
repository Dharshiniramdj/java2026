import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class AppointmentDesk extends JFrame {
    private final JTextField patient = new JTextField(12);
    private final JComboBox<String> department = new JComboBox<>(new String[]{"Cardiology","Dermatology","General"});
    private final JComboBox<String> doctor = new JComboBox<>();
    private final JComboBox<String> slot = new JComboBox<>(new String[]{"09:00","10:00","11:00"});
    private final JLabel status = new JLabel("Choose appointment details");
    private final Map<String,String[]> doctors = new HashMap<>();
    private boolean inProgress=false;
    public AppointmentDesk() {
        super("Outpatient Appointment Desk");
        doctors.put("Cardiology",new String[]{"Dr Arun","Dr Meena"});
        doctors.put("Dermatology",new String[]{"Dr Priya","Dr Ravi"});
        doctors.put("General",new String[]{"Dr Kiran","Dr Sara"});
        JPanel form=new JPanel(new GridLayout(5,2,8,8));
        form.add(new JLabel("Patient ID")); form.add(patient);
        form.add(new JLabel("Department")); form.add(department);
        form.add(new JLabel("Doctor")); form.add(doctor);
        form.add(new JLabel("Time")); form.add(slot);
        JButton book=new JButton("Book"); form.add(book); form.add(status);
        add(form); department.addActionListener(e -> refreshDoctors());
        patient.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e){inProgress=true;}
            public void removeUpdate(javax.swing.event.DocumentEvent e){inProgress=true;}
            public void changedUpdate(javax.swing.event.DocumentEvent e){inProgress=true;}
        });
        book.addActionListener(e -> book()); refreshDoctors();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){ public void windowClosing(WindowEvent e){
            if(!inProgress || JOptionPane.showConfirmDialog(AppointmentDesk.this,"Discard current booking?","Close",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) dispose();
        }});
        pack();setLocationRelativeTo(null);
    }
    private void refreshDoctors(){doctor.removeAllItems(); for(String d: doctors.get((String)department.getSelectedItem())) doctor.addItem(d);}
    private void book(){
        if(patient.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Patient ID required");return;}
        String msg="Booked "+patient.getText().trim()+" with "+doctor.getSelectedItem()+" at "+slot.getSelectedItem();
        status.setText("Booked"); JOptionPane.showMessageDialog(this,msg); inProgress=false;
    }
    public static void main(String[] a){SwingUtilities.invokeLater(()->new AppointmentDesk().setVisible(true));}
}
